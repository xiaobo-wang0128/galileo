package org.armada.galileo.autoconfig.util;

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
public interface AutoConfigEntityMapper<T> extends BaseMapper<T> {

    @Select("SELECT tenant_id, tenant_type, config_class, config_value, update_time FROM ${table_name} WHERE tenant_id = #{tenant_id} and  tenant_type = #{tenant_type} and config_class = #{config_class} ")
    AutoConfigEntity selectConfig(
            @Param("table_name") String table_name,
            @Param("tenant_id") Long tenant_id,
            @Param("tenant_type") TenantTypeEnum tenant_type,
            @Param("config_class") String config_class
    );

    @Insert("INSERT INTO ${table_name} ( tenant_id, tenant_type, config_class, config_value, update_time) VALUES ( #{tenant_id}, #{tenant_type}, #{config_class}, #{config_value}, #{update_time} )")
    void insertConfig(
            @Param("table_name") String table_name,
            @Param("tenant_id") Long tenant_id,
            @Param("tenant_type") TenantTypeEnum tenant_type,
            @Param("config_class") String config_class,
            @Param("config_value") String config_value,
            @Param("update_time") Long update_time
    );

    @Update("UPDATE ${table_name}  SET tenant_id=#{tenant_id}, tenant_type=#{tenant_type}, config_class=#{config_class}, config_value=#{config_value}, update_time=#{update_time} WHERE tenant_id = #{tenant_id} and  tenant_type = #{tenant_type} and config_class = #{config_class}")
    void updateConfig(
            @Param("table_name") String table_name,
            @Param("tenant_id") Long tenant_id,
            @Param("tenant_type") TenantTypeEnum tenant_type,
            @Param("config_class") String config_class,
            @Param("config_value") String config_value,
            @Param("update_time") Long update_time
    );

}
