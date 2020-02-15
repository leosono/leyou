package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author leoso
 * @create 2020-01-06 12:47
 */
@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<Spu>> querySpuByPage(
            @RequestParam(value="page",defaultValue="1") Integer page,
            @RequestParam(value="rows",defaultValue = "5") Integer rowsPerPage,
            @RequestParam(value="key",required = false) String key,
            @RequestParam(value = "saleable",required = false)Boolean saleable
    ){
        PageResult<Spu> pageResult = goodsService.querySpuByPage(page,rowsPerPage,key,saleable);
        return ResponseEntity.ok(pageResult);
    }

    @PostMapping("/goods")
    public ResponseEntity<Void> saveGoods(@RequestBody Spu spu){
       goodsService.saveGoods(spu);
       return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //编辑商品的数据回显
    @GetMapping("/spu/detail/{id}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("id") Long id){
        return ResponseEntity.ok(goodsService.querySpuDetailBySpuId(id));
    }

    @GetMapping("/sku/list")
    public ResponseEntity<List<Sku>> querySkusBySpuId(@RequestParam("id") Long id){
        return ResponseEntity.ok(goodsService.querySkusBySpuId(id));
    }

    @PutMapping("/goods")
    public ResponseEntity<Void> updateGoods(@RequestBody Spu spu){
        goodsService.updateGoods(spu);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //根据spuId获取Spu
    @GetMapping("/id")
    public ResponseEntity<Spu> querySpuById(@RequestParam("id") Long id){
        return ResponseEntity.ok(goodsService.querySpuById(id));
    }
}
