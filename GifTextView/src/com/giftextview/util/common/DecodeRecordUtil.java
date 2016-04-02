package com.giftextview.util.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.giftextview.global.GifTextViewGlobal;

/**
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年03月30日
 * 
 *      记得给我个star哦~
 * 
 */
public class DecodeRecordUtil {

	private SharedPreferences preferences = null;

	public DecodeRecordUtil(Context context) {
		preferences = context.getSharedPreferences(
				GifTextViewGlobal.PREFERENCES_RECORD_NAME_DECODE,
				Context.MODE_PRIVATE);
	}

	public void set(String key, String value) {
		Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public String get(String key) {
		return preferences.getString(key, null);
	}

	public void remove(String key) {
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
