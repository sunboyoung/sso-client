package com.carl.auth.shiro.client.demo.realm;

import com.carl.auth.shiro.client.demo.model.User;
import com.carl.auth.shiro.client.demo.util.JsonUtils;
import com.carl.auth.shiro.client.demo.util.LoggerUtils;
import io.buji.pac4j.realm.Pac4jRealm;
import io.buji.pac4j.subject.Pac4jPrincipal;
import io.buji.pac4j.token.Pac4jToken;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.pac4j.core.profile.CommonProfile;

import java.util.*;

/**
 * @Author:zms
 * @Description:
 * @Date:Create On 2018/5/4 17:15
 */
public class shiroRealm extends Pac4jRealm {

    @Override
    public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        System.out.println("登录验证");
        final Pac4jToken token = (Pac4jToken) authenticationToken;
        final LinkedHashMap<String, CommonProfile> profiles = token.getProfiles();
        final Pac4jPrincipal principal = new Pac4jPrincipal(profiles);
        User user = new User();
        for (Map.Entry<String, CommonProfile> entry : profiles.entrySet()) {
            String name = entry.getKey();
            LoggerUtils.fmtDebug(this.getClass(), "CasRealm 接受到 client名字为:%s", name);
            CommonProfile profile = entry.getValue();
            Map<String, Object> attributeMap = profile.getAttributes();
            String attributeJSON = JsonUtils.objToJson(attributeMap);
            user = JsonUtils.jsonToObjUseGson(attributeJSON, User.class);
        }
        return new SimpleAuthenticationInfo(user,profiles.hashCode(),getName());
    }
    
    
    /**
     * @Author:zms
     *
     * @Description: 注释中的方法为pa4j封装的用户信息principal的获取
     * cas登录成功后将返回的用户信息存放在principals中
     * 不过我们是自定义授权 与service层结合 这里不使用
     *
     * @Date:2018/5/7 15:45
     *
     */
    @Override
    public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Set<String> roles = new HashSet();
        roles.add("zms");
//        Pac4jPrincipal principal = principals.oneByType(Pac4jPrincipal.class);
//        if (principal != null) {
//            List<CommonProfile> profiles = principal.getProfiles();
//            Iterator var6 = profiles.iterator();
//            while(var6.hasNext()) {
//                CommonProfile profile = (CommonProfile)var6.next();
//                if (profile != null) {
//                    roles.addAll(profile.getRoles());
//                }
//            }
//        }
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        authorizationInfo.addRoles(roles);
        System.out.println("授权信息完毕");
        return authorizationInfo;
    }
}
