package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author leoso
 * @create 2019-12-20 10:11
 */

@RestController
@RequestMapping("/category")
public class CategoryContoller {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoryListByPid(@RequestParam("pid") Long pid){
        return ResponseEntity.ok(categoryService.queryCategoryListByPid(pid));
    }

    @GetMapping("/bid/{bid}")
    public ResponseEntity<List<Category>> queryByBrandId(@PathVariable("bid") Long bid){
        List<Category> categories = categoryService.queryByBrandId(bid);
        return ResponseEntity.ok(categories);
    }

    //搜索服务_根据cids查询分类名称
    @GetMapping("/list/ids")
    public ResponseEntity<List<Category>> queryCategoryByIds(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.ok(categoryService.queryByIds(ids));
    }
}
