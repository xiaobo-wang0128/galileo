package org.armada.galileo.user.web.rpc;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.AssertUtil;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.annotation.mvc.NoToken;
import org.armada.galileo.mvc_plus.encrypt.SecurityIDUtil;
import org.armada.galileo.user.domain.*;
import org.armada.galileo.user.service.UserService;
import org.armada.galileo.user.util.LoginCookieUtil;
import org.armada.galileo.user.vo.LoginLogSearchVO;
import org.armada.galileo.user.vo.SimpleSearchVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Controller
@Slf4j
public class UserRpc implements CommandLineRunner {

    @Autowired
    private UserService userService;

    private static Executor ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * 新增、修改
     *
     * @param dto
     */
    public String saveUpdate(@RequestBody UserInfoDomain dto) {

        if (CommonUtil.isNotEmpty(dto.getSecurityId())) {
            dto.setId(SecurityIDUtil.decryptId(dto.getSecurityId()));
        }
        if (CommonUtil.isNotEmpty(dto.getSecurityRoleId())) {
            dto.setRoleId(SecurityIDUtil.decryptId(dto.getSecurityRoleId()));
        }

        AssertUtil.isNotNull(dto.getLoginId(), "登陆账号");
        AssertUtil.isNotNull(dto.getRoleId(), "角色");
        AssertUtil.isNotNull(dto.getName(), "姓名");

        UserRoleDomain role = userService.selectRoleById(dto.getRoleId());
        if (role == null) {
            throw new BizException("用户角色不存在");
        }
        dto.setRoleName(role.getName());
        dto.setSearchKeywords(dto.getName().trim() + dto.getLoginId().trim());
        if (dto.getId() == null) {
            String pwd = CommonUtil.getRandomNumber(6);
            dto.updatePwd(pwd);
            dto.setStatus("enable");
            userService.insertUser(dto);
            return pwd;

        } else {
            userService.updateUser(dto);
        }
        return null;
    }

    /**
     * 重置密码
     *
     * @param securityId
     * @return
     */
    public Map<String, String> resetPwd(String securityId) {
        Long userId = SecurityIDUtil.decryptId(securityId);
        UserInfoDomain user = userService.selectUserById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        String pwd = CommonUtil.getRandomNumber(6);
        user.updatePwd(pwd);
        userService.updateUser(user);

        Map<String, String> map = new HashMap<>();
        map.put("loginId", user.getLoginId());
        map.put("pwd", pwd);

        return map;
    }

    /**
     * 修改密码
     */
    public void updatePwd(String newPwd) {
        if (CommonUtil.isEmpty(newPwd)) {
            throw new BizException("新密码不能为空字符串");
        }
        LoginUser loginUser = ThreadUser.get();
        if (loginUser == null) {
            throw new BizException("用户未登陆");
        }

        UserInfoDomain user = userService.selectUserById(loginUser.getUserId());

        user.updatePwd(newPwd);

        userService.updateUser(user);
    }

    /**
     * 查询
     *
     * @param vo
     * @return
     */
    public List<UserInfoDomain> list(SimpleSearchVO vo) {
        List<UserInfoDomain> list = userService.selectUser(vo);
        if (list != null) {
            list.stream().forEach(e -> {
                e.setSecurityId(SecurityIDUtil.encryptId(e.getId()));
                e.setPassword(null);
                e.setSalt(null);
            });
        }
        return list;
    }

    /**
     * 删除
     *
     * @param securityId
     */
    public void delete(String securityId) {
        Long userId = SecurityIDUtil.decryptId(securityId);
        userService.deleteUser(userId);
    }

    /**
     * 用户详情
     *
     * @param securityId
     * @return
     */
    public UserInfoDomain detail(String securityId) {
        Long userId = SecurityIDUtil.decryptId(securityId);
        UserInfoDomain user = userService.selectUserById(userId);
        user.setSecurityId(SecurityIDUtil.encryptId(user.getId()));
        user.setSecurityRoleId(SecurityIDUtil.encryptId(user.getRoleId()));
        return user;
    }

    /**
     * 冻结、解冻
     *
     * @param type
     * @param securityId
     */
    public void freeze(String type, String securityId) {
        Long userId = SecurityIDUtil.decryptId(securityId);

        UserInfoDomain user = userService.selectUserById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        if ("freeze".equals(type)) {
            user.setStatus("disable");
        } else if ("unfreeze".equals(type)) {
            user.setStatus("enable");
        }

        userService.updateUser(user);
    }


