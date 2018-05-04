package com.carl.auth.sso.client.config;

import com.carl.auth.sso.client.realm.CasRealm;
import io.buji.pac4j.filter.CallbackFilter;
import io.buji.pac4j.filter.SecurityFilter;
import io.buji.pac4j.subject.Pac4jSubjectFactory;
import io.buji.pac4j.filter.LogoutFilter;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SubjectFactory;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.client.rest.CasRestFormClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.cas.config.CasProtocol;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.ParameterClient;
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.jwt.profile.JwtGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.shiro.spring.web.config.AbstractShiroWebFilterConfiguration;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zms
 * @Description: shiro配置
 * @Date: Create On 2018/5/4 14:43
 */
@Configuration
public class shiroConfigApplicatioin extends  AbstractShiroWebFilterConfiguration {

    @Value("#{ @environment['cas.prefixUrl'] ?: null }")
    private String prefixUrl;//服务器地址
    @Value("#{ @environment['cas.loginUrl'] ?: null }")
    private String casLoginUrl;//登录地址
    @Value("#{ @environment['cas.callbackUrl'] ?: null }")
    private String callbackUrl;//登录后回调地址

    //jwt秘钥
    @Value("${jwt.salt}")
    private String salt;


    /**
     * @Author: zms
     *
     * @Description: 初始化clients
     *
     * @Date: 2018/5/4 15:13
     *
     */
    @Bean
    protected Clients clients() {
        //可以设置默认client
        Clients clients = new Clients();
        //支持的client全部设置进去
        clients.setClients(casClient(), casRestFormClient(), parameterClient());
        return clients;
    }

    /**
     * @Author:zms
     *
     * @Description:自定义实现Realm
     *
     * @Date:2018/5/4 16:28
     *
     */

    @Bean
    protected CasRealm casRealm(){
        CasRealm casRealm=new CasRealm();
        return casRealm;
    }


    /**
     * @Author: zms
     *
     * @Description: token校验器，可以用HeaderClient更安全
     *
     * @Date: 2018/5/4 15:16
     *
     */
    @Bean
    protected  ParameterClient parameterClient(){
        ParameterClient parameterClient=new ParameterClient("token",jwtAuthenticator());
        parameterClient.setSupportGetRequest(true);//支持get请求
        parameterClient.setSupportPostRequest(true);//支持post请求
        parameterClient.setName("jwt");//设置该client名称为jwt
        return parameterClient;
    }
    /**
     * @Author: zms
     *
     * @Description: 通过rest接口可以获取tgt，获取service ticket，甚至可以获取CasProfile
     *
     * @Date: 2018/5/4 15:08
     *
     */
    @Bean
    protected CasRestFormClient casRestFormClient() {
        CasRestFormClient casRestFormClient = new CasRestFormClient();
        casRestFormClient.setConfiguration(casConfiguration());
        casRestFormClient.setName("rest");
        return casRestFormClient;
    }


    /**
     * @Author: zms
     *
     * @Description: JWT校验器，
     * 也就是目前设置的ParameterClient进行的校验器，是rest/或者前后端分离的核心校验器
     *
     * @Date: 2018/5/4 15:48
     *
     */
    @Bean
    protected JwtAuthenticator jwtAuthenticator() {
        JwtAuthenticator jwtAuthenticator = new JwtAuthenticator();
        jwtAuthenticator.addSignatureConfiguration(new SecretSignatureConfiguration(salt));
        jwtAuthenticator.addEncryptionConfiguration(new SecretEncryptionConfiguration(salt));
        return jwtAuthenticator;
    }
    /**
     * @Author: zms
     *
     * @Description: Jwt token生成器 对CommonProfile生成然后每次请求都要携带
     *
     * @Date: 2018/5/4 15:47
     *
     */
    @Bean
    protected JwtGenerator jwtGenerator() {
        return new JwtGenerator(new SecretSignatureConfiguration(salt), new SecretEncryptionConfiguration(salt));
    }
    /**
     * @Author: zms
     *
     * @Description: CAS配置中心注册需要接入的Client
     *
     * @Date: 2018/5/4 15:06
     *
     */
    @Bean
    protected Config casConfig() {
        Config config = new Config();
        config.setClients(clients());
        return config;
    }

   /**
    * @Author: zms
    *
    * @Description: 设置CAS服务器登录地址 还有协议版本号
    *
    * @Date: 2018/5/4 15:07
    *
    */
    @Bean
    public CasConfiguration casConfiguration() {
        CasConfiguration casConfiguration = new CasConfiguration(casLoginUrl);
        casConfiguration.setProtocol(CasProtocol.CAS30);//指定CAS协议版本
        casConfiguration.setPrefixUrl(prefixUrl);//设置CAS服务器地址
        return casConfiguration;
    }

    /**
     * @Author: zms
     *
     * @Description: 设置CASClient回调地址 即验证成功后跳转的地址
     *
     * @Date: 2018/5/4 15:07
     *
     */
    @Bean
    public CasClient casClient() {
        CasClient casClient = new CasClient();
        casClient.setConfiguration(casConfiguration());
        casClient.setName("casClient");
        casClient.setCallbackUrl(callbackUrl);
        return casClient;
    }

    /**
     * @Author: zms
     *
     * @Description: cas核心过滤器，把支持的client写上，
     * filter过滤时才会处理，clients必须在casConfig.clients已经注册
     *
     * @Date: 2018/5/4 15:44
     *
     */
    @Bean
    public Filter casSecurityFilter() {
        SecurityFilter filter = new SecurityFilter();
        filter.setClients("casClient,rest,jwt");
        filter.setConfig(casConfig());
        return filter;
    }

    /**
     * @Author: zms
     *
     * @Description: 路径过滤设置
     *
     * @Date: 2018/5/4 15:58
     *
     */
    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition definition = new DefaultShiroFilterChainDefinition();
        definition.addPathDefinition("/callback", "callbackFilter");
        definition.addPathDefinition("/logout", "logoutFilter");
        definition.addPathDefinition("/**", "casSecurityFilter");
        return definition;
    }
    /**
     * @Author:zms
     *
     * @Description: 由于cas代理了用户，所以必须通过cas进行创建对象
     *
     * @Date:2018/5/4 15:59
     *
     */
    @Bean
    protected SubjectFactory subjectFactory() {
        return new Pac4jSubjectFactory();
    }

    /**
     * @Author:zms
     *
     * @Description: 对过滤器进行调整
     *
     * @Date:2018/5/4 16:09
     *
     */
    @Bean
    protected ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        //把subject对象设为subjectFactory
        ((DefaultSecurityManager) securityManager).setSubjectFactory(subjectFactory());
        ShiroFilterFactoryBean filterFactoryBean = super.shiroFilterFactoryBean();
        filterFactoryBean.setSecurityManager(securityManager);

        filterFactoryBean.setFilters(filters());
        return filterFactoryBean;
    }


    /**
     * @Author:zms
     *
     * @Description: 组装过滤器
     *  对shiro的过滤器进行明确组装
     * @Date:2018/5/4 16:06
     *
     */
    @Bean
    protected Map<String, Filter> filters() {
        //过滤器设置
        Map<String, Filter> filters = new HashMap<>();
        filters.put("casSecurityFilter", casSecurityFilter());
        CallbackFilter callbackFilter = new CallbackFilter();
        callbackFilter.setConfig(casConfig());
        filters.put("callbackFilter", callbackFilter);
        LogoutFilter logoutFilter = new LogoutFilter();
        logoutFilter.setConfig(casConfig());
        filters.put("logoutFilter", logoutFilter);
        return filters;
    }
}
