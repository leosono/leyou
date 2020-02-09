package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author leoso
 * @create 2019-12-28 20:02
 */
@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPage(Integer page,String key,Integer rowsPerPage,String sortBy,Boolean descending) {
        //分页
        PageHelper.startPage(page, rowsPerPage);
        //过滤搜索框 底层使用MyBatis拦截器
        Example example = new Example(Brand.class);
        if(StringUtils.isNotBlank(key)){
            example.createCriteria().orLike("name", "%"+key+"%")
                    .orEqualTo("letter",key.toUpperCase());
        }
        //排序
        if(StringUtils.isNotBlank(sortBy)){
            String orderByClause = sortBy+(descending?" DESC":" ASC");
            example.setOrderByClause(orderByClause);
        }
        //查询
        List<Brand> list = brandMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //PageInfo对Page<E>结果进行包装
        PageInfo<Brand> info = new PageInfo<>(list);
        return new PageResult<Brand>(info.getTotal(), list);
    }

    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        //添加品牌
        brand.setId(null);
        int count = brandMapper.insert(brand);
        if(count!=1){
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
        //添加中间表
        for(Long cid:cids){
            count = brandMapper.insertCategoryBrand(cid,brand.getId());
            if(count!=1){
                throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }
    }

    @Transactional
    public void updateBrand(Brand brand, List<Long> cids){
        //如果相应的为null不修改
        int count = brandMapper.updateByPrimaryKeySelective(brand);
        if(count!=1){
            throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROR);
        }
        //修改中间表
        for(Long cid : cids){
            count = brandMapper.updateCategoryBrand(cid,brand.getId());
            System.out.println(cid+"-------------"+brand.getId());
            if(count!=1){
                throw new LyException(ExceptionEnum.BRAND_UPDATE_ERROR);
            }
        }
    }

    public void deleteBrand(Long bid) {
        int count = brandMapper.deleteByPrimaryKey(bid);
        if(count!=1){
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROR);
        }
        //删除中间表
        count = brandMapper.deleteCategoryBrand(bid);
        if(count!=1){
            throw new LyException(ExceptionEnum.BRAND_DELETE_ERROR);
        }
    }

    public Brand queryById(Long id) {
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if(brand==null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    public List<Brand> queryBrandByCid(Long cid) {
        return brandMapper.queryBrandByCid(cid);
    }

    public List<Brand> queryBrandByIds(List<Long> ids) {
        return brandMapper.selectByIdList(ids);
    }
}
