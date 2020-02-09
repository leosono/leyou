package com.leyou.search.pojo;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SearchResult extends PageResult<Goods>{
    private List<Category> categoryList;
    private List<Brand> brandList;
    private List<Map<String,Object>> specs;

    public SearchResult(){

    }
    //alt+insert
    public SearchResult(Long total, Long totalPage, List<Goods> items, List<Category> categoryList, List<Brand> brandList, List<Map<String, Object>> specs) {
        super(total, totalPage, items);
        this.categoryList = categoryList;
        this.brandList = brandList;
        this.specs = specs;
    }
}
