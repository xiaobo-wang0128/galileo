package org.armada.galileo.mybatis.bo.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;
import org.armada.galileo.common.page.PageList;
import org.armada.galileo.common.page.PageParam;
import org.armada.galileo.common.page.ThreadPagingUtil;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.model.constant.YesOrNoEnum;
import org.armada.galileo.mybatis.bo.BaseBO;
import org.armada.galileo.mybatis.bo.MapstructConvertor;
import org.armada.galileo.mybatis.domain.BaseDTO;
import org.armada.galileo.mybatis.domain.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


public class BaseBOImpl<DO, DTO, M extends BaseMapper<DO>, C extends MapstructConvertor<DO, DTO>> implements BaseBO<DO, DTO> {

	protected Log log = LogFactory.getLog(getClass());

	@Autowired
	protected M mapper;

	@Autowired
	protected C convertor;

	protected Class<DO> entityClass = currentModelClass();

	protected Class<DO> mapperClass = currentMapperClass();

	private boolean isBaseEntity = isBaseEntity();

	@Override
	public M getBaseMapper() {
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

	private boolean isBaseEntity() {
		if (entityClass.getSuperclass().getName().equals(BaseEntity.class.getName())) {
			return true;
		}
		return false;
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
	 * @param dto
	 */
	public boolean insert(DTO dto) {
		DO entity = convertor.toDO(dto);

		boolean bool = SqlHelper.retBool(getBaseMapper().insert(entity));
		//try {
		if (isBaseEntity) {
			BaseDTO baseDto = (BaseDTO) dto;
			TableInfo tableInfo = TableInfoHelper.getTableInfo(this.entityClass);
			Object idVal = ReflectionKit.getFieldValue(entity, tableInfo.getKeyProperty());
			baseDto.setId((Long) idVal);
		} else {
			throw new RuntimeException("仅支持 baseEntity 的子类");
		}

		return bool;
	}

	/**
	 * 根据 ID 选择修改
	 *
	 * @param dto
	 */
	public boolean updateById(DTO dto) {
		DO entity = convertor.toDO(dto);
		return SqlHelper.retBool(getBaseMapper().updateById(entity));
	}

//
//    @Override
//    public boolean updateDTO(DTO dto, UpdateWrapper<DO> updateWrapper) {
//        if (dto != null) {
//            return getBaseMapper().update(convertor.toDO(dto), updateWrapper) > 0;
//        } else {
//            return getBaseMapper().update(null, updateWrapper) > 0;
//        }
//    }


	@Override
	public int updateDTO(DTO dto, Wrapper<DO> queryWrapper) {
		if (dto != null) {
			return getBaseMapper().update(convertor.toDO(dto), queryWrapper);
		} else {
			return getBaseMapper().update(null, queryWrapper);
		}
	}

	/**
	 * 根据 ID 删除
	 *
	 * @param id 主键ID
	 */
	public boolean removeById(Serializable id) {
		if (isBaseEntity) {
			try {
				DO entity = entityClass.newInstance();
				((BaseEntity) entity).setId((Long) id).setIsDelete(YesOrNoEnum.Y);
				SqlHelper.retBool(getBaseMapper().updateById(entity));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return false;
			}
			return true;
		}
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

	@Override
	public void removeBatch(Wrapper<DO> query) {
		try {
			DO entity = entityClass.newInstance();
			((BaseEntity) entity).setIsDelete(YesOrNoEnum.Y);
			getBaseMapper().update(entity, query);
		} catch (Exception e) {
			throw new BizException(e);
		}
	}

	/**
	 * 根据 ID 查询
	 *
	 * @param id 主键ID
	 */
	public DTO selectById(Serializable id) {
		DO entity = getBaseMapper().selectById(id);
		if (Objects.isNull(entity)) {
			return null;
		}
		if (isBaseEntity) {
			BaseEntity be = ((BaseEntity) entity);
			if (YesOrNoEnum.Y == (be.getIsDelete())) {
				return null;
			}
		}
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
	public List<DTO> selectList(Wrapper<DO> query) {
		if (query instanceof QueryWrapper) {
			((QueryWrapper) query).eq("is_delete", YesOrNoEnum.N.name());
		} else if (query instanceof LambdaQueryWrapper) {
			((LambdaQueryWrapper<DO>) query).apply("is_delete='N'");
			// ((LambdaQueryWrapper<DO>) query).last("is_delete='N'");
		}

		List<DO> doList = getBaseMapper().selectList(query);
		return convertToDtoList(doList);
	}

	/**
	 * 根据 entity 条件，查询一条记录
	 *
	 * @param query 查询
	 * @return {@code DTO}
	 */
	public DTO selectOne(Wrapper<DO> query) {
		if (query instanceof QueryWrapper) {
			((QueryWrapper) query).eq("is_delete", YesOrNoEnum.N.name());
		} else if (query instanceof LambdaQueryWrapper) {
			((LambdaQueryWrapper<DO>) query).apply("is_delete='N'");
		}
		DO entity = getBaseMapper().selectOne(query);
		if (entity == null) {
			return null;
		}
		return convertor.toDTO(entity);
	}


	/**
	 * 保存或更新记录
	 * 当id为空时，调用 insert 语句
	 * 当id不为空时，先根据id查询，若返回为空则新增，返回不为空则更新
	 *
	 * @param dto
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean saveOrUpdate(DTO dto) {
		if (!isBaseEntity) {
			throw new BizException("当前 Entity 不是 BaseEntity 的子类，不支持该操作");
		}

		if (null != dto) {
			DO aDo = convertor.toDO(dto);
			BaseEntity entity = (BaseEntity) aDo;
			if (entity.getId() == null) {
				mapper.insert(aDo);
			} else {
				if (mapper.selectById(entity.getId()) == null) {
					mapper.insert(aDo);
				} else {
					mapper.updateById(aDo);
				}
			}
			// 回写 id
			BaseDTO baseDto = (BaseDTO) dto;
			baseDto.setId(entity.getId());
			return true;
		}
		return false;
	}

	@Override
	public boolean saveUpdateBatchDTO(Collection<DTO> dtoList) {
		if (CommonUtil.isEmpty(dtoList)) {
			return false;
		}
		for (DTO dto : dtoList) {
			saveOrUpdate(dto);
		}
		return true;
	}

	@Override
	public boolean insertBatchDO(Collection<DO> entityList) {
		if (CommonUtil.isEmpty(entityList)) {
			return false;
		}

		for (DO ado : entityList) {
			mapper.insert(ado);
		}

		return true;
	}

	@Override
	public boolean insertBatchDTO(Collection<DTO> dtoList) {
		if (CommonUtil.isEmpty(dtoList)) {
			return false;
		}

		for (DTO dto : dtoList) {
			DO ado = convertor.toDO(dto);
			mapper.insert(ado);
		}
		return true;
	}

	@Override
	public boolean saveUpdateBatchDO(Collection<DO> entityList) {
		if (CommonUtil.isEmpty(entityList)) {
			return false;
		}

		if (!isBaseEntity) {
			throw new BizException("当前 Entity 不是 BaseEntity 的子类，不支持该操作");
		}

		for (DO aDo : entityList) {
			BaseEntity entity = (BaseEntity) aDo;
			if (entity.getId() == null) {
				mapper.insert(aDo);
			} else {
				if (mapper.selectById(entity.getId()) == null) {
					mapper.insert(aDo);
				} else {
					mapper.updateById(aDo);
				}
			}
		}
		return true;
	}


	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean updateBatchDO(Collection<DO> entityList) {

		if (!isBaseEntity) {
			throw new BizException("当前 Entity 不是 BaseEntity 的子类，不支持该操作");
		}

		for (DO aDo : entityList) {
			BaseEntity entity = (BaseEntity) aDo;
			if (entity.getId() == null) {
				continue;
			}
			mapper.updateById(aDo);
		}

		return true;
	}

	@Override
	public boolean updateBatchDTO(Collection<DTO> dtoList) {
		if (CommonUtil.isEmpty(dtoList)) {
			return false;
		}
		Collection<DO> entityList = dtoList.stream().map(e -> convertor.toDO(e)).collect(Collectors.toList());
		return updateBatchDO(entityList);
	}

	public List<DTO> selectAll() {
		if (ThreadPagingUtil.get() == null) {
			PageParam pp = PageParam.instanceByOffset(0, 1000);
			ThreadPagingUtil.set(pp);
			ThreadPagingUtil.turnOn();
		}

		if (isBaseEntity) {
			QueryWrapper<DO> query = new QueryWrapper<>();
			query.eq("is_delete", YesOrNoEnum.N.name());
			return convertToDtoList(getBaseMapper().selectList(query));
		}
		return convertToDtoList(getBaseMapper().selectList(null));
	}


	private static final int MAX_BATCH_SIZE = 1000;

	@Override
	public int selectCount(Wrapper<DO> query) {
		Integer count = getBaseMapper().selectCount(query);
		return count == null ? 0 : count;
	}

	@Override
	public Page<DTO> page(Page<DO> page, Wrapper<DO> wrapper) {
		Page<DO> resultPage = getBaseMapper().selectPage(page, wrapper);
		Page<DTO> pageResult = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
		pageResult.setRecords(convertor.toListDTO(resultPage.getRecords()));
		return pageResult;
	}

	@Override
	public void insertExecBatchDTO(Collection<DTO> dtoList) {
		this.insertBatchDTO(dtoList);
	}

	@Override
	public void insertExecBatchDO(Collection<DO> entityList) {
		this.insertBatchDO(entityList);
	}

}
