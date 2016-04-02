package com.giftextview.util.common;

import android.content.Context;
import android.content.res.Resources;

/**
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年03月30日
 * 
 *      记得给我个star哦~
 * 
 */
public class ResUtil {

	private static ResUtil mInstance = null;
	private String mPackageName = null;
	private Resources mResources = null;

	private ResUtil(Context context) {
		mPackageName = context.getPackageName();
		mResources = context.getResources();
	}

	public static ResUtil getInstance(Context context) {
		if (mInstance == null)
			mInstance = new ResUtil(context);
		return mInstance;
	}

	public void clear() {
		mResources = null;
		mInstance = null;
	}

	public int getIdFromDrawable(String name) {
		return mResources.getIdentifier(name, "drawable", mPackageName);
	}

	public int getIdFromString(String name) {
		return mResources.getIdentifier(name, "string", mPackageName);
	}

	public int getIdFromLayout(String name) {
		return mResources.getIdentifier(name, "layout", mPackageName);
	}

	public int getIdFromAnim(String name) {
		return mResources.getIdentifier(name, "anim", mPackageName);
	}

	public int getIdFromColor(String name) {
		return mResources.getIdentifier(name, "color", mPackageName);
	}

	public int getIdFromDimen(String name) {
		return mResources.getIdentifier(name, "dimen", mPackageName);
	}

	public int getIdFromAttr(String name) {
		return mResources.getIdentifier(name, "attr", mPackageName);
	}

	public int getIdFromStyleable(String name) {
		return mResources.getIdentifier(name, "styleable", mPackageName);
	}

	public int getIdFromView(String name) {
		return mResources.getIdentifier(name, "id", mPackageName);
	}

}
