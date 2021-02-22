package com.atguigu.gmall.auth.service;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.UserException;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@EnableConfigurationProperties(JwtProperties.class)
@Service
public class AuthService {

    @Autowired
    private JwtProperties properties;

    @Autowired
    private GmallUmsClient umsClient;

    public void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1.校验用户名和密码是否正确：调用远程接口
            ResponseVo<UserEntity> userEntityResponseVo =
                    this.umsClient.queryUser(loginName, password);
            UserEntity userEntity = userEntityResponseVo.getData();

            // 2.判断用户信息是否为空
            if (userEntity == null) {
                throw new UserException("用户名或者密码错误！");
            }

            // 3.组织载荷
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userEntity.getId());
            map.put("userName", userEntity.getUsername());
            map.put("ip", IpUtils.getIpAddressAtService(request));

            // 4.生成jwt
            String token = JwtUtils.generateToken(map, this.properties.getPrivateKey(), this.properties.getExpire());

            // 5.需要把jwt放入cookie中
            CookieUtils.setCookie(request, response, this.properties.getCookieName(), token, this.properties.getExpire() * 60);

            // 6.为了方便展示用户的登录信息，需要写入unick
            CookieUtils.setCookie(request, response, this.properties.getUnick(), userEntity.getNickname(), this.properties.getExpire() * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
