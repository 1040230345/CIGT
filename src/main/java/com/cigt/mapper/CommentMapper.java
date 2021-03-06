package com.cigt.mapper;

import com.cigt.dto.CommentDto;
import com.cigt.dto.CommentExt;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评论的持久层
 */
@Mapper
public interface CommentMapper {
    /**
     * 传入商品id，获取这个商品的一级评论
     */
    @Select(" SELECT t_user.name,t_user.real_name,t_user.image,t_user.sex,t_comment.* " +
            "FROM t_comment " +
            "LEFT JOIN t_user ON t_comment.user_id=t_user.id  " +
            "where pid =0 AND type=0 AND goods_id =#{goods_id} ")
    List<CommentDto> findCommentById(@Param("goods_id") int goods_id);

    /**
     * 获取子评论
     */
    @Select("select a.user_id,b.name,b.sex,a.content,a.create_time,b.image,b.real_name ," +
            "c.id as reply_id,c.image as reply_image,c.real_name as reply_real_name " +
            "from t_comment a,t_user b ,t_user c where " +
            "a.user_id = b.id AND " +
            "a.reply_user_id = c.id AND " +
            "a.goods_id= #{goods_id} AND " +
            "a.pid = #{pid} AND " +
            "a.type =1")
    List<CommentExt> getCommentSons(@Param("pid") int pid,
                                    @Param("goods_id") int goods_id);
    /**
     * 插入评论
     */
    @Insert("insert into t_comment (goods_id,user_id,content,create_time," +
            "type,reply_user_id,pid) VALUES (#{goods_id},#{user_id},#{content}," +
            "#{create_time},#{type},#{reply_user_id},#{pid})")
    int insertComment(CommentDto commentDto);
}
