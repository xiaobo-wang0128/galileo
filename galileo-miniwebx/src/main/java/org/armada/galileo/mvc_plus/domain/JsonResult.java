package org.armada.galileo.mvc_plus.domain;


import org.armada.galileo.common.page.PageList;
import org.armada.galileo.common.util.JsonUtil;

import java.util.Collection;

public class JsonResult {

    // 错误码
    // 超时
    // 登陆
    // 权限
    // 业务异常
    // 系统异常
    /**
     * <pre>
     * 0:		操作成功
     * 404 	接口不存在
     * 500 	后台业务异常（已捕获）
     * 501		后台系统异常（未捕获）
     * 601 	未登陆
     * 602 	权限异常
     * </pre>
     */
    private Integer code = 0;

    // 返回的数据对象
    private Object data;

    // 错误信息
    private String message;

    public JsonResult() {
    }

    private final static String PageListCLassName = PageList.class.getName();

    public JsonResult(Object data) {
        this.setData(data);
    }

    public void setData(Object data) {
        if (data != null) {
            String typeName = data.getClass().getTypeName();

            if (typeName.equals(PageListCLassName)) {

                PageList<?> pageList = (PageList<?>) data;

                PageObject obj = new PageObject();
                obj.setRows(pageList);
                obj.setPageIndex(pageList.getPageIndex());
                obj.setHasNext(pageList.getHasNext());
                obj.setHasPre(pageList.getHasPre());
                obj.setPageSize(pageList.getPageSize());
                obj.setTotalPage(pageList.getTotalPage());
                obj.setTotalSize(pageList.getTotalSize());

                this.data = obj;
            } else if (data instanceof Collection) {

                PageObject obj = new PageObject();
                obj.setRows(data);

                this.data = obj;
            } else {
                this.data = data;
            }
        }
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
