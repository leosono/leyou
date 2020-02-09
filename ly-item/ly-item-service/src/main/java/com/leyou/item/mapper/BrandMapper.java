package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author leoso
 * @create 2019-12-28 20:03
 */
public interface BrandMapper extends BaseMapper<Brand> {

    @Insert("INSERT INTO tb_category_brand(category_id,brand_id) VALUES (#{cid},#{bid})")
    public int insertCategoryBrand(@Param("cid")Long cid, @Param("bid") Long bid);

    @Update("UPDATE tb_category_brand SET category_id=#{cid} WHERE brand_id = #{bid}")
    public int updateCategoryBrand(@Param("cid")Long cid, @Param("bid") Long bid);

    @Delete("DELETE FROM tb_category_brand WHERE brand_id = #{id}")
    public int deleteCategoryBrand(Long id);

    @Select("SELECT b.* FROM tb_brand b INNER JOIN tb_category_brand cb ON b.id = cb.brand_id WHERE cb.category_id = #{cid}")
    public List<Brand> queryBrandByCid(@Param("cid") Long cid);
}
