package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.service.GoodsService;
import com.leyou.item.pojo.Spu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author leoso
 * @create 2020-01-06 12:47
 */
@RestController
@RequestMapping("/spu")
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    @GetMapping("/page")
    public ResponseEntity<PageResult<Spu>> querySpuByPage(
            @RequestParam(value="page",defaultValue="1") Integer page,
            @RequestParam(value="rows",defaultValue = "5") Integer rowsPerPage,
            @RequestParam(value="key",required = false) String key,
            @RequestParam(value = "saleable",required = false)Boolean saleable
    ){
        PageResult<Spu> pageResult = goodsService.querySpuByPage(page,rowsPerPage,key,saleable);
        return ResponseEntity.ok(pageResult);
    }
}
