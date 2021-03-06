/*
 * Created on Oct 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.statistics;

import com.bos.art.logParser.broadcast.beans.BrowserBean;
import com.bos.art.logParser.broadcast.network.CommunicationChannel;
import com.bos.art.logParser.db.ConnectionPoolT;
import com.bos.art.logParser.records.ILiveLogParserRecord;
import com.bos.art.logParser.records.UserRequestTiming;
import com.bos.helper.MutableSingletonInstanceHelper;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author I0360D3
 *         <p/>
 *         To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 */
public class BrowserStats extends StatisticsUnit {

    private static final Logger logger = (Logger) Logger.getLogger(BrowserStats.class.getName());
    private static final int MINUTE_DELAY = 5;
    private static MutableSingletonInstanceHelper instance = new MutableSingletonInstanceHelper<BrowserStats>(BrowserStats.class) {
        @Override
        public java.lang.Object createInstance() {
            return new BrowserStats();
        }
    };
    private static long writeCount = 0;
    private static DateTimeFormatter sdfDate = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static DateTimeFormatter sdfFullDate = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private final String SQL_INSERT_STATEMENT = "insert into browserstats" + " (day, browser_id, count, state) values "
            + "(?, (select browser_id from browsers where patternmatchstring=? limit 1),?,?)";
    private final String SQL_UPDATE_STATEMENT = "update browserstats "
            + "  set count=?, state = ? where day =? and browser_id = (select browser_id from browsers where patternmatchstring = ? ) ";
    private ConcurrentHashMap<String, BrowserRecord> browsers;
    //private TreeMap sessionsTallied;
    private ConcurrentHashMap<String, Date> sessionsTallied;
    private int calls;
    private DateTime lastDataWriteTime;
    private java.util.Date currentDate;
    private BrowserRecord totalBrowsers;

    public BrowserStats() {
        browsers = new ConcurrentHashMap<String, BrowserRecord>();
        initBrowsers();
        lastDataWriteTime = new DateTime();
        totalBrowsers = new BrowserRecord("total", "total");
    }

    public static BrowserStats getInstance() {
        return (BrowserStats) instance.getInstance();
    }

    public void setInstance(StatisticsUnit su) {

        if (su instanceof BrowserStats) {

            if (instance.getInstance() != null) {
                ((BrowserStats) instance.getInstance()).setRunnable(false);
            }
            instance.setInstance(su);
        }
    }

    private void initBrowsers() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement("Select * from Browsers order by browser_id");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String patternKey = rs.getString("patternmatchstring");
                int browserID = rs.getInt("browser_id");
                String desc = rs.getString("description");
                String type = rs.getString("recordtype");
                BrowserRecord br = new BrowserRecord(patternKey, desc);

                br.setType(type);
                //browsers.put(patternKey, br);
                browsers.put(String.valueOf(browserID), br);
                logger.info("Loaded :" + br);

