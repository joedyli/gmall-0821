package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
	private static final String pubKeyPath = "D:\\project-0821\\rsa\\rsa.pub";
    private static final String priKeyPath = "D:\\project-0821\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 2);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2MTM5NTkzMDZ9.g1M3KvaZsPg0fnpBp71yrCVgKQEkmvSRA7rHjXeuAPuGTOOGLgRubXDaC3HdDYkifEOlQSk6kRuqLjkoGExxPYQoxzgKuZo8C6F1vrDGTDFJNVkhXT47K9AoPi7wLxpKfu3csTWHzR8y6hpDbXFdXM60aab5pof1lIYVlvNB9n1Junv60rvP94keNJOO8cYRvrBCfM4tyV7CYyODnlgWGtwK0USCw078eKtq_CAj656HCyFgJU_PyWGBkMzjqUxkge7i5sBZdfn2Pe5lI1dHUdM6GtB1ljfBTpNadcp7zadD8lafLtrcobMWnbbjFva5SVmYlEujpQiItSDkdDuj7Q";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}
