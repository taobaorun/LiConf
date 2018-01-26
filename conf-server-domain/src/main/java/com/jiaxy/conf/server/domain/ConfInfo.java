package com.jiaxy.conf.server.domain;

import java.util.Date;


public class ConfInfo {

    private int id;

    /**
     * conf owner:
     *
     * e.g. application
     */
    private String confOwner;

    private String path;

    private String confKey;

    private String confValue;

    private int version;

    private Date timeStamp;


    public String getConfOwner() {
        return confOwner;
    }

    public void setConfOwner(String confOwner) {
        this.confOwner = confOwner;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getConfKey() {
        return confKey;
    }

    public void setConfKey(String confKey) {
        this.confKey = confKey;
    }

    public String getConfValue() {
        return confValue;
    }

    public void setConfValue(String confValue) {
        this.confValue = confValue;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
