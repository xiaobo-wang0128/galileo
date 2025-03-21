package org.armada.galileo.rainbow_gate.transfer.discovery.zk;

import java.util.List;

public interface ChangeListener {

	public void afterChange(String className, List<String> newChildPaths);
}
