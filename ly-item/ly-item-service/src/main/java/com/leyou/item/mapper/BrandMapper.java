package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author leoso
 * @create 2019-12-28 20:03
 */
public interface BrandMapper extends Mapper<Brand> {

    @Insert("INSERT INTO tb_category_brand(category_id,brand_id) VALUES (#{cid},#{bid})")
    public int insertCategoryBrand(@Param("cid")Long cid, @Param("bid") Long bid);

    @Update("UPDATE tb_category_brand SET category_id=#{cid} WHERE brand_id = #{bid}")
    public int updateCategoryBrand(@Param("cid")Long cid, @Param("bid") Long bid);

    @Delete("DELETE FROM tb_category_brand WHERE brand_id = #{id}")
    public int deleteCategoryBrand(Long id);
}