                // readOnlyBrowserPatterns.add(patternKey);
            }
        } catch (SQLException se) {
            logger.error("Exception In intBrowsers", se);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }
                if (con != null) {
                    con.close();
                    con = null;
                }
            } catch (SQLException se) {
                logger.error("Exception In initBrowsers", se);
            }
        }
    }

    private void reloadBrowsers() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement("Select * from Browsers order by browser_id");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String patternKey = rs.getString("patternmatchstring");
                int browserID = rs.getInt("browser_id");
                String desc = rs.getString("description");
                String type = rs.getString("recordtype");

                BrowserRecord br = browsers.get(String.valueOf(browserID));
                if (br != null) {
                    // update an existing record
                    br.setBrowserString(patternKey);
                    br.desc = desc;
                } else {
                    // it's a new record so add it
                    br = new BrowserRecord(patternKey, desc);
                    browsers.put(String.valueOf(browserID), br);
                }

                logger.info("re-Loaded :" + br);

                // readOnlyBrowserPatterns.add(patternKey);
            }
        } catch (SQLException se) {
            logger.error("Exception In intBrowsers", se);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }
                if (con != null) {
                    con.close();
                    con = null;
                }
            } catch (SQLException se) {
                logger.error("Exception In initBrowsers", se);
            }
        }
    }

    /**
     * We are only going to record this event if the Browser has not yet been tallied for this sessionsID. Browser stats are
     * also currently only being tallied for AccessRecord Stats.
     */
    public void processRecord(ILiveLogParserRecord rec) {
        String dateString = sdfDate.print(new DateTime());

        if (currentDate == null) {
            currentDate = new java.util.Date();
        }
        String stringCurrentDate = sdfDate.print(currentDate.getTime());

        if (!dateString.equals(stringCurrentDate) || sessionsTallied == null) {
            sessionsTallied = new ConcurrentHashMap<String, Date>();
            currentDate = new java.util.Date();
        }

        int recordCount = 0;

        if (rec.isAccessRecord()) {
            if (logger.isDebugEnabled()) {
                logger.debug("BrowserStat processRecord() is accessrecord");
            }
            ++calls;
            UserRequestTiming record = (UserRequestTiming) rec;
            String browserString = record.getBrowser();
            String sessionTxt = record.getSessionId();

            if (!sessionsTallied.containsKey(sessionTxt)) {

                if (logger.isDebugEnabled()) {
                    logger.debug("BrowserStat processRecord() is accessrecord is not recored yet.");
                }
                sessionsTallied.put(sessionTxt, new java.util.Date());
                recordBrowser(browserString);
            }
        }
        return;
    }

    private void recordBrowser(String bs) {
        int browserTimesTallied = 0;
        int osTimesTallied = 0;

        for (String nextBrowser : browsers.keySet()) {

            Pattern pattern = ((BrowserRecord) browsers.get(nextBrowser)).pattern;
            Matcher matcher = pattern.matcher(bs);

            if (matcher.find()) {
                BrowserRecord br = (BrowserRecord) browsers.get(nextBrowser);

                br.incrementCount();
                if (br.isOs) {
                    ++osTimesTallied;
                } else {
                    ++browserTimesTallied;
                }
            }
        }

        if (browserTimesTallied == 0) {
            logger.warn("NO Browser Matched useragent string -> " + bs);
        } else if (browserTimesTallied > 1) {
            logger.warn("Multiple Browser Matched (" + browserTimesTallied + ") (fix patterns!)- useragent string -> " + bs);
        }

        if (osTimesTallied == 0) {
            logger.warn("NO OS Matched useragent string -> " + bs);
        } else if (osTimesTallied > 1) {
            logger.warn("Multiple OS Matched (" + osTimesTallied + ") (fix patterns!)- useragent string -> " + bs);
        }

        //print stats about all browsers
        totalBrowsers.count++;
        if (totalBrowsers.count % 1000 == 0) {
            logger.info("---- Browser and OS Stats ----");

            for (String nextBrowser : browsers.keySet()) {
                BrowserRecord br = (BrowserRecord) browsers.get(nextBrowser);

                logger.info("BS : " + br.toString());
            }
        }
    }

    public static void main(String []args) {
        DateTime nextWriteDate = new DateTime();
        nextWriteDate = nextWriteDate.plusMinutes(10);
        System.out.println(nextWriteDate);
    }

    /*
     * (non-Javadoc) @see com.bos.art.logParser.statistics.StatisticsUnit#persistData()
     */
    public void persistData() {
        DateTime nextWriteDate = new DateTime(lastDataWriteTime);
        nextWriteDate = nextWriteDate.plusMinutes(MINUTE_DELAY);

        if (++writeCount % 20 == 0) {
            if (writeCount == Long.MAX_VALUE) {
                writeCount = 0;
            }
            System.out.println("reloading browsers at: " + new java.util.Date());
            reloadBrowsers();
        }

        if (new DateTime().isAfter(nextWriteDate)) {
            lastDataWriteTime = new DateTime();
            insertOrUpdate(totalBrowsers);

            for (String brKey : browsers.keySet()) {
                BrowserRecord br = browsers.get(brKey);

                insertOrUpdate(br);
                broadcast(br);
            }
        }
    }

    private void insertOrUpdate(BrowserRecord br) {
        if (br.isNew) {
            insertBrowserRecord(br);
        } else if (sdfDate.print(br.date.getTime()).equals(sdfDate.print(new DateTime()))) {
            updateBrowserRecord(br, "O");
        } else if (br.isDirty) {
            updateCloseBrowserRecord(br);
        }
    }

    private void updateCloseBrowserRecord(BrowserRecord br) {
        updateBrowserRecord(br, "C");
        if (!sdfDate.print(br.date.getTime()).equals(sdfDate.print(new DateTime()))) {
            try {
                br.date = sdfFullDate.parseDateTime(sdfDate.print(new DateTime()) + " 00:00:00").toDate();
            } catch (IllegalArgumentException pe) {
                logger.warn("Exception in update CloseRecord .. ", pe);
            }
            br.isNew = true;
            br.isDirty = true;
            br.count = 0;
        }
    }

    private void insertBrowserRecord(BrowserRecord br) {
        Connection con = null;

        try {
            con = getConnection();
            PreparedStatement pstmt = con.prepareStatement(SQL_INSERT_STATEMENT);
            String dateString = sdfDate.print(new DateTime());

            try {
                br.date = sdfFullDate.parseDateTime(dateString + " 00:00:00").toDate();
            } catch (IllegalArgumentException pe) {
                logger.warn("Exception in insert Browser Record .. ", pe);
            }
            pstmt.setDate(1, new java.sql.Date(br.date.getTime()));
            pstmt.setString(2, br.browserString);
            pstmt.setInt(3, br.count);
            pstmt.setString(4, "O");
            pstmt.execute();
            br.isNew = false;
            br.isDirty = false;
            pstmt.close();
        } catch (SQLException se) {
            String message = se.getMessage();

            if (message != null && message.indexOf("Duplicate entry") < 0) {
                br.isNew = false;
                try {

                    PreparedStatement pstmt2 = con.prepareStatement(
                            "select * from browserstats where day=? and browser_id=(select browser_id from browsers where patternmatchstring=? limit 1) ");

                    pstmt2.setDate(1, new java.sql.Date(br.date.getTime()));
                    pstmt2.setString(2, br.browserString);
                    ResultSet rs = pstmt2.executeQuery();

                    if (rs.next()) {
                        int count = rs.getInt("count");

                        br.count = count;
                        System.out.println("Updating Browser Stats for : " + br.browserString + " : to -> " + br.count);
                    }
                    rs.close();
                    pstmt2.close();
                    // se.printStackTrace();
                    // con.rollback();
                } catch (SQLException sse) {
                    sse.printStackTrace();
                }
            }

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Throwable t) {
                    logger.error("Error Closing Connection ... ", t);
                }
            }
        }
    }

    private void updateBrowserRecord(BrowserRecord br, String state) {
        Connection con = null;

        try {
            con = getConnection();
            PreparedStatement pstmt = con.prepareStatement(SQL_UPDATE_STATEMENT);

            pstmt.setInt(1, br.count);
            pstmt.setString(2, state);
            pstmt.setDate(3, new java.sql.Date(br.date.getTime()));
            pstmt.setString(4, br.browserString);
            pstmt.execute();
            br.isNew = false;
            br.isDirty = false;
            pstmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
//            try {
//                con.rollback();
//            } catch (SQLException sse) {
//                sse.printStackTrace();
//            }

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Throwable t) {
                    logger.error("Error Closing Conneciton ", t);
                }
            }
        }
    }

    Connection getConnection() throws SQLException {
        return ConnectionPoolT.getConnection();
    }

    public void flush() {
    }

    private void broadcast(BrowserRecord br) {

        BrowserBean bean = new BrowserBean();

        bean.setCount(br.getCount());
        bean.setBrowserString(br.getBrowserString());
        bean.setTotalCount(totalBrowsers.getCount());
        bean.setIsOs(br.isOs());
        bean.setDesc(br.getDesc());

        logger.info("Broadcast Called for Browser Stats...." + br.toString());
        try {
            CommunicationChannel.getInstance().broadcast(bean, null);
        } catch (Exception e) {
            logger.error("Error broadcasting data", e);
        }
    }

    private static class BrowserRecord implements Serializable {

        public String recordType;
        public Pattern pattern;
        public String browserString;
        public String desc;
        public boolean isOs;
        public int count;
        public java.util.Date date;
        public boolean isDirty;
        public boolean isNew;

        BrowserRecord(String s, String s2) {
            browserString = s;

            desc = s2;
            try {
                pattern = Pattern.compile(browserString);
            } catch (java.util.regex.PatternSyntaxException e) {
                logger.warn(" Exception with pattern " + browserString, e);
            }

            count = 0;
            isNew = true;
            isDirty = true;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getBrowserString() {
            return browserString;
        }

        public boolean isOs() {
            return isOs;
        }

        public void setOs(boolean isOs) {
            this.isOs = isOs;
        }

        public void setType(String t) {
            recordType = t;
            if (recordType.equalsIgnoreCase("O")) {
                isOs = true;
            } else {
                isOs = false;
            }
        }

        public void setBrowserString(String s) {
            try {
                pattern = Pattern.compile(s);
            } catch (java.util.regex.PatternSyntaxException e) {
                logger.warn(" Exception with pattern " + browserString, e);
            }
        }

        void incrementCount() {
            count++;
            isDirty = true;
        }

        public String toString() {
            return browserString + ":" + count + ":" + isNew + ":" + isDirty;
        }
    }
}
