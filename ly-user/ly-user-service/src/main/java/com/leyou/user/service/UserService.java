package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    private static String KEY_PREFIX = "user:verify:phone:";
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //校验用户数据 (用户名,手机号)
    public Boolean verifyUser(String data, Integer type) {
        User user = new User();
        //使用switch,相比if else 可以处理第三种情况
        switch (type) {
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }
        return userMapper.selectCount(user) == 0;
    }

    //发送短信
    public void sendCode(String phone) {
        Map<String,String> map = new HashMap<>();
        String code = NumberUtils.generateCode(6);
        map.put("phone",phone);
        map.put("code",code);
        //设置验证码5分钟失效
        stringRedisTemplate.opsForValue().set(KEY_PREFIX+phone,code,5, TimeUnit.MINUTES);
        amqpTemplate.convertAndSend("ly.sms.exchange","sms.verifycode",map);
    }

    public void register(User user, String code) {
        //校验验证码
        String codeCache = stringRedisTemplate.opsForValue().get(KEY_PREFIX+user.getPhone());
        if(!code.equals(codeCache)){
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        //加密密码
        String salt = NumberUtils.generateCode(4);
        user.setSalt(salt);
        user.setPassword(salt+ DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        user.setCreated(new Date());
        int count = userMapper.insert(user);
        if(count==1){
            stringRedisTemplate.delete(KEY_PREFIX+user.getPhone());
        }
    }

    public User queryUserByUsernameAndPwd(String username,String password) {
        User record = new User();
        record.setUsername(username);
        User user = userMapper.selectOne(record);
        if(user == null){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        if(!user.getPassword().equals(user.getSalt()+DigestUtils.md5DigestAsHex(password.getBytes()))){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        return user;
    }
}
