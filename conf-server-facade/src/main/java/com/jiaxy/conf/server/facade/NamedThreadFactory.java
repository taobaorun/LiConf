package com.jiaxy.conf.server.facade;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description: <br/>
 * <p/>
 * Company:
 *
 * @author: <a href=mailto:taobaorun@gmail.com>tom</a>
 * <br/>
 * @Date: 2017/09/06 17:56
 */
public class NamedThreadFactory implements ThreadFactory {


    private boolean daemon = true;

    private String name;

    private ThreadGroup group;

    private AtomicInteger integer = new AtomicInteger();

    public NamedThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
    }

    public NamedThreadFactory(boolean daemon, String name) {
        this.daemon = daemon;
        this.name = name;
    }

    public NamedThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        if (name == null || "".equals(name)) {
            name = "conf-thread-" + integer.incrementAndGet();
        }
        Thread thread = new Thread(group, r, name);
        thread.setDaemon(daemon);
        return thread;
    }
}
