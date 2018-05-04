/*
 * 版权所有.(c)2008-2017. 卡尔科技工作室
 */

package com.carl.auth.sso.client.demo;

import org.jasig.cas.client.authentication.UrlPatternMatcherStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author Carl
 * @date 2017/9/28
 * @since 1.5.0
 */
@SpringBootApplication
@EnableWebMvc
public class SimpleUrlPatternMatcherStrategy implements UrlPatternMatcherStrategy {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @Author:zms
     *
     * @Description:返回true代表不拦截此路径
     * 可以根据services判断 该路径是否应该拦截
     * true不拦截 false拦截
     * @Date:2018/5/3 19:06
     *
     */
    @Override
    public boolean matches(String url) {
        logger.debug("访问路径：" + url);
        return url.contains("zhangsan.jsp");
    }

    @Override
    public void setPattern(String pattern) {

    }
}
