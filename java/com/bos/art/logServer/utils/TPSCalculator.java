package com.bos.art.logServer.utils;

import org.joda.time.DateTime;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: i0360b6
 * Date: 1/29/14
 * Time: 8:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class TPSCalculator {
    private class CalcRecord {
        int count;
        DateTime periodStart;

        CalcRecord() {
            periodStart = new DateTime();
            count = 0;
        }
    }

    private Map<Integer,CalcRecord> tpsRecordMap = new LinkedHashMap<Integer,CalcRecord>()
    {
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > 1;
        }
    };

    public void incrementTransaction() {
        DateTime now = new DateTime();
        int minute = now.getMinuteOfHour();
        CalcRecord calcRecord = tpsRecordMap.get(minute);
        if ( calcRecord == null) {
            calcRecord = new CalcRecord();
            tpsRecordMap.put(minute, calcRecord);
        }
        calcRecord.count++;
    }

    public long getMessagesPerSecond() {
        DateTime now = new DateTime();
        CalcRecord calcRecord = tpsRecordMap.get(now.getMinuteOfHour());
        return calcRecord.count / ((now.toDateTime().getMillis() - calcRecord.periodStart.toDateTime().getMillis())/1000);
    }
}