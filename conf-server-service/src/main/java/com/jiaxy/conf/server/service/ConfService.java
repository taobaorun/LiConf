package com.jiaxy.conf.server.service;

import com.jiaxy.conf.server.domain.ConfInfo;

import java.util.List;

/**
 * Description: <br/>
 * <p/>
 * Company:
 *
 * @author: <a href=mailto:taobaorun@gmail.com>tom</a>
 * <br/>
 * @Date: 2017/04/17 17:14
 */
public interface ConfService {

    /**
     * save conf
     *
     * @param confInfo
     */
    boolean save(ConfInfo confInfo);


    ConfInfo load(String confOwner, String path, String key, Integer version);

    /**
     *
     * @param confOwner
     * @param path
     * @return the conf of the path
     */
    List<ConfInfo> load(String confOwner, String path);


    /**
     * get the fuzzy key conf of the path
     *
     * @param confOwner
     * @param path
     * @param fuzzyKey
     * @return
     */
    List<ConfInfo> load(String confOwner, String path,String fuzzyKey);


    /**
     * @param confOwner
     * @param path
     * @param key
     */
    boolean remove(String confOwner, String path, String key);


    boolean remove(String confOwner, String path);

    /**
     * @param confOwner
     * @param path
     */
    boolean updatePath(String confOwner, String path);

    /**
     *
     * @param confInfo
     * @param newVersion
     * @return
     */
    boolean updateConfValue(ConfInfo confInfo,
                            Integer newVersion);
}
