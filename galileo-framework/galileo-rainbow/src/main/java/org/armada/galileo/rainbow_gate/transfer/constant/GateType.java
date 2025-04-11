package org.armada.galileo.rainbow_gate.transfer.constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum GateType {

	/**
	 * http 直连
	 */
	http_direct("http直连"),

	/**
	 * http + ftp 前置机模式
	 */
	http_ftp_double("http + ftp 前置机模式"),

	/**
	 * 共享sftp
	 */
	sftp_share("共享sftp（内外网共用一台）"),

	/**
	 * 腾讯里约
	 */
	tencent_rio("腾讯里约"),

	/**
	 * 网闸单向 外网
	 */
	http_outer("http单向（外网）", false),

	/**
	 * 网闸单向 内网
	 */
	http_inner("http单向（内网）", false),

	/**
	 * 单向ftp
	 */
	ftp_single("单台ftp（读写目录分离）"),

	/**
	 * 单向sftp
	 */
	sftp_single("单向sftp（读写目录分离）"),

	/**
	 * 双向ftp
	 */
	ftp_double("双向ftp（光闸内部ftp）"),
	
	
	/**
	 * mysql数据库同步
	 */
	mysql_transfer("mysql数据库同步"),

	;
	private String name;

	/**
	 * 是否开启
	 */
	private boolean open;

	private GateType(String name, boolean open) {
		this.name = name;
		this.open = open;
	}

	private GateType(String name) {
		this.name = name;
		this.open = true;
	}

	public String getName() {
		return name;
	}

	public static List<Map<String, String>> getAllConfig() {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		for (GateType r : GateType.values()) {

			if (!r.open) {
				continue;
			}
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", r.toString());
			map.put("name", r.getName());

			list.add(map);
		}
		return list;
	}

}
