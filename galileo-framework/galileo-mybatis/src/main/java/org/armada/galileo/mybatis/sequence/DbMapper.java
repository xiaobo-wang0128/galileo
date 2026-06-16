package org.armada.galileo.mybatis.sequence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author xiaobo
 *
 * @date 2022/12/18 18:17
 */
public interface DbMapper <T> extends BaseMapper<T> {

   @Select("SELECT id,head,`day`,current_index,current_no FROM ${tableName} WHERE id = #{id} FOR UPDATE")
   SequenceNo selectById4Update(@Param("tableName") String tableName,@Param("id") String id);

   @Insert("INSERT INTO ${tableName} ( id, head, `day`, current_index, current_no )  VALUES  ( #{record.id}, #{record.head}, #{record.day}, #{record.currentIndex}, #{record.currentNo} )")
   void insertRecord(@Param("tableName") String tableName, @Param("record")  SequenceNo sequenceNo);

   @Update("UPDATE  ${tableName}  SET current_index=#{record.currentIndex}, current_no=#{record.currentNo}  WHERE id=#{record.id}")
   void updateRecord(@Param("tableName") String tableName,@Param("record")  T update);
}
