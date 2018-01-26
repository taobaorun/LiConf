package com.jiaxy.conf.server.facade;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Description: <br/>
 * <p/>
 * Company:
 *
 * @author: <a href=mailto:taobaorun@gmail.com>tom</a>
 * <br/>
 * @Date: 2017/09/06 17:52
 */
public class ConfDelEvent implements Delayed {

    //expire millis
    private long expire;

    private long start;

    private String confOwner;

    private String path;

    private String confKey;

    private Integer version;


    public ConfDelEvent(long expire, String confOwner, String path) {
        this(expire, confOwner, path, null);
    }

    public ConfDelEvent(long expire, String confOwner, String path, String confKey) {
        this(expire, confOwner, path, confKey, null);
    }

    public ConfDelEvent(long expire, String confOwner, String path, String confKey, Integer version) {
        this.expire = expire;
        this.start = System.currentTimeMillis() + this.expire;
        this.confOwner = confOwner;
        this.path = path;
        this.confKey = confKey;
        this.version = version;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long elapsed = start - System.currentTimeMillis();
        return unit.convert(elapsed, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return this.start - ((ConfDelEvent) o).start < 0 ? -1 : 1;
    }

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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ConfDelEvent{" +
                "expire=" + expire +
                ", start=" + start +
                ", confOwner='" + confOwner + '\'' +
                ", path='" + path + '\'' +
                ", confKey='" + confKey + '\'' +
                ", version=" + version +
                '}';
    }
}
