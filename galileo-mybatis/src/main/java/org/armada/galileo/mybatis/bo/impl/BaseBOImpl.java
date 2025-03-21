package org.armada.galileo.mybatis.bo.impl;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;
import org.armada.galileo.common.page.PageList;
import org.armada.galileo.common.page.PageParam;
import org.armada.galileo.common.page.ThreadPagingUtil;
import org.armada.galileo.mybatis.bo.BaseBO;
import org.armada.galileo.mybatis.bo.MapstructConvertor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;

public class BaseBOImpl<DO, DTO, M extends BaseMapper<DO>, C extends MapstructConvertor<DO, DTO>> implements BaseBO<DO, DTO> {

	protected Log log = LogFactory.getLog(getClass());

	@Autowired
	protected M mapper;

	@Autowired
	protected C convertor;

	protected Class<DO> entityClass = currentModelClass();

	protected Class<DO> mapperClass = currentMapperClass();

	protected M getBaseMapper() {
		return this.mapper;
	}

	public DTO convertToDto(DO data) {
		if (data == null) {
			return null;
		}
		return convertor.toDTO(data);
	}

	public List<DTO> convertToDtoList(List<? extends DO> list) {
		if (list == null) {
			return null;
		}
		if (list instanceof PageList) {
			PageList<?> pageList = (PageList<?>) list;

			PageList<DTO> result = new PageList<DTO>(list.size());
			result.setPageIndex(pageList.getPageIndex());
			result.setHasNext(pageList.getHasNext());
			result.setHasPre(pageList.getHasPre());
			result.setPageSize(pageList.getPageSize());
			result.setTotalPage(pageList.getTotalPage());
			result.setTotalSize(pageList.getTotalSize());

			for (DO item : list) {
				result.add(convertToDto(item));
			}
			return result;
		} else {
			ArrayList<DTO> result = new ArrayList<DTO>(list.size());

			for (DO item : list) {
				result.add(convertToDto(item));
			}
			return result;
		}
	}

	@SuppressWarnings("unchecked")
	protected Class<DO> currentMapperClass() {
		return (Class<DO>) ReflectionKit.getSuperClassGenericType(getClass(), 2);
	}

	@SuppressWarnings("unchecked")
	protected Class<DO> currentModelClass() {
		return (Class<DO>) ReflectionKit.getSuperClassGenericType(getClass(), 0);
	}

	/**
	 * 获取mapperStatementId
	 *
	 * @param sqlMethod 方法名
	 * @return 命名id
	 * @since 3.4.0
	 */
	protected String getSqlStatement(SqlMethod sqlMethod) {
		return SqlHelper.getSqlStatement(mapperClass, sqlMethod);
	}

	/**
	 * 执行批量操作
	 *
	 * @param list      数据集合
	 * @param batchSize 批量大小
	 * @param consumer  执行方法
	 * @param <E>       泛型
	 * @return 操作结果
	 * @since 3.3.1
	 */
	protected <E> boolean executeBatch(Collection<E> list, int batchSize, BiConsumer<SqlSession, E> consumer) {
		return SqlHelper.executeBatch(this.entityClass, this.log, list, batchSize, consumer);
	}

	/**
	 * 插入一条记录（选择字段，策略插入）
	 *
	 * @param dto 实体对象
	 */
	public boolean save(DTO dto) {
		DO entity = convertor.toDO(dto);
		int insert = getBaseMapper().insert(entity);
		try {
			TableInfo tableInfo = TableInfoHelper.getTableInfo(this.entityClass);
			Object idVal = ReflectionKit.getFieldValue(entity, tableInfo.getKeyProperty());
			Field fid = dto.getClass().getDeclaredField(tableInfo.getKeyProperty());
			ReflectionUtils.makeAccessible(fid);
			ReflectionUtils.setField(fid, dto, idVal);

		} catch (Exception e) {
			log.error("dto id 回写失败");
			log.error(e.getMessage(), e);
		}

		return SqlHelper.retBool(insert);
	}

	/**
	 * 根据 ID 选择修改
	 *
	 * @param dto 实体对象
	 */
	public boolean updateById(DTO dto) {
		DO entity = convertor.toDO(dto);
		return SqlHelper.retBool(getBaseMapper().updateById(entity));
	}

	/**
	 * 插入（批量）
	 *
	 * @param entityList 实体对象集合
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean saveBatch(Collection<DTO> entityList) {
		return saveBatch(entityList, DEFAULT_BATCH_SIZE);
	}

	@Override
	public boolean saveBatchDO(Collection<DO> entityList) {
		String sqlStatement = getSqlStatement(SqlMethod.INSERT_ONE);
		return executeBatch(entityList, 1000, (sqlSession, entity) -> sqlSession.insert(sqlStatement, entity));
	}

	/**
	 * 根据 ID 删除
	 *
	 * @param id 主键ID
	 */
	public boolean removeById(Serializable id) {
		return SqlHelper.retBool(getBaseMapper().deleteById(id));
	}

