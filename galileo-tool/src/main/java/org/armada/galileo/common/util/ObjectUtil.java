package org.armada.galileo.common.util;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectUtil {

	private static Logger log = LoggerFactory.getLogger(ObjectUtil.class);

	public static byte[] serializeJdk(Object obj) {
		try {
			if (obj == null)
				throw new NullPointerException();

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(os);
			out.writeObject(obj);
			return os.toByteArray();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public static Object deserializeJdk(byte[] by) {
		try {
			if (by == null)
				return null;

			ByteArrayInputStream is = new ByteArrayInputStream(by);
			ObjectInputStream in = new ObjectInputStream(is);
			return in.readObject();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

	// public static byte[] serializeHessian(Object obj) throws IOException {
	// if (obj == null)
	// throw new NullPointerException();
	//
	// ByteArrayOutputStream os = new ByteArrayOutputStream();
	// HessianOutput ho = new HessianOutput(os);
	// ho.writeObject(obj);
	// return os.toByteArray();
	// }
	//
	// public static Object deserializeHessian(byte[] by) throws IOException {
	// if (by == null)
	// throw new NullPointerException();
	//
	// ByteArrayInputStream is = new ByteArrayInputStream(by);
	// HessianInput hi = new HessianInput(is);
	// return hi.readObject();
	// }
}
