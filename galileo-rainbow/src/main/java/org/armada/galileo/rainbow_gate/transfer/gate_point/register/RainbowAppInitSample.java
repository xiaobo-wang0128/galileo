package org.armada.galileo.rainbow_gate.transfer.gate_point.register;// package org.armada.galileo.rainbow_gate.transfer.gate_point.register;
//
// import org.springframework.beans.BeansException;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.web.proxy.ServletRegistrationBean;
// import org.springframework.context.ApplicationContext;
// import org.springframework.context.ApplicationContextAware;
// import org.springframework.context.annotation.Bean;
//
// import org.armada.galileo.rainbow_gate.transfer.gate_point.app_server.RainbowRpcInit;
// import org.armada.galileo.rainbow_gate.transfer.zookeeper.LocalServerAddressUtil;
// import org.armada.galileo.rainbow_gate.transfer.zookeeper.ZkCLient;
//
/// **
// * App 端配置示例, 将此类代码拷至项目中 ， 加上 @Configuration 即可 <br/>
// * 将同时注册 AppServer AppClient
// *
// * @author xiaobowang
// *
// */
// public class RainbowAppInitSample implements ApplicationContextAware {
//
// @Bean
// public LocalServerAddressUtil initLocalServerAddressUtil() {
// return new LocalServerAddressUtil();
// }
//
// /**
// * 注册 AppServer端 Servlet
// *
// * @return
// */
// @Bean
// public ServletRegistrationBean initAppServerServlet() {
// return RegisterUtil.getAppServerServlet();
// }
//
// /**
// * 注册 appServer端 生产者接口信息至 zookeeper
// *
// * @return
// */
// @Bean
// public RainbowRpcInit getAppServiceRegister() {
// return new RainbowRpcInit();
// }
//
// /**
// * 监听 rainbow-client 地址、 app-server 地址
// */
// public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
// RegisterUtil.listenAppServerAddress4AppClient();
// RegisterUtil.listenGateClientAddress();
// }
// }
