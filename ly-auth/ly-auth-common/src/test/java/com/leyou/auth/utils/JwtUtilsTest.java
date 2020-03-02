package com.leyou.auth.utils;

import com.leyou.auth.entity.UserInfo;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtUtilsTest {
    private static final String pubKeyPath = "D:\\key\\rsa\\rsa.pub";

    private static final String priKeyPath = "D:\\key\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU4MjY5NDAzOX0.U8r7BODWMltXdnRkoCpupwIzbrMM4I5DlLj9jZuHPYOA_31xiMgUS9r_STGwxMr2kHloOxJqUa9YdHYs9BH6YCaEwcxAhnE00zLwFiVPzaI8drE5_a74MXx6_YuwPxUndtqisUXUZJ6aKgNRnsiNHwDbnCsaJgnHD09Lh_hsQZk";
        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}