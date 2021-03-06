/*
 * Created on Oct 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.statistics;

import com.bos.art.logParser.tools.MemoryTool;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author I0360D3
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AccumulatorEventContainer implements Serializable, IEventContainer {

    private static final Logger logger = (Logger) Logger.getLogger(AccumulatorEventContainer.class.getName());
    private static final DateTimeFormatter fdf = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");
    //  Class Administative Parameters.
    //
    // dirty                 - determines whether to recalculate the Percentiles.
    // databaseDirty         - either use insert or update.
    // lastModDate           - Used for determining Closure (Imprtant to prevent Memory Leak
    // isReloaded            - Reloads can't determine Percentiles because the TreeMap is gone.
    // timesPersisted        - Count How Many Times this is Persisted.
    // persistOpportunities  - Count How Many Times this is Persisted.
    private boolean dirty = true;
    private boolean databaseDirty = true;
    private java.util.Date lastModDate;
    private boolean isReload = false;
    private int timesPersisted = 0;
    private int persistOpportunities = 0;
    // persistance Times
    //
    private int modDelayMinutes = 15;
    private int dataDelayMinutes = 15;
    private DateTime closeTimeForMod = null;
    private DateTime closeTimeForData = null;
    private int contextID = 0;
    //  Identifying Attributes.
    //  Some of these won't be used, but I wanted it to contain all possiblilities.
    private Calendar mtime;
    private int accumulatorId;
    // Statistical Data
    private int accumulationStat;
    private int accumulationCount;

    public AccumulatorEventContainer(int accId, Calendar time, int contextid) {
        this.mtime = time;
        accumulatorId = accId;
        contextID = contextid;

        closeTimeForData = new DateTime(mtime);
        closeTimeForData = closeTimeForData.plusMinutes(dataDelayMinutes);

        closeTimeForMod = new DateTime();
        closeTimeForMod = closeTimeForMod.plusMinutes(modDelayMinutes);
    }

    public AccumulatorEventContainer(int accId, Calendar time, int val, int cnt, int contextid) {
        this.mtime = time;
        accumulatorId = accId;
        contextID = contextid;

        closeTimeForData = new DateTime(mtime);
        closeTimeForData = closeTimeForData.plusMinutes(dataDelayMinutes);

        closeTimeForMod = new DateTime();
        closeTimeForMod = closeTimeForMod.plusMinutes(modDelayMinutes);

        accumulationStat = val;
        accumulationCount = cnt;
    }

    public int getAccumulatorId() {
        return accumulatorId;
    }

    public void setAccumulatorId(int accumulatorId) {
        this.accumulatorId = accumulatorId;
    }

    public int getAccumulationCount() {
        return accumulationCount;
    }

    public void tally(int value, String function) {
        accumulationCount++;
        dirty = true;
        databaseDirty = true;
        lastModDate = new java.util.Date();
        closeTimeForMod = new DateTime(lastModDate);
        closeTimeForMod = closeTimeForMod.plusMinutes(modDelayMinutes);

        if (function == null) {
            return;
        } else if (function.equalsIgnoreCase("sum")) {
            accumulationStat += value;
        } else if (function.equalsIgnoreCase("multiply")) {
            accumulationStat *= value;
        } else if (function.equals("sample")) {
            accumulationStat = value;
        }
    }

    /**
     * @return
     */
    public Calendar getTime() {
        return mtime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\time:").append(fdf.print(mtime.getTimeInMillis()));
        return sb.toString();
    }

    public String tooString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\t\ttime:....................").append(mtime.toString());
        sb.append("\n\t\tSize in bytes............").append(MemoryTool.showSize(this, "DailyEventContainer"));
        return sb.toString();
    }

    /**
     * @return
     */
    public java.util.Date getLastModDate() {
        return lastModDate;
    }


    /**
     * @return
     */
    public boolean isDatabaseDirty() {
        return databaseDirty;
    }


    /**
     * @param b
     */
    public void setDatabaseDirty(boolean b) {
        databaseDirty = b;
    }

    /**
     * @return
     */
    public int getTimesPersisted() {
        return timesPersisted;
    }

    /**
     * @param i
     */
    public void setTimesPersisted(int i) {
        timesPersisted = i;
    }

    /**
     * @return
     */
    public int getPersistOpportunities() {
        return persistOpportunities;
    }

    /**
     * @param i
     */
    public void setPersistOpportunities(int i) {
        persistOpportunities = i;
    }

    /**
     * @return
     */
    public Calendar getCloseTimeForData() {
        return closeTimeForData.toGregorianCalendar();
    }

    /**
     * @return
     */
    public Calendar getCloseTimeForMod() {
        return closeTimeForMod.toGregorianCalendar();
    }

    /**
     * @param i
     */
    public void setDataDelayMinutes(int i) {
        dataDelayMinutes = i;
    }

    /**
     * @param i
     */
    public void setModDelayMinutes(int i) {
        modDelayMinutes = i;
    }

    public int getAccumulationStat() {
        return accumulationStat;
    }

    public void setAccumulationStat(int accumulationStat) {
        this.accumulationStat = accumulationStat;
    }


    public int getContextID() {
        return contextID;
    }

    public void setContextID(int contextID) {
        this.contextID = contextID;
    }

}
