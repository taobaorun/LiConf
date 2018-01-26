package com.jiaxy.conf.server.facade;

import com.jiaxy.conf.server.domain.ConfInfo;
import com.jiaxy.conf.server.service.ConfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Description: <br/>
 * <p/>
 * Company:
 *
 * @author: <a href=mailto:taobaorun@gmail.com>tom</a>
 * <br/>
 * @Date: 2017/04/24 11:10
 */
@Component
public class ConfServiceHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConfServiceHandler.class);


    @Resource
    private ConfService confService;

    //must singleton
    private static final ConfDelEventCollection delEventCol = new ConfDelEventCollection();


    private static final ThreadPoolExecutor confThreadPool = new ThreadPoolExecutor(20,
            200,
            0,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue(1000),
            new NamedThreadFactory()
    );


    public DeferredResult<ResponseEntity<ConfInfo>> execute(boolean async,
                                                            String confOwner,
                                                            String path,
                                                            String key,
                                                            Integer version,
                                                            Long wait,
                                                            boolean emptyReturn) {
        if (async) {
            return asyncExecute(confOwner, path, key, version, wait, emptyReturn);
        } else {
            DeferredResult<ResponseEntity<ConfInfo>> result = new DeferredResult<>();
            ConfInfo confInfo = confService.load(confOwner, path, key, version);
            if (confInfo != null) {
                result.setResult(ResponseEntity.ok(confInfo));
            } else {
                result.setResult(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
            }
            return result;
        }
    }


    /**
     * @param confOwner
     * @param path
     * @param key
     * @param version
     * @param wait      等待时间 millis
     * @return
     */
    public DeferredResult<ResponseEntity<ConfInfo>> asyncExecute(String confOwner,
                                                                 String path,
                                                                 String key,
                                                                 Integer version,
                                                                 Long wait,
                                                                 boolean emptyReturn) {
        if (wait == null) {
            wait = 5000L;
        }
        DeferredResult<ResponseEntity<ConfInfo>> result = new DeferredResult<>(wait);
        result.onTimeout(() -> {
            logger.debug("confOwner:{},path:{},confKey:{},version:{} deferred timeout",
                    confOwner, path, key, version);
        });
        result.onCompletion(() -> {
            logger.debug("confOwner:{},path:{},confKey:{},version:{} deferred completed",
                    confOwner, path, key, version);
        });
        confThreadPool.execute(() -> {
            if (result.isSetOrExpired()) {
                logger.info("path %s, key %s expired ", path, key);
                return;
            }
            ConfInfo confInfo = confService.load(confOwner, path, key, version);

            if (confInfo != null) {
                result.setResult(ResponseEntity.ok(confInfo));
            } else {
                ConfDelEvent event = delEventCol.getConfDelEvent(confOwner, path, key, version);
                if (event != null) {
                    logger.debug("send del event.{}", event);
                    result.setResult(ResponseEntity.ok(buildFromDelEvent(event)));
                } else {
                    if (emptyReturn) {
                        result.setResult(ResponseEntity.status(HttpStatus.NO_CONTENT).body(null));
                        return;
                    }
                }
            }

        });
        return result;
    }


    public DeferredResult<ResponseEntity<List<ConfInfo>>> execute(boolean async, String confOwner, String path, Long wait, boolean emptyReturn) {
        if (async) {
            return asyncExecute(confOwner, path, wait, emptyReturn);
        } else {
            DeferredResult<ResponseEntity<List<ConfInfo>>> result = new DeferredResult<>();
            List<ConfInfo> confInfos = confService.load(confOwner, path);
            if (confInfos != null && !confInfos.isEmpty()) {
                result.setResult(ResponseEntity.ok(confInfos));
            } else {
                result.setResult(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
            }
            return result;
        }
    }


    public DeferredResult<ResponseEntity<List<ConfInfo>>> asyncExecute(String confOwner, String path, Long wait, boolean emptyReturn) {
        if (wait == null) {
            wait = 5000L;
        }
        DeferredResult<ResponseEntity<List<ConfInfo>>> result = new DeferredResult<>(wait);
        result.onTimeout(() -> {
            logger.debug("confOwner:{},path:{} deferred timeout",
                    confOwner, path);
        });
        result.onCompletion(() -> {
            logger.debug("confOwner:{},path:{} deferred completed",
                    confOwner, path);
        });
        confThreadPool.execute(() -> {
            if (result.isSetOrExpired()) {
                logger.info("path %s expired ", path);
                return;
            }
            List<ConfInfo> confInfos = confService.load(confOwner, path);
            if (confInfos != null && !confInfos.isEmpty()) {
                result.setResult(ResponseEntity.ok(confInfos));
            } else {
                //优先删除事件下发
                ConfDelEvent event = delEventCol.getConfDelEvent(confOwner, path, null, null);
                if (event != null) {
                    confInfos = new ArrayList<>();
                    confInfos.add(buildFromDelEvent(event));
                    result.setResult(ResponseEntity.ok(confInfos));
                } else {
                    if (emptyReturn) {
                        result.setResult(ResponseEntity.status(HttpStatus.NO_CONTENT).body(null));
                        return;
                    }
                }
            }

        });
        return result;
    }


    public void publishDelConfEvent(String confOwner, String path, String confKey, Integer version) {
        delEventCol.addConfDelEvent(30000L, confOwner, path, confKey, version);
    }


    private ConfInfo buildFromDelEvent(ConfDelEvent event) {
        ConfInfo info = new ConfInfo();
        info.setConfOwner(event.getConfOwner());
        info.setPath(event.getPath());
        info.setConfKey(event.getConfKey());
        //代表是删除
        info.setVersion(Integer.MIN_VALUE);
        return info;
    }

}
