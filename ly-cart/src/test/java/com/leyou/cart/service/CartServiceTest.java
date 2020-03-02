package com.leyou.cart.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
/*@TestPropertySource("classpath:application.yaml")*/
public class CartServiceTest {

   @Autowired
   private StringRedisTemplate stringRedisTemplate;

   @Test
   public void delete(){
       stringRedisTemplate.delete("33");
   }
}