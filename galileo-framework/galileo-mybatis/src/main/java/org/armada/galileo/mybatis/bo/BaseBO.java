package org.armada.galileo.mybatis.bo;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface BaseBO<DO, DTO> {

	/**
	 * 插入一条记录（选择字段，策略插入）
	 *
	 * @param dto
	 */
	boolean insert(DTO dto);

	/**
	 * 根据 ID 选择修改
	 *
	 * @param dto
	 */
	boolean updateById(DTO dto);

	/**
	 * 更新DTO
	 *
	 * @param dto          DTO
	 * @param queryWrapper 查找
	 * @return boolean
	 */
	int updateDTO(DTO dto, Wrapper<DO> queryWrapper);

	/**
	 * 新增或更新，
	 * 当 id 有值时会先查，若查出的结果为空则新增，不为空则更新
	 * 当 id 为空时，执行新增逻辑
	 *
	 * @param dto
	 */
	boolean saveOrUpdate(DTO dto);

	/**
	 * 批量新增或更新，
	 * 当 id 有值时会先查，若查出的结果为空则新增，不为空则更新
	 * 当 id 为空时，执行新增逻辑
	 *
	 * @param entityList 实体对象集合
	 */
	boolean saveUpdateBatchDO(Collection<DO> entityList);

	/**
	 * 批量新增或更新，
	 * 当 id 有值时会先查，若查出的结果为空则新增，不为空则更新
	 * 当 id 为空时，执行新增逻辑
	 *
	 * @param dtoList 实体对象集合
	 */
	boolean saveUpdateBatchDTO(Collection<DTO> dtoList);

	/**
	 * 批量新增
	 *
	 * @param entityList
	 * @return
	 */
	boolean insertBatchDO(Collection<DO> entityList);

	/**
	 * 批量新增
	 *
	 * @param dtoList
	 * @return
	 */
	boolean insertBatchDTO(Collection<DTO> dtoList);

	/**
	 * 根据 id 批量更新 ， id 必须要有值否则会忽略
	 *
	 * @param entityList
	 * @return
	 */
	boolean updateBatchDO(Collection<DO> entityList);

	/**
	 * 根据 id 批量更新 ， id 必须要有值否则会忽略
	 *
	 * @param dtoList
	 * @return
	 */
	boolean updateBatchDTO(Collection<DTO> dtoList);

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
	 * 删除（根据查询条件批量删除）
	 *
	 * @param query
	 */
	void removeBatch(Wrapper<DO> query);

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
	List<DTO> selectList(Wrapper<DO> query);

	/**
	 * 根据 entity 条件，查询一条记录
	 *
	 * @param query 查询
	 * @return {@code DTO}
	 */
	DTO selectOne(Wrapper<DO> query);

	/**
	 * 查询（根据ID 批量查询）
	 *
	 * @param idList 主键ID列表
	 */
	List<DTO> selectByIds(Collection<? extends Serializable> idList);

	/**
	 * 查询 当前表所有记录 (只返回 1000 条)
	 *
	 * @return
	 */
	List<DTO> selectAll();

	/**
	 * do to dto
	 *
	 * @param entity
	 * @return
	 */
	public DTO convertToDto(DO entity);

	/**
	 * do to dto
	 *
	 * @param entityList
	 * @return
	 */
	public List<DTO> convertToDtoList(List<? extends DO> entityList);

	/**
	 * 条件计数
	 */
	int selectCount(Wrapper<DO> query);

	/**
	 * 分页查询
	 */
	Page<DTO> page(Page<DO> page, Wrapper<DO> wrapper);

	/**
	 * 获取 Mapper
	 */
	BaseMapper<DO> getBaseMapper();

	/**
	 * 大批量插入（DTO）
	 */
	void insertExecBatchDTO(Collection<DTO> dtoList);

	/**
	 * 大批量插入（DO）
	 */
	void insertExecBatchDO(Collection<DO> dos);

}
