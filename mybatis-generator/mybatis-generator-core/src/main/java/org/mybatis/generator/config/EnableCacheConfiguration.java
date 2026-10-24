package org.mybatis.generator.config;

import java.util.ArrayList;
import java.util.List;

/**
 * 缓存注解配置
 *
 */
public class EnableCacheConfiguration extends PropertyHolder{

    private boolean enableCache = true;
    private List<String> cacheNames = new ArrayList<>();

    public EnableCacheConfiguration() {
        super();
    }

    public boolean isEnableCache() {
        return enableCache;
    }

    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }

    public List<String> getCacheNames() {
        return cacheNames;
    }

    public void setCacheNames(List<String> cacheNames) {
        this.cacheNames = cacheNames;
    }
}
