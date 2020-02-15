package com.leyou.page.web;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

    @Autowired
    private PageService pageService;
    //转到商品详情
    @GetMapping("item/{id}.html")
    public String toItemPage(Model model,@PathVariable("id") Long spuId){
        model.addAllAttributes(pageService.loadModel(spuId));
        return "item";
    }
}
