package com.giftextview.entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年03月30日
 * 
 *      记得给我个star哦~
 * 
 */
public class SpanEntity implements Serializable {

	public static final int SPAN_TYPE_LINK = 0x201;
	public static final int SPAN_TYPE_IMAGE = 0x202;
	public static final int SPAN_TYPE_GIF = 0x203;
	public static final int SPAN_TYPE_EMOJI = 0x204;
	public static final int SPAN_TYPE_ZIP = 0x205;

	public int spanType;
	public ArrayList<Integer> starts;
	public ArrayList<Integer> ends;
	public String url;
	public int frameCount;
	public int nextFrameIndex;
	public int[] delays;

}
