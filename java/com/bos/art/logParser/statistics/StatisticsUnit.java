/*
 * Created on Oct 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.art.logParser.statistics;

import com.bos.art.logParser.records.ILiveLogParserRecord;
import org.apache.log4j.Logger;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;
import java.util.TimerTask;

/**
 * @author I0360D3
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class StatisticsUnit extends TimerTask implements Serializable {

    private static final Logger logger = (Logger) Logger.getLogger(StatisticsUnit.class.getName());
    protected boolean runnable = true;

    abstract public void processRecord(ILiveLogParserRecord record);

    abstract public void persistData();

    abstract public void flush();

    abstract public void setInstance(StatisticsUnit su);

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        if (!runnable) {
            return;
        }
        try {
            persistData();
        } catch (Throwable e) {
            logger.error("StatisticsUnit threw serious EXCEPTION! ", e);
        }
    }

    /**
     * strip the time portion off of a date
     */
    protected Date stripTime(Date d) {
        DateMidnight midnight = new DateMidnight(d);
        return midnight.toDate();
    }

    protected boolean shouldCloseRecord(IEventContainer tsec) {
        DateTime currentDate = new DateTime();

        if (currentDate.isAfter(tsec.getCloseTimeForData().getTimeInMillis())
                && currentDate.isAfter(tsec.getCloseTimeForMod().getTimeInMillis())) {
            return true;
        }
        return false;
    }

    public boolean isRunnable() {
        return runnable;
    }

    public void setRunnable(boolean runnable) {
        this.runnable = runnable;
    }
}
