package com.cigt.mapper;

import com.cigt.dto.GoodsDto;
import com.cigt.dto.UserDto;
import org.apache.ibatis.annotations.*;

/**
 * 操作t_user表
 */
@Mapper
public interface UserMapper {
    /**
     * 异步
     */
    @Select("select name from t_user where name = #{name}")
    String findUserNameByUserName(@Param("name") String name);
    /**
     * 注册
     */
    @Insert("INSERT INTO t_user (name,password,created_at,updated_at) " +
            "VALUES (#{name},#{password},#{created_at},#{updated_at})")
    @Options(useGeneratedKeys = true, keyProperty = "id",keyColumn="id")
    int insertUser(UserDto userDto);
    /**
     * 登录
     */
    @Select("select * from t_user where name = #{name} and password = #{password}")
    UserDto findUser(UserDto userDto);
    /**
     * 修改个人信息
     */
    @Update("update t_user set address = #{address},sex = #{sex}," +
            "phone = #{phone},Information_state=#{Information_state}," +
            "autograph=#{autograph},updated_at=#{updated_at}," +
            "real_name=#{real_name}" +
            " where id = #{id}")
    int updateUser(UserDto userDto);
    /**
     * 修改密码
     */
    @Update("update t_user set password =#{password} ,updated_at=#{updated_at} where id = #{id}")
    int updateUserPassword(@Param("password") String password,
                           @Param("id") int id,
                           @Param("updated_at") String updated_at);
    /**
     * 修改用户头像
     */
    @Update("update t_user set image = #{imagePath} where id = #{id}")
    int updateUserImage(@Param("imagePath") String imagePath,
                        @Param("id") int user_id);

    /**
     * 修改个人商品
     */
    @Update("update t_goods set name=#{name},depict=#{depict},price=#{price}," +
            "images=#{images},updated_at=#{updated_at},num=#{num}," +
            "category=#{category},banner_image1=#{banner_image1}," +
            "banner_image2=#{banner_image2},banner_image3=#{banner_image3} " +
            "where id = #{id}")
    int updateUserGoods(GoodsDto goodsDto);

    /**
     * 获取商品信息
     */
    @Select("select * from t_goods where id = #{goodsId} ")
    GoodsDto findGoodsMsgByGoodsId(@Param("goodsId") int goodsId);
}
