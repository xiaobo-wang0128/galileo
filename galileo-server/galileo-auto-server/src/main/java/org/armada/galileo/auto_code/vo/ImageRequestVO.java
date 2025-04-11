package org.armada.galileo.auto_code.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.awt.*;
import java.io.Serializable;

/**
 * 镜像下载请求
 *
 * @author xiaobo
 * @date 2022/3/12 4:54 PM
 */
@Data
@Accessors(chain = true)
public class ImageRequestVO implements Serializable {

    /**
     * 镜像地址名
     */
    private String imagePath;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 状态： success 生成成功，可下载， saving 生成中, fail 生成失败, done 已下载
     */
    private String status;

    /**
     * 镜像在磁盘上的绝对路径
     */
    private String absoluteFilePath;

    /**
     * 完成时间
     */
    private Long doneTime;

    /**
     * 失败信息
     */
    private String failMessage;


}
