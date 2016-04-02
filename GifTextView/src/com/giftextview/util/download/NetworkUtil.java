package com.giftextview.util.download;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年03月30日
 * 
 *      记得给我个star哦~
 * 
 */
public class NetworkUtil {

	/** 网络断开 */
	public static final int STATE_NO_NETWORK = 0;
	/** wifi网络 */
	public static final int STATE_WIFI = 1;
	/** 2G、3G、4G网络 */
	public static final int STATE_MOBILE = 2;

	/**
	 * 获取手机的网络连接状态。
	 * 
	 * @return 
	 *         STATE_NO_NETWORK（网络断开），STATE_WIFI（wifi网络），STATE_MOBILE（2G、3G、4G网络）
	 */
	public static int getNetworkState(Context context) {
		ConnectivityManager conMan = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = conMan.getActiveNetworkInfo();
		if (info != null && info.isConnected() && info.isAvailable()) { // 网络可用
			String type = info.getTypeName();
			if (type.equalsIgnoreCase("wifi"))
				return STATE_WIFI;
			else if (type.equalsIgnoreCase("mobile"))
				return STATE_MOBILE;
		}
		// 网络不可用
		return STATE_NO_NETWORK;
	}

}
