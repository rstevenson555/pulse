package com.bos.art.logParser.broadcast.beans;

import java.io.Serializable;
import java.util.Date;

public class MinuteStatsKey implements Serializable {
    private Date time;
    private String serverName;
    private String instanceName;
    private int matchResolution = FULL_RESOLUTION;

    public static int TIME_RESOLUTION = 1;
    public static int FULL_RESOLUTION = 10;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String server) {
        serverName = server;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instance) {
        instanceName = instance;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date t) {
        time = t;
    }

    public void setMatchingResolution(int resolution) {
        matchResolution = resolution;
    }

    @Override
    public boolean equals(Object o) {
        if (null == o) return true;

        MinuteStatsKey that = (MinuteStatsKey) o;

        if (matchResolution == FULL_RESOLUTION) {
            if (instanceName != null ? !instanceName.equals(that.instanceName) : that.instanceName != null)
                return false;
            if (serverName != null ? !serverName.equals(that.serverName) : that.serverName != null) return false;
        }
        if (time != null ? !time.equals(that.time) : that.time != null) return false;

        return true;
    }

    @Override
    public String toString() {
        return "MinuteStatsKey{" +
                "time=" + time +
                ", serverName='" + serverName + '\'' +
                ", instanceName='" + instanceName + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        int result = time != null ? time.hashCode() : 0;
        if (matchResolution == FULL_RESOLUTION) {
            result = 31 * result + (serverName != null ? serverName.hashCode() : 0);
            result = 31 * result + (instanceName != null ? instanceName.hashCode() : 0);
        }
        return result;
    }
}