package com.haigui.common.pdf.util.location;

/**********************************************************************
 * <pre>
 * FILE : PositionRenderListener.java
 * CLASS : PositionRenderListener
 *
 * AUTHOR : caoxu-yiyang@qq.com
 *
 * FUNCTION : TODO
 *
 *
 *======================================================================
 * CHANGE HISTORY LOG
 *----------------------------------------------------------------------
 * MOD. NO.|   DATE   |   NAME  | REASON  | CHANGE REQ.
 *----------------------------------------------------------------------
 * 		    |2016年11月9日|caoxu-yiyang@qq.com| Created |
 * DESCRIPTION:
 * </pre>
 ***********************************************************************/

/**
 * pdf渲染监听,当找到渲染的文本时，得到文本的坐标x,y,w,h
 * 
 */
public class PositionRenderListener extends PositionRenderListenerAll {

	public PositionRenderListener(String findText) {
		super(findText);
	}

	@Override
	protected boolean shouldStop() {
		return !this.getResultList().isEmpty();
	}

	public ReplaceRegion getResult() {
		if (this.getResultList().isEmpty()) {
			return null;
		}
		return this.getResultList().get(0);
	}

}
