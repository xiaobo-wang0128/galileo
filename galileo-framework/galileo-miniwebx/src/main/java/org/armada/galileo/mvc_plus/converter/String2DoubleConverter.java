package org.armada.galileo.mvc_plus.converter;

import org.springframework.core.convert.converter.Converter;

public class String2DoubleConverter implements Converter<String, Double> {

	public Double convert(String arg0) {
		if (arg0 == null) {
			return null;
		}

		try {

			return Double.valueOf(arg0);
		} catch (Exception e) {
		}
		return null;
	}

}
