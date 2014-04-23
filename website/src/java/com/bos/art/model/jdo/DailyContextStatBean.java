package com.bos.art.model.jdo;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class DailyContextStatBean implements Serializable {

    /** identifier field */
    private Date day;

    /** identifier field */
    private int count;

    /** identifier field */
    private String state;

    /** identifier field */
    private Date lastModTime;

    /** persistent field */
    private com.bos.art.model.jdo.ContextBean context;

    /** full constructor */
    public DailyContextStatBean(Date day, int count, String state, Date lastModTime, com.bos.art.model.jdo.ContextBean context) {
        this.day = day;
        this.count = count;
        this.state = state;
        this.lastModTime = lastModTime;
        this.context = context;
    }

    /** default constructor */
    public DailyContextStatBean() {
    }

    public Date getDay() {
        return this.day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getLastModTime() {
        return this.lastModTime;
    }

    public void setLastModTime(Date lastModTime) {
        this.lastModTime = lastModTime;
    }

    public com.bos.art.model.jdo.ContextBean getContext() {
        return this.context;
    }

    public void setContext(com.bos.art.model.jdo.ContextBean context) {
        this.context = context;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("day", getDay())
            .append("count", getCount())
            .append("state", getState())
            .append("lastModTime", getLastModTime())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof DailyContextStatBean) ) return false;
        DailyContextStatBean castOther = (DailyContextStatBean) other;
        return new EqualsBuilder()
            .append(this.getDay(), castOther.getDay())
            .append(this.getCount(), castOther.getCount())
            .append(this.getState(), castOther.getState())
            .append(this.getLastModTime(), castOther.getLastModTime())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getDay())
            .append(getCount())
            .append(getState())
            .append(getLastModTime())
            .toHashCode();
    }

}
