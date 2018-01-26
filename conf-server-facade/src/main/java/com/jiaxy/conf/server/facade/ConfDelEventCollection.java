package com.jiaxy.conf.server.facade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

/**
 * Description: <br/>
 * <p/>
 * Company:
 *
 * @author: <a href=mailto:taobaorun@gmail.com>tom</a>
 * <br/>
 * @Date: 2017/09/06 17:16
 */
public class ConfDelEventCollection {

    private static final Logger logger = LoggerFactory.getLogger(ConfDelEventCollection.class);


    private ConcurrentHashMap<String, ConfDelEvent> confDelEventMap = new ConcurrentHashMap<>();

    private DelayQueue<ConfDelEvent> queue = new DelayQueue<>();

    public ConfDelEventCollection() {
        startExpireThread();
    }

    public void addConfDelEvent(long expire,
                                String confOwner,
                                String path,
                                String confKey,
                                Integer version) {
        String key = buildKey(confOwner, path, confKey, version);
        ConfDelEvent old = confDelEventMap.putIfAbsent(key, new ConfDelEvent(expire, confOwner, path, confKey, version));
        if (old == null) {
            queue.offer(confDelEventMap.get(key));
        }
    }

    public ConfDelEvent getConfDelEvent(
            String confOwner,
            String path,
            String confKey,
            Integer version) {
        return confDelEventMap.get(buildKey(confOwner, path, confKey, version));
    }

    /**
     * get all del events belong to the path of the conf owner
     *
     * @param confOwner
     * @param path
     * @return
     */
    public List<ConfDelEvent> getConfDelEvents(
            String confOwner,
            String path
    ) {
        Map<String, ConfDelEvent> map = new HashMap<>(confDelEventMap);
        if (map.isEmpty()) {
            return null;
        }
        List<ConfDelEvent> events = new ArrayList<>();
        for (ConfDelEvent event : map.values()) {
            if (event.getConfOwner().equals(confOwner) &&
                    event.getPath().equals(path)) {
                events.add(event);
            }
        }
        return events;
    }

    public ConfDelEvent removeConfDelEvent(
            String confOwner,
            String path,
            String confKey,
            Integer version) {
        return confDelEventMap.remove(buildKey(confOwner, path, confKey, version));
    }

    /**
     * @param confOwner
     * @param path
     * @param confKey
     * @param version
     * @return
     */
    private String buildKey(String confOwner, String path, String confKey, Integer version) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(confOwner)
                .append("#")
                .append(path);
        if (confKey != null && !"".equals(confKey)) {
            keyBuilder.append("#")
                    .append(confKey);
        }
        if (version != null) {
            keyBuilder.append("#")
                    .append(version);
        }
        return keyBuilder.toString();
    }

    private void startExpireThread() {
        Thread expireThread = new Thread(() -> {
            while (true) {
                try {
                    ConfDelEvent event = queue.take();
                    if (event != null) {
                        removeConfDelEvent(event.getConfOwner(),
                                event.getPath(),
                                event.getConfKey(),
                                event.getVersion());
                        logger.info("confOwner:{},path:{},confKey:{},version:{} conf del event expired.",
                                event.getConfOwner(),
                                event.getPath(),
                                event.getConfKey(),
                                event.getVersion());
                    }
                } catch (InterruptedException e) {
                    logger.error("conf del event expire thread error.", e);
                }
            }

        }, "conf-del-event-expired-thread");
        expireThread.start();
        logger.info("conf-del-event-expired-thread started");
    }


}

