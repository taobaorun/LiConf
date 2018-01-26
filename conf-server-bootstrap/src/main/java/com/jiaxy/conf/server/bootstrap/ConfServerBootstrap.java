package com.jiaxy.conf.server.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ImportResource;

/**
 * Description: <br/>
 * <p/>
 * Company:
 *
 * @author: <a href=mailto:taobaorun@gmail.com>tom</a>
 * <br/>
 * @Date: 2017/04/14 17:19
 */
@SpringBootApplication(
        scanBasePackages = "com.jiaxy.conf.server"
)
@EnableCaching
@ImportResource("classpath*:spring/spring-db.xml")
public class ConfServerBootstrap extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ConfServerBootstrap.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ConfServerBootstrap.class);
    }
}
