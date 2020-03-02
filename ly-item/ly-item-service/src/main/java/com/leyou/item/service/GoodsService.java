package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author leoso
 * @create 2020-01-06 12:47
 */
@Service
@Slf4j
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Transactional
    public PageResult<Spu> querySpuByPage(Integer page, Integer rowsPerPage, String key, Boolean saleable) {
        //分页
        PageHelper.startPage(page, rowsPerPage);
        //过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("title", "%"+key+"%");
        }
        if(saleable!=null){
            criteria.andEqualTo("saleable", saleable);
        }
        //默认排序
        example.setOrderByClause("last_update_time DESC");
        //查询
        List<Spu> list = spuMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        loadCategoryAndBrandName(list);
        PageInfo<Spu> pageInfo = new PageInfo<>(list);
        return new PageResult<Spu>(pageInfo.getTotal(), list);
    }

    public void loadCategoryAndBrandName(List<Spu> list){
        for(Spu spu : list){
            //获取分类名称
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names,"/"));
            //获取品牌
            Brand brand = brandService.queryById(spu.getBrandId());
            spu.setBname(brand.getName());
        }
    }

    @Transactional
    public void saveGoods(Spu spu) {
        //保存 spu
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);
        int count = spuMapper.insert(spu);
        if(count!=1){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //保存 spu详情
        spu.getSpuDetail().setSpuId(spu.getId());
        count = spuDetailMapper.insert(spu.getSpuDetail());
        if(count!=1){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //保存sku和stock
        saveSkuAndStock(spu.getSkus(),spu.getId());

        //通知其他微服务
        sendMessage("insert",spu.getId());
    }

    @Transactional
    public void saveSkuAndStock(List<Sku> skus,Long spuId){
        List<Stock> stockList = new ArrayList<>();
        for(Sku sku : skus){
            sku.setSpuId(spuId);
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            int count = skuMapper.insert(sku);
            if(count!=1){
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockList.add(stock);
        }
        //批量添加
        int count = stockMapper.insertList(stockList);
        if(count!=stockList.size()){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }

    public SpuDetail querySpuDetailBySpuId(Long id) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(id);
        if(spuDetail == null){
            throw new LyException(ExceptionEnum.GOODS_SPUDETAIL_NOT_FOUND);
        }
        return spuDetail;
    }

    public List<Sku> querySkusBySpuId(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skuList = skuMapper.select(sku);
        if(CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }

        //查询库存
        //stockMapper根据skuId获取stock对象，之后把stock中的 Integer stock取出传给sku
        /*for(Sku s : skuList){
            Stock stock = stockMapper.selectByPrimaryKey(s.getId());
            if(stock ==null){
                throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
            }
            s.setStock(stock.getStock());
        }*/

        //遍历一次 stockMapper查询一次数据库 所以使用流来改进
        List<Long> skuIds = skuList.stream().map(Sku::getId).collect(Collectors.toList());
        List<Stock> stockList = stockMapper.selectByIdList(skuIds);
        if(CollectionUtils.isEmpty(stockList)){
            throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
        }
        //已经获得了stock对象的集合，现在需要获取stock对象中的stock，赋值给每个sku
        //我们把stock变成一个map，其key是sku_id,值是库存值
        Map<Long,Integer> stockMap = stockList.stream().collect(Collectors.toMap(Stock::getSkuId,Stock::getStock));
        skuList.forEach(s -> s.setStock(stockMap.get(s.getId())));
        return skuList;
    }

    @Transactional
    public void updateGoods(Spu spu) {
        List<Sku> skuList = querySkusBySpuId(spu.getId());
        if(!CollectionUtils.isEmpty(skuList)){
            //删除stock
            List<Long> skuIds = skuList.stream().map(s -> s.getId()).collect(Collectors.toList());
            stockMapper.deleteByIdList(skuIds);
            /*Example example = new Example(Stock.class);
            example.createCriteria().andIn("skuId", skuIds);
            this.stockMapper.deleteByExample(example);*/
            //删除sku
            Sku sku = new Sku();
            sku.setSpuId(spu.getId());
            skuMapper.delete(sku);
        }
        //新增sku与stock
        saveSkuAndStock(spu.getSkus(),spu.getId());
        //更新spu
        //防止乱修改，设为null，这样就继承原先的值
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        spu.setValid(null);
        spu.setSaleable(null);
        spuMapper.updateByPrimaryKeySelective(spu);
        //更新spuDetail
        spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());

        //发送消息
        sendMessage("update",spu.getId());
    }

    public Spu querySpuById(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(spu==null){
            throw new LyException(ExceptionEnum.GOODS_SPU_NOT_FOUND);
        }
        List<Sku> skus = querySkusBySpuId(id);
        spu.setSkus(skus);
        SpuDetail spuDetail = querySpuDetailBySpuId(id);
        spu.setSpuDetail(spuDetail);
        return spu;
    }

    public Sku querySkuById(Long id){
        Sku sku = skuMapper.selectByPrimaryKey(id);
        if(sku==null){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        return sku;
    }

    //减库存
    public void decreaseStock(Long skuId, Integer num) {
       /* //会出现高并发下的线程安全问题
        Stock stock = stockMapper.selectByPrimaryKey(skuId);
        int stockCount = stock.getStock();
        if(stockCount>=num){
            Example example = new Example(Stock.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("sku_id",skuId);
            Stock stock1 = new Stock();
            stock1.setStock(stockCount-num);
            stockMapper.updateByExampleSelective(stock1,example);
        }*/

        int count = stockMapper.decreaseStock(skuId, num);
        if(count!=1){
            throw new LyException(ExceptionEnum.STOCK_UPDATE_ERROR);
        }
    }

    //发送消息
    public void sendMessage(String type,Long id){
        try{
            amqpTemplate.convertAndSend("item."+type, id);
        }catch(Exception e){
            log.error("{} 商品消息发送异常,商品id {}",type,id,e);
        }
    }
}
