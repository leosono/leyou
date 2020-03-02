package com.leyou.cart.web;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {
        cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<Cart>> listCart() {
        return ResponseEntity.ok(cartService.listCart());
    }

    //合并购物车
    @PostMapping("/merge")
    public ResponseEntity<List<Cart>> mergelistCart(@RequestBody List<Cart> cart) {
        cartService.addCartList(cart);
        List<Cart> cartList = cartService.listCart();
        return ResponseEntity.ok(cartList);
    }

    @PutMapping
    public ResponseEntity<Void> updateCartNum(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num){
        cartService.updateCartNum(skuId,num);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId") Long skuId){
        cartService.deleteCart(skuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}