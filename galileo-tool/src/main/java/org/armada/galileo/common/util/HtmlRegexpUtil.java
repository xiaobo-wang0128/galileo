package org.armada.galileo.common.util;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 标签处理类
 */
public class HtmlRegexpUtil {

	// 过滤所有以<开头以>结尾的标签
	private final static String regxpForHtml = "<.*?>";
	
	// 找出IMG标签
	private final static String regxpForImgTag = "<\\s*img\\s+([^>]*)\\s*>";
	
	// 找出IMG标签的SRC属性
	static Pattern p = Pattern.compile("<\\s*[Ff][Oo][Nn][Tt]\\s*>.*</[Ff][Oo][Nn][Tt]>");

	private String encodeTitle(String input) {
		Matcher m = p.matcher(input);
		String title = "";
		if (m.find()) {
			title = m.group();
			title = title.substring(title.indexOf(">") + 1, title.lastIndexOf("<"));
		}
		return title;
	}

	static Pattern pKeyword = Pattern.compile("<[Mm][Ee][Tt][Aa]\\s+[Nn][Aa][Mm][Ee]=\"[Kk][Ee][Yy][Ww][Oo][Rr][Dd][Ss]\" [Cc][Oo][Nn][Tt][Ee][Nn][Tt]=\".*\"\\s*/>");
	static Pattern pContent = Pattern.compile("[Cc][Oo][Nn][Tt][Ee][Nn][Tt]\\s*=\\s*\".*\"");

	private String encodeKeyword(String input) {
		String content = "";
		Matcher mm = pKeyword.matcher(input);
		String big = "";
		if (mm.find()) {
			big = mm.group();
			Matcher m2 = pContent.matcher(big);
			if (m2.find()) {
				content = m2.group();
				content = content.substring(content.indexOf("\"") + 1, content.lastIndexOf("\""));
			}
		}
		return content;
	}

	// public static void main(String[] args) throws Exception {
	//
	// String regex = "<meta\\s+name=[\"']keywords[\"']\\s*?content\\s*?=\\s*?[\"'].*?[\"']\\s*?/?>";
	//
	// BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("C:/Documents and Settings/Administrator/桌面/a.html"))));
	// String s = null;
	// String input = "";
	// while ((s = br.readLine()) != null) {
	// input += s + "\n";
	// }
	// br.close();
	//
	// Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
	//
	// Matcher mm = p.matcher(input);
	// String big = "";
	// if (mm.find()) {
	// big = mm.group();
	//
	// System.out.println(big);
	// System.out.println("***************************");
	//
	// Pattern p2 = Pattern.compile("content\\s*?=\\s*?[\"'].*?[\"']", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
	// Matcher m2 = p2.matcher(big);
	//
	// if (m2.find()) {
	// String content = m2.group();
	// System.out.println("1:" + content);
	// content = content.substring(content.indexOf("content") + "content".length() + 1);
	// char[] c = content.trim().toCharArray();
	// s = "";
	// for (int i = 0; i < c.length; i++) {
	// if (c[i] == '=' || c[i] == '\"' || c[i] == '\'') {
	// continue;
	// }
	// s += c[i];
	// }
	// System.out.println(s);
	//
	// } else {
	// System.out.println("not matchered");
	// }
	//
	// System.out.println("#");
	// }
	//
	// }

	/**
	 * 基本功能：替换标记以正常显示
	 */
	public static String replaceTag(String input) {
		if (!hasSpecialChars(input)) {
			return input;
		}
		StringBuffer filtered = new StringBuffer(input.length());
		char c;
		for (int i = 0; i <= input.length() - 1; i++) {
			c = input.charAt(i);
			switch (c) {
			case '<':
				filtered.append("&lt;");
				break;
			case '>':
				filtered.append("&gt;");
				break;
			case '"':
				filtered.append("&quot;");
				break;
			case '&':
				filtered.append("&amp;");
				break;
			default:
				filtered.append(c);
			}

		}
		return (filtered.toString());
	}

	/**
	 * 基本功能：判断标记是否存在
	 * <p>
	 * 
	 * @param input
	 * @return boolean
	 */
	public static boolean hasSpecialChars(String input) {
		boolean flag = false;
		if ((input != null) && (input.length() > 0)) {
			char c;
			for (int i = 0; i <= input.length() - 1; i++) {
				c = input.charAt(i);
				switch (c) {
				case '>':
					flag = true;
					break;
				case '<':
					flag = true;
					break;
				case '"':
					flag = true;
					break;
				case '&':
					flag = true;
					break;
				}
			}
		}
		return flag;
	}

	/**
	 * 基本功能：过滤所有以"<"开头,以">"结尾的标签
	 * <p>
	 * 
	 * @param str
	 * @return String
	 */
	public static String filterHtml(String str) {
		if (str == null) {
			return "";
		}
		Pattern pattern = Pattern.compile(regxpForHtml);
		Matcher matcher = pattern.matcher(str);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, "");
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 基本功能：过滤指定标签
	 * <p>
	 * 
	 * @param str
	 * @param tag
	 *            指定标签
	 * @return String
	 */
	public static String fiterHtmlTag(String str, String tag) {
		String regxp = "<\\s*" + tag + "\\s+([^>]*)\\s*>";
		Pattern pattern = Pattern.compile(regxp);
		Matcher matcher = pattern.matcher(str);
		StringBuffer sb = new StringBuffer();
		boolean result1 = matcher.find();
		while (result1) {
			matcher.appendReplacement(sb, "");
			result1 = matcher.find();
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 基本功能：替换指定的标签
	 * <p>
	 * 
	 * @param str
	 * @param beforeTag
	 *            要替换的标签
	 * @param tagAttrib
	 *            要替换的标签属性值
	 * @param startTag
	 *            新标签开始标记
	 * @param endTag
	 *            新标签结束标记
	 * @return String
	 * @如：替换img标签的src属性值为[img]属性值[/img]
	 */
	public static String replaceHtmlTag(String str, String beforeTag, String tagAttrib, String startTag, String endTag) {
		String regxpForTag = "<\\s*" + beforeTag + "\\s+([^>]*)\\s*>";
		String regxpForTagAttrib = tagAttrib + "=\"([^\"]+)\"";
		Pattern patternForTag = Pattern.compile(regxpForTag);
		Pattern patternForAttrib = Pattern.compile(regxpForTagAttrib);
		Matcher matcherForTag = patternForTag.matcher(str);
		StringBuffer sb = new StringBuffer();
		boolean result = matcherForTag.find();
		while (result) {
			StringBuffer sbreplace = new StringBuffer();
			Matcher matcherForAttrib = patternForAttrib.matcher(matcherForTag.group(1));
			if (matcherForAttrib.find()) {
				matcherForAttrib.appendReplacement(sbreplace, startTag + matcherForAttrib.group(1) + endTag);
			}
			matcherForTag.appendReplacement(sb, sbreplace.toString());
			result = matcherForTag.find();
		}
		matcherForTag.appendTail(sb);
		return sb.toString();
	}
}
