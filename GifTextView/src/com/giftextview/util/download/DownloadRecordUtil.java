package com.giftextview.util.download;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.giftextview.global.GifTextViewGlobal;
import com.giftextview.util.common.MD5Util;

/**
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年03月30日
 * 
 *      记得给我个star哦~
 * 
 */
public class DownloadRecordUtil {

	private SharedPreferences preferences = null;

	public DownloadRecordUtil(Context context) {
		preferences = context.getSharedPreferences(
				GifTextViewGlobal.PREFERENCES_RECORD_NAME_DOWNLOAD,
				Context.MODE_PRIVATE);
	}

	public void set(String url, long totalSize) {
		String key = MD5Util.getMD5(url);
		Editor editor = preferences.edit();
		editor.putLong(key, totalSize);
		editor.commit();
	}

	public long get(String url) {
		String key = MD5Util.getMD5(url);
		return preferences.getLong(key, -1);
	}

	public void remove(String url) {
		String key = MD5Util.getMD5(url);
		Editor editor = preferences.edit();
		editor.remove(key);
		editor.commit();
	}

	public void clear() {
		Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
	}

}
