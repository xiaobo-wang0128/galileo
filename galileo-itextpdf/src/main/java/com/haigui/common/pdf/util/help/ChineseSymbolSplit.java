package com.haigui.common.pdf.util.help;

import java.util.ArrayList;
import java.util.List;

/**
 * 不能放在行首的中文标点
 * 
 * @author Administrator
 *
 */
public class ChineseSymbolSplit {
	private static List<Character> chSymSplits;

	static {
		chSymSplits = new ArrayList<>();
		chSymSplits.add('，');
		chSymSplits.add('。');
		chSymSplits.add('！');
		chSymSplits.add('；');
		chSymSplits.add('？');
		chSymSplits.add('：');
		chSymSplits.add('、');
		chSymSplits.add(',');
		chSymSplits.add(';');

		chSymSplits.add('》');
		chSymSplits.add('>');
		chSymSplits.add('）');
		chSymSplits.add(')');
		chSymSplits.add('”');
		chSymSplits.add('’');
		
		chSymSplits.add('}');
		chSymSplits.add('】');

		/** 添加你所需的标点 ***/

	}

	public static boolean isIncludeChar(char srcChar) {
		return chSymSplits.contains(srcChar);
	}

}
