package org.armada.galileo.user.web.rpc;

import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.mvc_plus.encrypt.SecurityIDUtil;
import org.armada.galileo.user.domain.LoginUser;
import org.armada.galileo.user.domain.ThreadUser;
import org.armada.galileo.user.domain.UserRoleDomain;
import org.armada.galileo.user.service.UserService;
import org.armada.galileo.user.vo.SimpleSearchVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.List;

@Controller
public class RoleRpc {

    @Autowired
    private UserService userService;

    public void saveUpdate(@RequestBody UserRoleDomain dto) {

        if (dto == null) {
            return;
        }
        dto.setId(null);
        if (CommonUtil.isNotEmpty(dto.getSecurityId())) {
            dto.setId(SecurityIDUtil.decryptId(dto.getSecurityId()));
        }

        LoginUser loginUser = ThreadUser.get();
        if (dto.getId() == null) {
            dto.setGmtCreate(new Date());
            dto.setGmtModified(new Date());
            if (loginUser != null) {
                dto.setCreator(loginUser.getName());
                dto.setModifier(loginUser.getName());
            }
            userService.insertRole(dto);

        } else {
            dto.setGmtModified(new Date());
            if (loginUser != null) {
                dto.setModifier(loginUser.getName());
            }
            userService.updateRole(dto);
        }
    }

    public void delete(String securityId) {
        Long roleId = SecurityIDUtil.decryptId(securityId);
        userService.deleteRole(roleId);
    }

    public List<UserRoleDomain> list(SimpleSearchVO vo) {
        List<UserRoleDomain> list = userService.selectRole(vo);
        if(CommonUtil.isNotEmpty(list)){
            list.stream().forEach(e -> {
                e.setSecurityId(SecurityIDUtil.encryptId(e.getId()));
                e.setBtnCodes(null);
                e.setMenus(null);
                e.setDataUris(null);
            });
        }
        return list;
    }

    public UserRoleDomain detail(String securityId) {
        Long roleId = SecurityIDUtil.decryptId(securityId);

        UserRoleDomain e = userService.selectRoleById(roleId);
        e.setSecurityId(SecurityIDUtil.encryptId(e.getId()));
        return e;
    }

}
