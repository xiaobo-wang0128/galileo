package org.armada.galileo.annotation.mvc;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用户自定义输出.使用本注释用于自定义rpc请求的输出格式，可使用 response.getOutputStream()输出
 * @author wang xiaobo
 * @date
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface UserDefine {

}
