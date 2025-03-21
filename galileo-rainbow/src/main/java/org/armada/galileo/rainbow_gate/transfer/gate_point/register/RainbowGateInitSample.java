package org.armada.galileo.rainbow_gate.transfer.gate_point.register;//package org.armada.galileo.rainbow_gate.transfer.gate_point.register;
//
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.web.proxy.ServletRegistrationBean;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.context.annotation.Bean;
//
//import org.armada.galileo.rainbow_gate.transfer.zookeeper.LocalServerAddressUtil;
//import org.armada.galileo.rainbow_gate.transfer.zookeeper.ZkCLient;
//
///**
// * Gate 端配置示例, 将此类代码拷至项目中 ， 加上 @Configuration 即可 <br/>
// * 将同时注册 GateServer GateClient
// * 
// * @author xiaobowang
// *
// */
//public class RainbowGateInitSample implements ApplicationContextAware {
//
//	@Bean
//	public LocalServerAddressUtil initLocalServerAddressUtil() {
//		return new LocalServerAddressUtil();
//	}
//
//	/**
//	 * 注册 GateServer Servlet
//	 * 
//	 * @return
//	 */
//	@Bean
//	public ServletRegistrationBean initRainbowClientServlet() {
//		return RegisterUtil.getGateClientServlet();
//	}
//
//	/**
//	 * 注册 http 直连模式 Servlet
//	 * 
//	 * @return
//	 */
//	@Bean
//	public ServletRegistrationBean initRainbowHttpDirectServlet() {
//		return RegisterUtil.getGateHttpDirectServlet();
//	}
//
//	/**
//	 * 初始化 GateClient、GateServer <br/>
//	 * 将 GateClient 地址写入 zookeeper <br/>
//	 * 监听 appServer 端服务地址
//	 */
//	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//		RegisterUtil.initGateClient();
//		RegisterUtil.listenAppServerAddress4GateServer();
//	}
//
//}
