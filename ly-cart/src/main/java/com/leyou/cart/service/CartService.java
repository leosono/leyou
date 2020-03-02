package com.leyou.cart.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "cart:uid:";

    public void addCart(Cart cart){
        //获取用户信息
        UserInfo userInfo = UserInterceptor.getUser();
        String key = KEY_PREFIX+userInfo.getId();
        String hashKey = cart.getSkuId().toString();
        //新传过来的num
        Integer num = cart.getNum();
        BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(key);
        if(operations.hasKey(hashKey)){
            String cartJson = operations.get(hashKey).toString();
            //取出的旧cart覆盖了新传过来的的cart
            cart = JsonUtils.parse(cartJson, Cart.class);
            cart.setNum(cart.getNum()+num);
        }
        operations.put(hashKey, JsonUtils.serialize(cart));
    }

    public void addCartList(List<Cart> cartList) {
        //获取用户信息
        UserInfo userInfo = UserInterceptor.getUser();
        String key = KEY_PREFIX+userInfo.getId();
        BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(key);
        for (Cart cart : cartList) {
            String hashKey = cart.getSkuId().toString();
            Integer num = cart.getNum();
            if(operations.hasKey(hashKey)){
                String cartJson = operations.get(hashKey).toString();
                //取出的旧cart覆盖了新传过来的的cart
                cart = JsonUtils.parse(cartJson, Cart.class);
                cart.setNum(cart.getNum()+num);
            }
            operations.put(hashKey, JsonUtils.serialize(cart));
        }
    }

    public List<Cart> listCart() {
        String key = KEY_PREFIX + UserInterceptor.getUser().getId();
        if(!stringRedisTemplate.hasKey(key)){
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(key);
        List<Object> values = operations.values();
        List<Cart> cartList = values.stream().map(v -> JsonUtils.parse(v.toString(), Cart.class))
                .collect(Collectors.toList());
        return cartList;
    }

    public void updateCartNum(Long skuId, Integer num) {
        String key = KEY_PREFIX + UserInterceptor.getUser().getId();
        String hashkey = skuId.toString();
        BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(key);
        if(!operations.hasKey(hashkey)){
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        String jsonCart = operations.get(hashkey).toString();
        Cart cart = JsonUtils.parse(jsonCart, Cart.class);
        cart.setNum(num);
        operations.put(hashkey, JsonUtils.serialize(cart));
    }

    public void deleteCart(Long skuId) {
        String key = KEY_PREFIX + UserInterceptor.getUser().getId();
        String hashkey = skuId.toString();
        BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(key);
        if(!operations.hasKey(hashkey)){
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        operations.delete(hashkey);
    }
}
