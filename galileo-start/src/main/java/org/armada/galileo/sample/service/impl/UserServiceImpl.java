package org.armada.galileo.sample.service.impl;

import org.armada.galileo.common.page.PageList;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.user.domain.DataPrivDomain;
import org.armada.galileo.user.domain.UserInfoDomain;
import org.armada.galileo.user.domain.UserLoginLogDomain;
import org.armada.galileo.user.domain.UserRoleDomain;
import org.armada.galileo.user.service.UserService;
import org.armada.galileo.user.vo.LoginLogSearchVO;
import org.armada.galileo.user.vo.SimpleSearchVO;
import org.armada.galileo.util.CacheType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2022/2/7 12:41 下午
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private org.armada.galileo.util.JedisUtil jedisUtil;

    private static final String RoleRow = "roleRow";

    private static final String RoleIndex = "roleIndex";

    private static final String UserRow = "userRow";

    private static final String UserIndex = "userIndex";


    @Override
    public void insertRole(UserRoleDomain dto) {
        dto.setId(Long.valueOf(CommonUtil.getRandomNumber(12)));
        jedisUtil.lpush(CacheType.ApiDocCache, RoleRow, JsonUtil.toJson(dto));
        long len = jedisUtil.llen(CacheType.ApiDocCache, RoleRow);
        jedisUtil.hset(CacheType.ApiDocCache, RoleIndex, dto.getId().toString(), len + "");
    }

    @Override
    public void updateRole(UserRoleDomain dto) {
        long len = jedisUtil.llen(CacheType.ApiDocCache, RoleRow);
        String i = jedisUtil.hget(CacheType.ApiDocCache, RoleIndex, dto.getId().toString());

        int index = (int) len - Integer.valueOf(i);
        jedisUtil.lset(CacheType.ApiDocCache, RoleRow, index, JsonUtil.toJson(dto));
    }

    @Override
    public void deleteRole(Long roleId) {
//        long len = jedisUtil.llen(CacheType.ApiDocCache, roleRow);
//        String i = jedisUtil.hget(CacheType.ApiDocCache, roleIndex, roleId.toString());
//        int index = (int) len - Integer.valueOf(i);
        jedisUtil.lrem(CacheType.ApiDocCache, RoleIndex, 1, roleId.toString());
    }

    @Override
    public List<UserRoleDomain> selectRole(SimpleSearchVO vo) {
        long len = jedisUtil.llen(CacheType.ApiDocCache, RoleRow);
        List<String> reslut = jedisUtil.lrange(CacheType.ApiDocCache, RoleRow, 0, len);
        List<UserRoleDomain> list = reslut.stream().map(e -> JsonUtil.fromJson(e, UserRoleDomain.class)).collect(Collectors.toList());
        return list;
    }

    @Override
    public UserRoleDomain selectRoleById(Long roleId) {
        long len = jedisUtil.llen(CacheType.ApiDocCache, RoleRow);
        String i = jedisUtil.hget(CacheType.ApiDocCache, RoleIndex, roleId.toString());
        int index = (int) len - Integer.valueOf(i);
        String json = jedisUtil.lindex(CacheType.ApiDocCache, RoleRow, index);
        return JsonUtil.fromJson(json, UserRoleDomain.class);
    }

    // ---------

    @Override
    public void insertUser(UserInfoDomain dto) {
        dto.setId(Long.valueOf(CommonUtil.getRandomNumber(12)));
        jedisUtil.lpush(CacheType.ApiDocCache, UserRow, JsonUtil.toJson(dto));
        long len = jedisUtil.llen(CacheType.ApiDocCache, UserRow);
        jedisUtil.hset(CacheType.ApiDocCache, UserIndex, dto.getId().toString(), len + "");
    }

    @Override
    public void updateUser(UserInfoDomain dto) {
        long len = jedisUtil.llen(CacheType.ApiDocCache, UserRow);
        String i = jedisUtil.hget(CacheType.ApiDocCache, UserIndex, dto.getId().toString());

        int index = (int) len - Integer.valueOf(i);
        jedisUtil.lset(CacheType.ApiDocCache, UserRow, index, JsonUtil.toJson(dto));
    }

    @Override
    public void deleteUser(Long userId) {
        jedisUtil.lrem(CacheType.ApiDocCache, UserIndex, 1, userId.toString());
    }


    @Override
    public Integer selectUserCount() {

        long len = jedisUtil.llen(CacheType.ApiDocCache, UserRow);

        return (int) len;
    }


    @Override
    public PageList<UserInfoDomain> selectUser(SimpleSearchVO vo) {

        long len = jedisUtil.llen(CacheType.ApiDocCache, UserRow);

        int start = (vo.getPageIndex() - 1) * vo.getPageSize();
        int limit = start + vo.getPageSize();

        if (start >= len) {
            return null;
        }
        if (limit >= len) {
            limit = (int) len;
        }

        List<String> reesults = jedisUtil.lrange(CacheType.ApiDocCache, UserRow, start, limit - 1);
        List<UserInfoDomain> list = reesults.stream().map(e -> JsonUtil.fromJson(e, UserInfoDomain.class)).collect(Collectors.toList());

        list = list.stream().filter(e -> {

            if (vo != null) {

                if (CommonUtil.isNotEmpty(vo.getStatus()) && !vo.getStatus().equals(e.getStatus())) {
                    return false;
                }
                if (CommonUtil.isNotEmpty(vo.getKeyword()) && e.getSearchKeywords().indexOf(vo.getKeyword().trim()) == -1) {
                    return false;
                }

            }


            return true;
        }).collect(Collectors.toList());


        PageList<UserInfoDomain> pr = new PageList<>();
        pr.addAll(list);
        pr.setPageIndex(vo.getPageIndex());
        pr.setPageSize(vo.getPageSize());
        pr.setTotalSize((int) len);

        return pr;
    }

    @Override
    public UserInfoDomain selectUserById(Long userId) {
        long len = jedisUtil.llen(CacheType.ApiDocCache, UserRow);
        String i = jedisUtil.hget(CacheType.ApiDocCache, UserIndex, userId.toString());

        int index = (int) len - Integer.valueOf(i);

        String json = jedisUtil.lindex(CacheType.ApiDocCache, UserRow, index);
        return JsonUtil.fromJson(json, UserInfoDomain.class);
    }


    @Override
    public UserInfoDomain selectUserByLoginId(String loginId) {
        long len = jedisUtil.llen(CacheType.ApiDocCache, UserRow);
        List<String> result = jedisUtil.lrange(CacheType.ApiDocCache, UserRow, 0, len);
        UserInfoDomain domain = result
                .stream().map(e -> JsonUtil.fromJson(e, UserInfoDomain.class)).collect(Collectors.toList())
                .stream().filter(e -> e.getLoginId().equals(loginId)).findFirst().get();
        return domain;
    }

    @Override
    public void insertUserLog(UserLoginLogDomain r) {
        r.setId(Long.valueOf(CommonUtil.getRandomNumber(12)));
        jedisUtil.lpush(CacheType.ApiDocCache, "user_log_row", JsonUtil.toJson(r));
        long len = jedisUtil.llen(CacheType.ApiDocCache, "user_log_row");
        jedisUtil.hset(CacheType.ApiDocCache, "user_log_index", r.getId().toString(), len + "");
    }

    @Override
    public PageList<UserLoginLogDomain> selectLoginLog(LoginLogSearchVO vo) {
        long len = jedisUtil.llen(CacheType.ApiDocCache, "user_log_row");
        List<String> reslut = jedisUtil.lrange(CacheType.ApiDocCache, "user_log_row", 0, len);
        List<UserLoginLogDomain> list = reslut.stream().map(e -> JsonUtil.fromJson(e, UserLoginLogDomain.class)).collect(Collectors.toList());
        return new PageList(list);
    }

    @Override
    public List<DataPrivDomain> getDataPrivDomain() {

        List<DataPrivDomain> privs = new ArrayList<>();
        privs.add(new DataPrivDomain("管理员", "能查看所有数据", "admin"));
        privs.add(new DataPrivDomain("操作员", "只能查看自己操作的数据", "admin"));

        return privs;
    }
}
