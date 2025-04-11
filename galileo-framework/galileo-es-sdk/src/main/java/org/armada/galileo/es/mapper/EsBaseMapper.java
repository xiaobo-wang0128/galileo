package org.armada.galileo.es.mapper;


import org.armada.galileo.es.entity.EsBaseEntity;
import org.armada.galileo.es.query.EsQueryWrapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * es 单表操作接口
 *
 * @author xiaobo
 * @description: TODO
 * @date 2023/2/8 11:31
 */
public interface EsBaseMapper<T extends EsBaseEntity> {


    /**
     * 获取 class 名称
     *
     * @return
     */
    Class<T> getEntityClass();

    /**
     * 插入或更新一条记录
     *
     * @param entity 实体对象
     */
    void save(T entity);

    /**
     * 批量保存更新
     *
     * @param entityList
     */
    void batchSave(List<T> entityList) throws IOException;

    /**
     * 根据 ID 删除
     *
     * @param id 主键ID
     */
    void deleteById(String id);

    /**
     * 删除（根据ID 批量删除）
     *
     * @param idList 主键ID列表(不能为 null 以及 empty)
     */
    void deleteBatchIds(String[] idList);

    /**
     * 根据 entity 条件，删除记录
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null,里面的 entity 用于生成 where 语句）
     */
    void delete(EsQueryWrapper queryWrapper);

    /**
     * 根据 ID 查询
     *
     * @param id 主键ID
     */
    T selectById(String id);

    /**
     * 查询（根据ID 批量查询）
     *
     * @param idList 主键ID列表(不能为 null 以及 empty)
     */
    List<T> selectBatchIds(String[] idList);

    /**
     * 根据 entity 条件，查询一条记录
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
    T selectOne(EsQueryWrapper queryWrapper);

    /**
     * 根据 Wrapper 条件，查询总记录数
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
    Integer selectCount(EsQueryWrapper queryWrapper);

    /**
     * 根据 entity 条件，查询全部记录
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
    List<T> selectList(EsQueryWrapper queryWrapper);


    /**
     * 初始化索引结构至 es 服务器
     */
    public void initIndex();

    /**
     * 删除索引表（删除整张表，仅限于重建索引时调用 ）
     */
    public void dropIndex();

    /**
     * 分组数量汇总查询
     * @param queryWrapper
     * @param groupField
     * @return
     */
    public Map<String, Long> countWithGroupBy(EsQueryWrapper queryWrapper, String groupField);

}
