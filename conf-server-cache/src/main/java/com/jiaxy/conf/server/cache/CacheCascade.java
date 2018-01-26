package com.jiaxy.conf.server.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;


@Component
public class CacheCascade {

    @Resource
    private CacheManager cacheManager;


    /**
     * remove the cache under the conf path.used for conf path deleted
     *
     * @param confCacheKeys
     */
    public void pathCacheEvict(List<Object> confCacheKeys) {
        if (cacheManager == null || confCacheKeys == null || confCacheKeys.isEmpty()) {
            return;
        }
        Collection<String> cacheNames = cacheManager.getCacheNames();
        if (cacheNames != null && !cacheNames.isEmpty()) {
            for (String cacheName : cacheNames) {
                if ("default".equals(cacheName)) {
                    continue;
                }
                Cache cache = cacheManager.getCache(cacheName);
                for (Object cacheKey : confCacheKeys) {
                    cache.evict(cacheKey);
                }
            }
        }
    }


    public boolean isCacheOpened() {
        if (cacheManager instanceof NoOpCacheManager) {
            return false;
        }
        return true;
    }
}
