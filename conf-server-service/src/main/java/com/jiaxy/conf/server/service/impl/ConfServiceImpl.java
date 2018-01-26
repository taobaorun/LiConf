package com.jiaxy.conf.server.service.impl;

import com.jiaxy.conf.server.dao.ConfInfoDAO;
import com.jiaxy.conf.server.domain.ConfInfo;
import com.jiaxy.conf.server.service.ConfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Description: <br/>
 * <p/>
 * Company:
 *
 * @author: <a href=mailto:taobaorun@gmail.com>tom</a>
 * <br/>
 * @Date: 2017/04/17 17:17
 */
@Service
public class ConfServiceImpl implements ConfService {

    private static final Logger logger = LoggerFactory.getLogger(ConfServiceImpl.class);

    @Resource
    private ConfInfoDAO confInfoDAO;


    /**
     * save conf
     *
     * @param confInfo
     */
    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "conf", key = "{#confInfo.confOwner,#confInfo.path}"),
            @CacheEvict(cacheNames = "conf", key = "{#confInfo.confOwner,#confInfo.path,#confInfo.confKey}")
    })
    public boolean save(ConfInfo confInfo) {
        try {
            confInfoDAO.insert(confInfo);
        } catch (Throwable throwable) {
            if (throwable instanceof DuplicateKeyException) {
                logger.warn("the version[{}] of key[{}] path[{}] is duplicate",
                        confInfo.getVersion(),
                        confInfo.getConfKey(),
                        confInfo.getPath());
                return false;
            }
            logger.error("save conf error:", throwable);
            return false;
        }
        return true;
    }

    @Override
    @Cacheable(cacheNames = "conf", key = "{#confOwner,#path,#key}", condition = "{#version == null}")
    public ConfInfo load(String confOwner, String path, String key, Integer version) {
        return confInfoDAO.getByKey(confOwner, path, key, version);
    }

    @Override
    @Cacheable(cacheNames = "conf", key = "{#confOwner,#path}")
    public List<ConfInfo> load(String confOwner, String path) {
        return confInfoDAO.getByPath(confOwner, path);
    }

    @Override
    public List<ConfInfo> load(String confOwner, String path, String fuzzyKey) {
        return confInfoDAO.getByPathWithFuzzyKey(confOwner, path, fuzzyKey);
    }

    /**
     * @param confOwner
     * @param path
     * @param key
     */
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "conf", key = "{#confOwner,#path}"),
                    @CacheEvict(cacheNames = "conf", key = "{#confOwner,#path,#key}")
            }
    )
    @Override
    public boolean remove(String confOwner, String path, String key) {
        try {
            int count = confInfoDAO.delete(confOwner, path, key);
            if (count == 0) {
                return false;
            }
        } catch (Throwable throwable) {
            logger.error("remove {},{},{} conf error:", confOwner, path, key, throwable);
            return false;
        }
        return true;
    }


    @Override
    @CacheEvict(cacheNames = "conf", key = "{#confOwner,#path}")
    public boolean remove(String confOwner, String path) {
        try {
            int count = confInfoDAO.deletePath(confOwner, path);
            if (count == 0) {
                return false;
            }
        } catch (Throwable throwable) {
            logger.error("remove {},{} conf error:", confOwner, path, throwable);
            return false;
        }
        return true;
    }

    /**
     * @param confOwner
     * @param path
     */
    @Override
    public boolean updatePath(String confOwner, String path) {
        try {
            int count = confInfoDAO.updatePath(path);
            if (count == 0) {
                return false;
            }
        } catch (Throwable throwable) {
            logger.error("update path :{} error", path, throwable);
            return false;
        }
        return true;
    }

    /**
     * @param newVersion next version
     */
    @Override
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "conf", key = "{#confInfo.confOwner,#confInfo.path}"),
                    @CacheEvict(cacheNames = "conf", key = "{#confInfo.confOwner,#confInfo.path,#confInfo.confKey}")
            }
    )
    public boolean updateConfValue(ConfInfo confInfo,
                                   Integer newVersion) {
        try {
            int count = confInfoDAO.updateConfValue(confInfo.getPath(),
                    confInfo.getConfKey(),
                    confInfo.getConfValue(),
                    confInfo.getVersion(),
                    newVersion,
                    confInfo.getTimeStamp());
            if (count == 0) {
                return false;
            }
        } catch (Throwable throwable) {
            logger.error("update {},{},{} conf value error:", confInfo.getConfOwner(), confInfo.getPath(), confInfo.getConfKey(), throwable);
            return false;
        }
        return true;
    }
}
