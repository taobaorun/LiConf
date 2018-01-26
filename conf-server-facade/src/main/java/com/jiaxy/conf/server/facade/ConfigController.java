package com.jiaxy.conf.server.facade;


import com.jiaxy.conf.server.cache.CacheCascade;
import com.jiaxy.conf.server.domain.ConfInfo;
import com.jiaxy.conf.server.service.ConfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description: <br/>
 * <p/>
 * Company:
 *
 * @author: <a href=mailto:taobaorun@gmail.com>tom</a>
 * <br/>
 * @Date: 2017/04/14 18:05
 */
@Controller
@RequestMapping("cf")
public class ConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    @Resource
    private ConfService confService;

    @Resource
    private ConfServiceHandler confServiceHandler;

    @Resource
    private CacheCascade cacheCascade;


    @RequestMapping(value = "/{path}/{key:.+}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DeferredResult<ResponseEntity<ConfInfo>> getConfigValue(@PathVariable("key") String key,
                                                                   String confOwner,
                                                                   @PathVariable("path") String path,
                                                                   @RequestParam(required = false, defaultValue = "false") boolean async,
                                                                   @RequestParam(required = false) Integer version,
                                                                   @RequestParam(required = false, defaultValue = "5000") Long wait,
                                                                   @RequestParam(required = false, defaultValue = "false") boolean emptyReturn
    ) {
        return confServiceHandler.execute(async, confOwner, path, key, version, wait, emptyReturn);
    }


    /**
     * get the conf of the path
     *
     * @param confOwner
     * @param path
     * @param async
     * @return
     */
    @RequestMapping(value = "/{path:.+}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DeferredResult<ResponseEntity<List<ConfInfo>>> getPathConfigValue(
            String confOwner,
            @PathVariable("path") String path,
            @RequestParam(required = false, defaultValue = "false") boolean async,
            @RequestParam(required = false, defaultValue = "5000") Long wait,
            @RequestParam(required = false, defaultValue = "false") boolean emptyReturn
    ) {
        return confServiceHandler.execute(async, confOwner, path, wait, emptyReturn);
    }

    /**
     * get the conf of the path fuzzy
     *
     * @param confOwner
     * @param path
     * @param fuzzyKey
     * @return
     */
    @RequestMapping(value = "/fuzzy/{path}/{fuzzyKey}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<List<ConfInfo>> getPathFuzzyConfigValue(
            String confOwner,
            @PathVariable("path") String path,
            @PathVariable("fuzzyKey") String fuzzyKey
    ) {
        if (StringUtils.isEmpty(confOwner)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        if (StringUtils.isEmpty(path)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        if (StringUtils.isEmpty(fuzzyKey)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        try {
            return ResponseEntity.ok(confService.load(confOwner, path, fuzzyKey));
        } catch (Exception e) {
            logger.error("load path:[{}] fuzzyKey:[{}] the conf error:", path, fuzzyKey);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }


    }


    /**
     * <ul>
     * <li>version</li>
     * <li>no version</li>
     * </ul>
     * <p>
     * <br/>
     * no version is not thread safe
     *
     * @param path
     * @param confKey
     * @param version
     * @param verable
     * @param confOwner
     * @param confValue
     * @return
     */
    @RequestMapping(value = "/{path}/{key:.+}", method = {RequestMethod.PUT, RequestMethod.POST}, produces = "application/json")
    @ResponseBody
    public ResponseEntity<ConfInfo> addOrUpdateConf(
            @PathVariable("path") String path,
            @PathVariable("key") String confKey,
            @RequestParam(required = false) Integer version,
            @RequestParam(required = false, defaultValue = "true") boolean verable,
            String confOwner,
            String confValue
    ) {
        ConfInfo confInfo = new ConfInfo();
        confInfo.setConfKey(confKey);
        confInfo.setConfOwner(confOwner);
        confInfo.setPath(path);
        confInfo.setConfValue(confValue);
        if (version != null) {
            confInfo.setVersion(version);
        }
        if (check(path, confKey, confOwner, confInfo)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(confInfo);
        }

        confInfo.setConfValue(confValue);
        confInfo.setTimeStamp(new Date());
        //不启用version控制
        if (!verable) {
            ConfInfo existConf = confService.load(confOwner, path, confKey, -1);
            if (existConf != null) {
                //old version
                confInfo.setVersion(existConf.getVersion());
                confService.updateConfValue(confInfo, existConf.getVersion() + 1);
                confInfo.setConfValue(null);
                return ResponseEntity.ok(confInfo);
            }
        }

        if (version == null) {
            confInfo.setVersion(1);
        } else {
            confInfo.setVersion(++version);
        }
        if (confService.save(confInfo)) {
            //return null value
            confInfo.setConfValue(null);
            return ResponseEntity.ok(confInfo);
        } else {
            ConfInfo newest = confService.load(confOwner, path, confKey, -1);
            if (newest != null) {
                //return null value
                confInfo.setConfValue(String.format("%s,%s,version[%s] is exist", confInfo.getPath(), confInfo.getConfKey(), confInfo.getVersion()));
                //return the newest version
                confInfo.setVersion(newest.getVersion());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(confInfo);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(confInfo);
            }
        }

    }


    @RequestMapping(value = "/{path}/{key:.+}", method = {RequestMethod.DELETE}, produces = "application/json")
    @ResponseBody
    public ResponseEntity<ConfInfo> removeKeyConf(
            @PathVariable("path") String path,
            @PathVariable("key") String confKey,
            String confOwner
    ) {
        ConfInfo confInfo = new ConfInfo();
        confInfo.setPath(path);
        confInfo.setConfKey(confKey);
        confInfo.setConfOwner(confOwner);
        if (check(path, confKey, confOwner, confInfo)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(confInfo);
        }
        if (confService.remove(confOwner, path, confKey)) {
            confServiceHandler.publishDelConfEvent(confOwner, path, confKey, null);
            return ResponseEntity.ok(confInfo);
        } else {
            confInfo.setConfValue("remove key conf failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(confInfo);
        }
    }

    @RequestMapping(value = "/{path:.+}", method = {RequestMethod.DELETE}, produces = "application/json")
    @ResponseBody
    public ResponseEntity<ConfInfo> removePathConf(
            @PathVariable("path") String path,
            String confOwner
    ) {
        ConfInfo confInfo = new ConfInfo();
        confInfo.setPath(path);
        confInfo.setConfOwner(confOwner);
        if (check(path, confOwner, confInfo)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(confInfo);
        }
        boolean cached = cacheCascade.isCacheOpened();
        List<ConfInfo> confKeys = null;
        if (cached) {
            confKeys = confService.load(confOwner, path);
        }
        if (confService.remove(confOwner, path)) {
            if (cached) {
                cacheCascade.pathCacheEvict(confKeyCacheKeys(confKeys));
            }
            confServiceHandler.publishDelConfEvent(confOwner, path, null, null);
            return ResponseEntity.ok(confInfo);
        } else {
            confInfo.setConfValue("remove path conf failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(confInfo);
        }
    }

    private boolean check(String path,
                          String confKey,
                          String confOwner,
                          ConfInfo confInfo) {
        if (StringUtils.isEmpty(confOwner)) {
            confInfo.setConfValue("confOwner is empty");
            return true;
        }

        if (StringUtils.isEmpty(path)) {
            confInfo.setConfValue("path is empty");
            return true;
        }
        if (StringUtils.isEmpty(confKey)) {
            confInfo.setConfValue("confKey is empty");
            return true;
        }
        return false;
    }

    private boolean check(String path,
                          String confOwner,
                          ConfInfo confInfo) {
        if (StringUtils.isEmpty(confOwner)) {
            confInfo.setConfValue("confOwner is empty");
            return true;
        }

        if (StringUtils.isEmpty(path)) {
            confInfo.setConfValue("path is empty");
            return true;
        }
        return false;
    }


    private List<Object> confKeyCacheKeys(List<ConfInfo> confInfos) {
        if (confInfos != null) {
            return confInfos.stream().map(confInfo -> {
                List<String> list = new ArrayList<>();
                list.add(confInfo.getConfOwner());
                list.add(confInfo.getPath());
                list.add(confInfo.getConfKey());
                return list;
            }).collect(Collectors.toList());
        }
        return null;
    }
}
