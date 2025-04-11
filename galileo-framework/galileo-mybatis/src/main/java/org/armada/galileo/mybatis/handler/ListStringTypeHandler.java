package org.armada.galileo.mybatis.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import cn.hutool.json.JSONUtil;
import org.armada.galileo.common.util.JsonUtil;

@MappedJdbcTypes(JdbcType.VARCHAR)  //数据库类型
@MappedTypes({List.class})          //java数据类型
public class ListStringTypeHandler implements TypeHandler<List<String>> {
    @Override
    public void setParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JSONUtil.toJsonStr(parameter));
    }

    @Override
    public List<String> getResult(ResultSet rs, String columnName) throws SQLException {
        try {
            return JsonUtil.fromJson(rs.getString(columnName), new TypeToken<List<String>>() {
            }.getType());
        } catch (Exception e) {
            return new ArrayList<>(0);
        }
    }

    @Override
    public List<String> getResult(ResultSet rs, int columnIndex) throws SQLException {
        try {
            return JsonUtil.fromJson(rs.getString(columnIndex), new TypeToken<List<String>>() {
            }.getType());
        } catch (Exception e) {
            return new ArrayList<>(0);
        }
    }

    @Override
    public List<String> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        try {

            return JsonUtil.fromJson(cs.getString(columnIndex), new TypeToken<List<String>>() {
            }.getType());
        } catch (Exception e) {
            return new ArrayList<>(0);
        }
    }
}
