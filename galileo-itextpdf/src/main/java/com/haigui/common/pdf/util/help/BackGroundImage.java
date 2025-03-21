package com.haigui.common.pdf.util.help;

public class BackGroundImage {

	/**
	 * 图片字节流
	 */
	private byte[] imageBufs;

	/**
	 * 坐标x
	 */
	private int positionX;

	/**
	 * 坐标y
	 */
	private int positionY;

	public byte[] getImageBufs() {
		return imageBufs;
	}

	public void setImageBufs(byte[] imageBufs) {
		this.imageBufs = imageBufs;
	}

	public int getPositionX() {
		return positionX;
	}

	public void setPositionX(int positionX) {
		this.positionX = positionX;
	}

	public int getPositionY() {
		return positionY;
	}

	public void setPositionY(int positionY) {
		this.positionY = positionY;
	}

}
