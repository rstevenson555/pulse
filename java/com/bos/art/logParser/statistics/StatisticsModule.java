/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.statistics;

import com.bos.art.logParser.broadcast.beans.AccessRecordsMinuteBean;
import com.bos.art.logParser.broadcast.beans.ExternalAccessRecordsMinuteBean;
import com.bos.art.logParser.broadcast.beans.TransferBean;
import com.bos.art.logParser.broadcast.beans.MinuteStatsKey;

import com.bos.art.logParser.db.ForeignKeyStore;
import com.bos.art.logServer.utils.Scheduler;
import com.bos.helper.SingletonInstanceHelper;
import org.apache.log4j.Logger;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.bos.art.logServer.utils.TimeIntervalConstants.*;

/**
 * @author I0360D3
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class StatisticsModule extends TimerTask implements Serializable {
    private static Logger logger = null;
    private static final DateTimeFormatter fdfKey = DateTimeFormat.forPattern("yyyyMMddHHmm");

    private static SingletonInstanceHelper instance = new SingletonInstanceHelper<StatisticsModule>(StatisticsModule.class) {
        @Override
        public java.lang.Object createInstance() {
            return new StatisticsModule();
        }
    };

    static {
        logger = (Logger) Logger.getLogger(StatisticsModule.class.getName());
    }

    // made this a copyonwritearraylist, so we don't have to sync, and it's not added to
    // frequently so we don't need to worry about the overhead
    transient private CopyOnWriteArrayList<StatisticsUnit> statUnits;
    transient private Scheduler timer;
    transient private Scheduler timerClean;
    transient private DateTimeFormatter fdf = DateTimeFormat.forPattern("yyyy-MM/dd HH:mm:ss");
    private int FOUR_HOURS_OF_DATA = 4;

    private StatisticsModule() {
        statUnits = new CopyOnWriteArrayList<StatisticsUnit>();
        timer = Scheduler.getInstance("timer", Thread.NORM_PRIORITY);
        timerClean = Scheduler.getInstance("cleaner", Thread.NORM_PRIORITY);
//		        
        logger.warn("Scheduling tasks");
        timerClean.executePeriodically(THREE_MINUTES_MILLIS, new MyRunnable(this) {
            public void run() {
                try {
                    super.run();
                } catch (Exception e) {
                    logger.error("Error running StatisticsModule schedule:", e);
                }
            }
        }, false);
        //timerClean.executePeriodically(30*60,new Thread(ForeignKeyStore.getInstance()) {
        timerClean.executePeriodically(THIRTY_SECONDS_MILLIS, new MyRunnable(ForeignKeyStore.getInstance()) {
            public void run() {
                try {
                    super.run();
                } catch (Exception e) {
                    logger.error("Error running ForeignKeyStore schedule:", e);
                }
            }
        }, false);
        timerClean.executePeriodically(ONE_HOUR_MILLIS, new Thread(com.bos.art.logParser.db.maintanence.AccessRecordsCleanerDriver.getInstance()) {
            public void run() {
                try {
                    super.run();
                } catch (Exception e) {
                    logger.error("Error running AccessRecordsCleanerDriver schedule:", e);
                }
            }
        }, false);
        // establish callback to cancel after 60 seconds

        DateMidnight dm = new DateMidnight();
        DateTime d = dm.toDateTime();

        d = d.plusDays(1);
        Date midnight = d.toDate();

        timerClean.scheduleAtFixedRate(new Thread() {
            public void run() {
                try {
                    if (ForeignKeyStore.getInstance() != null) {
                        if (ForeignKeyStore.getInstance().getUserIdTree() != null) {
                            logger.warn("***********Clearing User Queue***************");
                            ForeignKeyStore.getInstance().getUserIdTree().clear();

                            logger.warn("***********Clearing Pages Queue***************");
                            ForeignKeyStore.getInstance().getPageIdTree().clear();
                        } else {
                            logger.warn("ForeignKeyStore userIdTree null");
                        }
                    } else {
                        logger.warn("ForeignKeyStore instance is null");
                    }
                } catch (Exception e) {
                    logger.error("ForeignKeyStore Error clearing queue:", e);
                }
            }
        }, midnight, ONE_DAY_MILLIS);
    }

    public static StatisticsModule getInstance() {
        return (StatisticsModule) instance.getInstance();
    }

    public void addStatUnit(StatisticsUnit su) {
        statUnits.add(su);
        timer.executePeriodically(FIFTEEN_SECONDS_MILLIS, new MyRunnable(new Thread(su)), true);
    }

    public void removeStatUnit(StatisticsUnit su) {
        statUnits.remove(su);
        su.cancel();
    }

    public Iterator iterator() {
        return statUnits.iterator();
    }

    public String getAllStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n");
        Date now = new java.util.Date();
        sb.append("STATISTICS RUN TIME  ").append(fdf.print(now.getTime()));
        sb.append("\n\n");
        for (StatisticsUnit su : statUnits) {
            sb.append(su.toString());
        }
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            if (logger.isInfoEnabled()) {
                logger.warn(getAllStatistics());
            }
        } catch (Throwable e) {
            logger.error("StatisticsModule threw serious EXCEPTION!", e);
        }
    }

    public Collection<TransferBean> getAllBeans() {
        DateTime now = new DateTime();
        long broadCastLimitTime = now.minusHours(FOUR_HOURS_OF_DATA).toInstant().getMillis();

        logger.warn("getAllBeans Called...");
        List<TransferBean> beans = new ArrayList<TransferBean>();

        AccessRecordsMinuteStats mStats = AccessRecordsMinuteStats.getInstance();
        Map<MinuteStatsKey, TimeSpanEventContainer> mStatsData = mStats.getData();

        for (MinuteStatsKey s : mStatsData.keySet()) {
            TimeSpanEventContainer timeSpanEventContainer = mStatsData.get(s);
            if (timeSpanEventContainer.getTime().getTimeInMillis() > broadCastLimitTime) {
                //beans.add(new AccessRecordsMinuteBeanImpl(timeSpanEventContainer, s));
                beans.add(new AccessRecordsMinuteBean() {
                    public AccessRecordsMinuteBean setData(TimeSpanEventContainer tsec, MinuteStatsKey lkey) {
                        //mkey = lkey;
                        setMkey(lkey);
                        context = tsec.getContext();
                        machine = lkey.getServerName();
                        instance = lkey.getInstanceName();
                        timeString = fdfKey.print(new DateTime(lkey.getTime()));
                        totalLoads = tsec.getTotalLoads();
                        averageLoadTime = tsec.getAverageLoadTime();
                        totalLoadTime = tsec.getTotalLoadTime();
                        maxLoadTime = tsec.getMaxLoadTime();
                        minLoadTime = tsec.getMinLoadTime();
                        distinctUsers = tsec.getDistinctUsers();
                        totalUsers = tsec.getTotalUsers();
                        errorPages = tsec.getErrorPages();
                        thirtySecondLoads = tsec.getThirtySecondLoads();
                        twentySecondLoads = tsec.getTwentySecondLoads();
                        fifteenSecondLoads = tsec.getFifteenSecondLoads();
                        tenSecondLoads = tsec.getTenSecondLoads();
                        fiveSecondLoads = tsec.getFiveSecondLoads();
                        i90Percentile = tsec.get90Percentile();
                        i75Percentile = tsec.get75Percentile();
                        i50Percentile = tsec.get50Percentile();
                        i25Percentile = tsec.get25Percentile();
                        return this;
                    }
                }.setData(timeSpanEventContainer, s));
            }
        }

        logger.warn("AccessRecords Minute Stat Beans Collected... :" + beans.size()
                + " : Total Time to Collect those Beans : " + (System.currentTimeMillis() - now.toInstant().getMillis()));
        ExternalTimingMachineClassificationMinuteStats emStats = ExternalTimingMachineClassificationMinuteStats.getInstance();
        Map<String, TimeSpanEventContainer> timeSpanEventContainerMap = emStats.getData();

        for (String s : timeSpanEventContainerMap.keySet()) {
            TimeSpanEventContainer timeSpanEventContainer = timeSpanEventContainerMap.get(s);
            if (timeSpanEventContainer.getTime().getTimeInMillis() > broadCastLimitTime) {
                beans.add(new ExternalAccessRecordsMinuteBeanImpl(timeSpanEventContainer, s));
            }
        }
        logger.warn("Total Beans Collected From Backfill Broadcast :" + beans.size()
                + " : Total Time to Collect those Beans : " + (System.currentTimeMillis() - now.toInstant().getMillis()));

        return beans;
    }
    // upon connect send four hours of data out to the new client

    private class MyRunnable implements Runnable {
        private Runnable runnable;

        MyRunnable(Runnable r) {
            runnable = r;
        }

        public void run() {

            try {
                runnable.run();
            } catch(Exception e) {
                logger.error("MyRunnable exception",e);
            } catch(Throwable t) {
                logger.error("MyRunnable throwable",t);
            }
        }
    }
}

