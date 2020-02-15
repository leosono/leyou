package com.leyou.page.service;

import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.Spu;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PageService {

    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private TemplateEngine templateEngine;

    public Map<String,Object> loadModel(Long spuId) {
        Map<String,Object> map = new HashMap<>();
        Spu spu = goodsClient.querySpuById(spuId);
        List<Category> categories = categoryClient.queryCategoryByIds
                (Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        List<SpecGroup> specs = specificationClient.querySpecificationList(spu.getCid3());
        map.put("title",spu.getTitle());
        map.put("subTitle",spu.getSubTitle());
        map.put("skus", spu.getSkus());
        map.put("detail",spu.getSpuDetail());
        map.put("categories",categories);
        map.put("brand",brand);
        map.put("specs",specs);
        return map;
    }

    //创建静态页面
    public void createHtml(Long spuId){
        Context context = new Context();
        context.setVariables(loadModel(spuId));
        File destFile = new File("D:\\upload",spuId+".html");
        if(destFile.exists()){
            destFile.delete();
        }
        try( PrintWriter writer = new PrintWriter(destFile,"UTF-8")){
            templateEngine.process("item", context, writer);
        }catch(Exception e){
            log.error("[页面静态化] 失败",e,spuId);
        }
    }
}
