package com.giftextview.util.cache;

import java.io.File;

import android.content.Context;
import android.os.Environment;

/**
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年03月30日
 * 
 *      记得给我个star哦~
 * 
 */
public class DiskCacheDirUtil {

	public static File getDiskCacheDir(Context context, String dirName) {
		String cachePath = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable())
			cachePath = context.getExternalCacheDir().getPath();
		else
			cachePath = context.getCacheDir().getPath();
		File file = new File(cachePath + File.separator + dirName);
		if (!file.exists())
			file.mkdirs();
		return file;
	}

	public static void clearDiskCache(Context context, String dirName) {
		File file = getDiskCacheDir(context, dirName);
		clearFiles(file.listFiles());
		file.delete();
	}

	private static void clearFiles(File[] list) {
		for (File file : list) {
			if (file.isFile())
				file.delete();
			else if (file.isDirectory()) {
				File[] children = file.listFiles();
				if (children.length == 0)
					file.delete();
				else
					clearFiles(children);
			}
		}
	}

}
