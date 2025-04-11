package org.armada.galileo.annotation.openapi;

import java.lang.annotation.*;

/**
 * 仅用于 回传类的 接口定义， 用于告诉开放平台走哪个通道
 * 此设计是为了提升代码的复用性
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface AppId {

}
