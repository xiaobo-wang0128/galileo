package org.armada.galileo.i18n_server.web.rpc;

import com.google.gson.reflect.TypeToken;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.i18n_server.dal.bo.I18nAppBO;
import org.armada.galileo.i18n_server.dal.dto.I18nAppDTO;
import org.armada.galileo.annotation.mvc.NoToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2022/12/26 16:18
 */
@Controller
public class AppRpc {

    @Autowired
    private I18nAppBO appBO;

    /**
     * 新增或修改
     */
    public void saveUpdate(HttpServletRequest request) {

        String json = CommonUtil.readJsonForm(request);
        List<I18nAppDTO> dtoList = JsonUtil.fromJson(json, new TypeToken<List<I18nAppDTO>>() {
        }.getType());

        List<I18nAppDTO> oldList = appBO.selectAll();
        if (oldList != null && oldList.size() > 0) {
            List<Integer> newIds = dtoList.stream().map(e -> e.getId()).collect(Collectors.toList());
            for (I18nAppDTO i18nAppDTO : oldList) {
                if (!newIds.contains(i18nAppDTO.getId())) {
                    appBO.removeById(i18nAppDTO.getId());
                }
            }
        }

        for (I18nAppDTO i18nAppDTO : dtoList) {
            if (i18nAppDTO.getId() == null) {
                appBO.saveOrUpdate(i18nAppDTO);
            } else {
                appBO.updateById(i18nAppDTO);
            }
        }
    }

    /**
     * 根据id删除
     *
     * @param id
     */
    public void remove(Long id) {
        appBO.removeById(id);
    }

    /**
     * 查询所有记录，只返回前1000条
     *
     * @return
     */
    @NoToken
    public List<I18nAppDTO> selectAll() {
        return appBO.selectAll();
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @NoToken
    public I18nAppDTO detail(Long id) {
        return appBO.selectById(id);
    }

}
