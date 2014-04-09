package com.bos.art.logParser.statistics;

import com.bos.art.logParser.broadcast.beans.ExternalAccessRecordsMinuteBean;

/**
 * Created by i0360b6 on 3/25/14.
 */
public class ExternalAccessRecordsMinuteBeanImpl extends ExternalAccessRecordsMinuteBean{
    private static final int MACHINE_START_INDEX = 12;
    private static final String START_CLASSIFICATION_DELIMETER = "#START_CLASSIFICATION#";
    private static final String START_SERVER_DELIMETER = "#START_SERVER#";

    public ExternalAccessRecordsMinuteBeanImpl(TimeSpanEventContainer tsec, String lkey) {
        //key = lkey;
        setKey(lkey);
        timeString = lkey.substring(0, MACHINE_START_INDEX);

//        System.out.println("lkey: " + lkey);

        int startMachine = lkey.indexOf(START_SERVER_DELIMETER) + START_SERVER_DELIMETER.length();
        int endMachine = lkey.indexOf(START_CLASSIFICATION_DELIMETER);
        int startClassification = endMachine + START_CLASSIFICATION_DELIMETER.length();
        machine = lkey.substring(startMachine, endMachine);
        classificationID = 0;
        try {
            classificationID = Integer.parseInt(lkey.substring(startClassification));
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            logger.error("ExternalAccessRecordsMinuteBeanImpl", e);
        }

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
    }
}
