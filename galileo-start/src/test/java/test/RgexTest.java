package test;

import org.armada.galileo.common.util.CommonUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RgexTest {

	public static List<String> getMatchedStrs(String input, String regex) {
		if (input == null) {
			return null;
		}
		List<String> list = new ArrayList<String>();
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(input);
		while (m.find()) {
			String str = m.group();
			list.add(str.trim());
		}
		return list;
	}

	public static void main(String[] args) {

		String s = "a3bc";

		List<String> list = getMatchedStrs(s, "\\w{0,6}");

		if (list == null) {
			return;
		}

		for (String str : list) {
			System.out.println(str);
		}

		System.out.println(CommonUtil.format(new Date(System.currentTimeMillis() - 86400000), "yyyy-MM-dd HH:mm:ss"));
	}

}
