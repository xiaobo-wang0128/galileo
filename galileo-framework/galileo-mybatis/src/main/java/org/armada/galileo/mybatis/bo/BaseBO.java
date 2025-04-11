package org.armada.galileo.mybatis.bo;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

public interface BaseBO<DO, DTO> {

	/**
	 * 默认批次提交数量
	 */
	int DEFAULT_BATCH_SIZE = 1000;

	/**
	 * 插入一条记录（选择字段，策略插入）
	 *
	 * @param dto 实体对象
	 */
	boolean save(DTO dto);

	/**
	 * 根据 ID 选择修改
	 *
	 * @param dto 实体对象
	 */
	boolean updateById(DTO dto);

	/**
	 * TableId 注解存在更新记录，否插入一条记录
	 *
	 * @param dto 实体对象
	 */
	boolean saveOrUpdate(DTO dto);

	/**
	 * 插入（批量）
	 *
	 * @param dtoList 实体对象集合
	 */
	boolean saveBatch(Collection<DTO> dtoList);
	
	/**
	 * 插入（批量）
	 *
	 * @param dtoList 实体对象集合
	 */
	boolean saveBatchDO(Collection<DO> dtoList);
	

	/**
	 * 插入（批量）
	 *
	 * @param dtoList   实体对象集合
	 * @param batchSize 插入批次数量
	 */
	boolean saveBatch(Collection<DTO> dtoList, int batchSize);


    /**
     * 批量修改插入
     *
     * @param entityList 实体对象集合
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean saveOrUpdateBatch(Collection<DO> entityList) {
        return saveOrUpdateBatch(entityList, DEFAULT_BATCH_SIZE);
    }

    /**
     * 批量修改插入
     *
     * @param entityList 实体对象集合
     * @param batchSize  每次的数量
     */
    boolean saveOrUpdateBatch(Collection<DO> entityList, int batchSize);

    /**
     * 根据id 批量更新
     * @param entityList
     * @return
     */
    public boolean updateBatchById(Collection<DO> entityList);
	
	/**
	 * 根据 ID 删除
	 *
	 * @param id 主键ID
	 */
	boolean removeById(Serializable id);

	/**
	 * 删除（根据ID 批量删除）
	 *
	 * @param idList 主键ID列表
	 */
	boolean removeByIds(Collection<? extends Serializable> idList);

	/**
	 * 根据 ID 查询
	 *
	 * @param id 主键ID
	 */
	DTO selectById(Serializable id);

	
	/**
	 * 查询根据 QueryWrapper 查询
	 *
	 * @param 
	 */
	 List<DTO> selectList(QueryWrapper<DO> query); 
	
	/**
	 * 查询（根据ID 批量查询）
	 *
	 * @param idList 主键ID列表
	 */
	List<DTO> selectByIds(Collection<? extends Serializable> idList);

	/**
	 * 查询 当前表所有记录 (只返回 1000 条)
	 * 
	 * 
	 * @return
	 */
	List<DTO> selectAll();
	
	public DTO convertToDto(DO data);

	public List<DTO> convertToDtoList(List<? extends DO> list);

}
