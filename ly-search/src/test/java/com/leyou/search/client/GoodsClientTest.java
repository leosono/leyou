package com.leyou.search.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.GoodsRepository;
import com.leyou.search.service.GoodsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GoodsClientTest {
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private GoodsService goodsService;

    //创建索引，添加映射
    @Test
    public void createIndex(){
        elasticsearchTemplate.createIndex(Goods.class);
        elasticsearchTemplate.putMapping(Goods.class);
    }

    //导入数据
    @Test
    public void loadData(){
        int page = 1;
        int rowsPerPage = 100;
        int size = 0;

        do{
            //查到所有spu
            PageResult<Spu> spuPageResult = goodsClient.querySpuByPage(page, rowsPerPage, null, true);
            List<Spu> spuList = spuPageResult.getItems();
            if(CollectionUtils.isEmpty(spuList)){
                break;
            }
            size = spuList.size();
            //把查询到的spuList转换为Goods
            List<Goods> goodsList = spuList.stream().map(goodsService::buildGoods).collect(Collectors.toList());
            //存入索引库
            goodsRepository.saveAll(goodsList);
            //翻页
            page++;
        }while(size==100);

    }
}