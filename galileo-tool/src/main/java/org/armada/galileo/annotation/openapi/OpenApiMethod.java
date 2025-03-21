package org.armada.galileo.annotation.openapi;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface OpenApiMethod {

    /**
     * 接口 code，会出现在通信协议中， 要求全局唯一
     *
     * @return
     */
    public String code();


//    /**
//     * 接口名称
//     *
//     * @return
//     */
//    public String name();

    /**
     * 消息分组字段 <br/>
     * 仅针对回传类接口有效，当多条回传消息属于同一个 group 时， 会把这些消息当成一个分组，按优先级进行回传来保证消息的有顺性 <br/>
     * 一组内的任何一条消息回传失败，该组内的后续消息将会全部阻塞
     *
     * @return
     */
    public String group() default "";

//    /**
//     * 分组内的消息排序，数字越小，优先级越高
//     *
//     * @return
//     */
//    public int sort() default 0;

    /**
     * 是否为异步调用，默认为 false (开启异步调用时，会立即返回执行成功，并将请求信息放进队列中排队执行)
     *
     * @return
     */
    public boolean async() default false;

}
