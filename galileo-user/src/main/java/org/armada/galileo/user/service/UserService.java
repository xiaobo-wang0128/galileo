package org.armada.galileo.user.service;

import org.armada.galileo.common.page.PageList;
import org.armada.galileo.user.domain.DataPrivDomain;
import org.armada.galileo.user.domain.UserInfoDomain;
import org.armada.galileo.user.domain.UserLoginLogDomain;
import org.armada.galileo.user.domain.UserRoleDomain;
import org.armada.galileo.user.vo.LoginLogSearchVO;
import org.armada.galileo.user.vo.SimpleSearchVO;

import java.util.List;

/**
 * @author xiaobo
 * @description: 用户模块数据持久化接口
 * @date 2022/2/7 10:35 上午
 */
public interface UserService {

    /*-- 角色相关 --*/

    void insertRole(UserRoleDomain dto);

    void updateRole(UserRoleDomain dto);

    void deleteRole(Long roleId);

    List<UserRoleDomain> selectRole(SimpleSearchVO vo);

    UserRoleDomain selectRoleById(Long roleId);

    /*-- 用户相关 --*/

    void insertUser(UserInfoDomain dto);

    void updateUser(UserInfoDomain dto);

    void deleteUser(Long userId);

    Integer selectUserCount();

    PageList<UserInfoDomain> selectUser(SimpleSearchVO vo);

    UserInfoDomain selectUserById(Long userId);


    /**
     * 只做查询，不做密码的转换逻辑
     * @param loginId
     * @return
     */
    UserInfoDomain selectUserByLoginId(String loginId);

    /**
     * 获取数据权限定义
     *
     * @return
     */
    List<DataPrivDomain> getDataPrivDomain();

    /**
     *  保存用户登陆日志
     * @param r
     */
    void insertUserLog(UserLoginLogDomain r);


    /**
     * 登陆日志查询
     *
     * @param vo
     * @return
     */
    PageList<UserLoginLogDomain> selectLoginLog(LoginLogSearchVO vo);


}
