package org.armada.galileo.rainbow_gate.transfer.discovery.zk;

public enum ZkType {

	AppService("/armada_galileo_rainbow/app_service"),

	GateClient("/armada_galileo_rainbow/gate_client"),

	AppDeploy("/armada_galileo_rainbow/app_deploy")

	;

	private String path;

	private ZkType(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

}
