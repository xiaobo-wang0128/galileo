package org.armada.galileo.i18n_server.config.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.armada.galileo.i18n_server.dal.enums.StatusEnum;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
public class I18nMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
        // todo: 暂时没有考虑用户模块，给个默认值
        this.strictInsertFill(metaObject, "createUser", String.class, "system");
        this.strictInsertFill(metaObject, "createUserId", Long.class, 1L);
        this.strictInsertFill(metaObject, "status", StatusEnum.class, StatusEnum.ENABLE);
        this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
        this.strictInsertFill(metaObject, "updateUser", String.class, "system");
        this.strictInsertFill(metaObject, "updateUserId", Long.class, 1L);

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
        // todo: 暂时没有考虑用户模块，给个默认值
        this.strictUpdateFill(metaObject, "updateUser", String.class, "system");
        this.strictUpdateFill(metaObject, "updateUserId", Long.class, 1L);
    }
}
