package com.atguigu.gmall.auth.config;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String pubKeyPath;
    private String priKeyPath;
    private String secret;
    private Integer expire;
    private String cookieName;
    private String unick;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    /**
     * 读取公钥和私钥赋值给公钥和私钥对象
     */
    @PostConstruct
    public void init(){
        try {
            File pubFile = new File(pubKeyPath);
            File priFile = new File(priKeyPath);
            if (!pubFile.exists() || !priFile.exists()){
                RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
            }
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
            this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
