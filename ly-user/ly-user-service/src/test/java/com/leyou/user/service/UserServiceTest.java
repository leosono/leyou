package com.leyou.user.service;

import org.junit.Test;
import org.springframework.util.DigestUtils;

import java.io.UnsupportedEncodingException;

public class UserServiceTest {

    @Test
    public void queryUserByUsernameAndPwd() throws UnsupportedEncodingException {
        System.out.println(DigestUtils.md5Digest("leoso".getBytes("UTF-8")));
        System.out.println(DigestUtils.md5Digest("leoso".getBytes("UTF-8")));
        System.out.println(DigestUtils.md5Digest("leoso".getBytes("UTF-8")));
        System.out.println(DigestUtils.md5DigestAsHex("leoso".getBytes()));
        System.out.println(DigestUtils.md5DigestAsHex("leoso".getBytes()));
        System.out.println(DigestUtils.md5DigestAsHex("leoso".getBytes()));
    }
}