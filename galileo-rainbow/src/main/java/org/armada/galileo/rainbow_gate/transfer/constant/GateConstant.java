package org.armada.galileo.rainbow_gate.transfer.constant;

import org.armada.galileo.rainbow_gate.transfer.domain.protocol.AppRequestDomain;
import org.armada.galileo.rainbow_gate.transfer.domain.protocol.RainbowRequestType;
import org.armada.galileo.rainbow_gate.transfer.gate_codec.GateCodecUtil;

public class GateConstant {

	/**
	 * 通用密钥（临时）
	 */
	public static final String appSecret = "haigui_rainbow_gate";

	/**
	 * app-client 端最大等待时间 ms
	 */
	public static final int appClientMaxWaitTime = 1800000;

	/**
	 * app-client 端循环等待结果响应的时间 ms
	 */
	public static final int appClientSleepTime = 100;

	/**
	 * gate-client 端缓存响应结果的最长时间（时间过长响应结果没有返回给 app-client， 会导致内存溢出）
	 */
	public static final int gateClientTimeoutTime = 5000;

	/**
	 * 网络心跳检测失败超时时间
	 */
	public static final long pingTimeoutMill = 15000;

	/**
	 * 网终心跳检测间隔时间
	 */
	public static final long heartbeatSleepTime = 60000;

	public static final byte[] pingBytes = GateCodecUtil.encodeRequest(new AppRequestDomain(RainbowRequestType.Ping));

	public static final byte[] pingDoubleBytes = GateCodecUtil.encodeRequest(new AppRequestDomain(RainbowRequestType.Ping_Double));;
}
