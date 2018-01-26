package com.jiaxy.conf.server.dao;

import com.jiaxy.conf.server.domain.ConfInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public interface ConfInfoDAO {

    void insert(ConfInfo confInfo);

    ConfInfo getByKey(@Param("confOwner") String confOwner,
                      @Param("path") String path,
                      @Param("confKey") String confKey,
                      @Param("version") Integer version
    );

    List<ConfInfo> getByPath(@Param("confOwner") String confOwner,
                             @Param("path") String path
    );

    List<ConfInfo> getByPathWithFuzzyKey(@Param("confOwner") String confOwner,
                                         @Param("path") String path,
                                         @Param("fuzzyKey") String fuzzyKey
    );

    int delete(@Param("confOwner") String confOwner,
               @Param("path") String path,
               @Param("confKey") String confKey);

    int deletePath(@Param("confOwner") String confOwner,
                   @Param("path") String path
    );

    int updatePath(@Param("path") String path);

    int updateConfValue(@Param("path") String path,
                        @Param("confKey") String confKey,
                        @Param("confValue") String confValue,
                        @Param("currentVersion") Integer currentVersion,
                        @Param("newVersion") Integer newVersion,
                        @Param("timeStamp") Date timeStamp);
}