	/**
	 * 删除（根据ID 批量删除）
	 *
	 * @param idList 主键ID列表
	 */
	public boolean removeByIds(Collection<? extends Serializable> idList) {
		if (CollectionUtils.isEmpty(idList)) {
			return false;
		}
		return SqlHelper.retBool(getBaseMapper().deleteBatchIds(idList));
	}

	/**
	 * 根据 ID 查询
	 *
	 * @param id 主键ID
	 */
	public DTO selectById(Serializable id) {
		DO entity = getBaseMapper().selectById(id);
		return convertor.toDTO(entity);
	}

	/**
	 * 查询（根据ID 批量查询）
	 *
	 * @param idList 主键ID列表
	 */
	public List<DTO> selectByIds(Collection<? extends Serializable> idList) {
		List<DO> doList = getBaseMapper().selectBatchIds(idList);
		return convertToDtoList(doList);
	}

	/**
	 * 查询根据 QueryWrapper 查询
	 *
	 * @param
	 */
	public List<DTO> selectList(QueryWrapper<DO> query) {
		List<DO> doList = getBaseMapper().selectList(query);
		return convertToDtoList(doList);
	}

	/**
	 * TableId 注解存在更新记录，否插入一条记录
	 *
	 * @param dto 实体对象
	 * @return boolean
	 */
	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean saveOrUpdate(DTO dto) {
		if (null != dto) {
			DO entity = convertor.toDO(dto);
			TableInfo tableInfo = TableInfoHelper.getTableInfo(this.entityClass);

			Assert.notNull(tableInfo, "error: can not execute. because can not find cache of TableInfo for entity!");
			String keyProperty = tableInfo.getKeyProperty();
			Assert.notEmpty(keyProperty, "error: can not execute. because can not find column for id from entity!");
			Object idVal = ReflectionKit.getFieldValue(entity, tableInfo.getKeyProperty());

			if (StringUtils.checkValNull(idVal) || Objects.isNull(selectById((Serializable) idVal))) {

				getBaseMapper().insert(entity);

				try {
					idVal = ReflectionKit.getFieldValue(entity, tableInfo.getKeyProperty());
					Field fid = dto.getClass().getDeclaredField(tableInfo.getKeyProperty());
					ReflectionUtils.makeAccessible(fid);
					ReflectionUtils.setField(fid, dto, idVal);
				} catch (Exception e) {
					log.error("dto id 回写失败");
					log.error(e.getMessage(), e);
				}

				return true;

			} else {
				return SqlHelper.retBool(getBaseMapper().updateById(entity));
			}
		}
		return false;
	}

	/**
	 * 批量插入
	 *
	 * @param dtoList   ignore
	 * @param batchSize ignore
	 * @return ignore
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean saveBatch(Collection<DTO> dtoList, int batchSize) {
		Collection<DO> entityList = dtoList.stream().map(e -> convertor.toDO(e)).collect(Collectors.toList());
		String sqlStatement = getSqlStatement(SqlMethod.INSERT_ONE);
		return executeBatch(entityList, batchSize, (sqlSession, entity) -> sqlSession.insert(sqlStatement, entity));
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean saveOrUpdateBatch(Collection<DO> entityList, int batchSize) {
		TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
		Assert.notNull(tableInfo, "error: can not execute. because can not find cache of TableInfo for entity!");
		String keyProperty = tableInfo.getKeyProperty();
		Assert.notEmpty(keyProperty, "error: can not execute. because can not find column for id from entity!");
		return SqlHelper.saveOrUpdateBatch(this.entityClass, this.mapperClass, this.log, entityList, batchSize, (sqlSession, entity) -> {
			Object idVal = ReflectionKit.getFieldValue(entity, keyProperty);
			return StringUtils.checkValNull(idVal) || CollectionUtils.isEmpty(sqlSession.selectList(getSqlStatement(SqlMethod.SELECT_BY_ID), entity));
		}, (sqlSession, entity) -> {
			MapperMethod.ParamMap<DO> param = new MapperMethod.ParamMap<>();
			param.put(Constants.ENTITY, entity);
			sqlSession.update(getSqlStatement(SqlMethod.UPDATE_BY_ID), param);
		});
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean updateBatchById(Collection<DO> entityList) {

		int batchSize = 1000;
		String sqlStatement = getSqlStatement(SqlMethod.UPDATE_BY_ID);
		return executeBatch(entityList, batchSize, (sqlSession, entity) -> {
			MapperMethod.ParamMap<DO> param = new MapperMethod.ParamMap<>();
			param.put(Constants.ENTITY, entity);
			sqlSession.update(sqlStatement, param);
		});
	}

	public List<DTO> selectAll() {
		if (ThreadPagingUtil.get() == null) {
			PageParam pageParam = PageParam.instanceByPageIndex(1,1000);
			ThreadPagingUtil.set(pageParam);
		}
		ThreadPagingUtil.turnOn();
		return convertToDtoList(getBaseMapper().selectList(null));
	}

}