    /**
     * 获取当前登陆信息
     *
     * @return
     */
    @NoToken
    public Map<String, Object> getLogin(HttpServletRequest request) {
        LoginUser login = ThreadUser.get();

        Map<String, Object> loginResult = new HashMap<>();
        loginResult.put("name", login.getName());
        loginResult.put("userId", login.getUserId());
        loginResult.put("roleId", login.getRoleId());
        if (login.getRoleId() > 0) {
            UserRoleDomain role = userService.selectRoleById(login.getRoleId());
            if (role == null) {
                throw new BizException("用户的权限不存在，请联系管理员");
            }

            loginResult.put("menuPath", role.getMenus());
            loginResult.put("btnCode", role.getBtnCodes());
        }

        return loginResult;
    }

    @NoToken
    public Map<String, Object> login(String loginId, String pwd, HttpServletRequest request, HttpServletResponse response) {
        UserInfoDomain userInfo = userService.selectUserByLoginId(loginId);
        if (userInfo == null) {
            throw new BizException("用户不存在或密码错误");
        }
        if (userInfo.getRoleId() == null) {
            throw new BizException("用户没有配置权限，请联系管理员");
        }
        if (!"enable".equals(userInfo.getStatus())) {
            throw new BizException("用户已冻结，请联系管理员");
        }

        userInfo.checkPwd(pwd);

        LoginUser loginUser = setCookie(userInfo, request, response);

        ex.execute(() -> {
            UserLoginLogDomain r = new UserLoginLogDomain();
            r.setType("login").setHappenTime(new Date()).setLoginId(CommonUtil.getIpAddr(request)).setLoginId(loginId).setName(userInfo.getName()).setRoleId(userInfo.getRoleId()).setRoleName(userInfo.getRoleName()).setSearchKeywords(userInfo.getName() + userInfo.getLoginId());
            userService.insertUserLog(r);
        });

        Map<String, Object> loginResult = new HashMap<>();
        loginResult.put("name", userInfo.getName());
        loginResult.put("userId", userInfo.getId());
        loginResult.put("roleId", userInfo.getRoleId());
        if (userInfo.getRoleId() > 0) {
            UserRoleDomain role = userService.selectRoleById(userInfo.getRoleId());
            if (role == null) {
                throw new BizException("用户的权限不存在，请联系管理员");
            }

            loginResult.put("menuPath", role.getMenus());
            loginResult.put("btnCode", role.getBtnCodes());
        }

        return loginResult;
    }

    @NoToken
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        LoginUser loginUser = ThreadUser.get();
        if (loginUser == null) {
            return;
        }
        ex.execute(() -> {
            UserInfoDomain userInfo = userService.selectUserById(loginUser.getUserId());
            if (userInfo == null) {
                return;
            }
            UserLoginLogDomain r = new UserLoginLogDomain();
            r.setType("logout").setHappenTime(new Date()).setLoginId(CommonUtil.getIpAddr(request)).setLoginId(userInfo.getLoginId()).setName(userInfo.getName()).setRoleId(userInfo.getRoleId()).setRoleName(userInfo.getRoleName()).setSearchKeywords(userInfo.getName() + userInfo.getLoginId());
            userService.insertUserLog(r);
        });

        LoginCookieUtil.clearLogin(request, response);
    }

    private LoginUser setCookie(UserInfoDomain userInfo, HttpServletRequest request, HttpServletResponse response) {

        // 登陆
        LoginUser u = new LoginUser();
        u.setMobile(userInfo.getPhone());
        u.setUserId(userInfo.getId());
        if (CommonUtil.isNotEmpty(userInfo.getNickname())) {
            u.setName(userInfo.getName() + "（" + userInfo.getNickname() + "）");
        } else {
            u.setName(userInfo.getName());
        }
        u.setRoleId(userInfo.getRoleId());
//        u.setEmail(userInfo.getEmail());
//        u.setNickname(userInfo.getNickname());
        u.setAvatarUrl(userInfo.getAvatarUrl());
        LoginCookieUtil.setLoginData(u, request, response);
        return u;
    }

    public List<UserLoginLogDomain> selectLoginLog(LoginLogSearchVO vo) {
        return userService.selectLoginLog(vo);
    }

    public List<DataPrivDomain> getDataPrivDomain() {
        return userService.getDataPrivDomain();
    }


    @Override
    public void run(String... args) throws Exception {

        // 如果用户表为空，则初始一个管理员用户
        int userNums = userService.selectUserCount();
        if (userNums == 0) {
            UserInfoDomain admin = new UserInfoDomain();
            admin.updatePwd("123456");

            admin.setRoleId(0L);
            admin.setLoginId("admin");
            admin.setName("管理员");
            admin.setRoleName("管理员");
            admin.setStatus("enable");
            userService.insertUser(admin);
        }
    }

    public static void main(String[] args) {
        UserInfoDomain admin = new UserInfoDomain();
        admin.updatePwd("123456");
        System.out.println(JsonUtil.toJson(admin));

        System.out.println(CommonUtil.md5("123456"));

    }
}
