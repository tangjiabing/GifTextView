package com.giftextview.util.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import android.content.Context;

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
public class DiskCacheUtil {

	private static final String SPLIT_OF_LINE = ",";
	private static final String ENTER_STR = "\r\n";
	private static final int MAX_CACHE_SIZE = 30 * 1024 * 1024;
	private static DiskCacheUtil mInstance = null;
	private LinkedHashMap<String, Integer> mCacheMap = null;
	private int mCurrentTotalSize = 0;

	private DiskCacheUtil() {
	}

	// ********************************************************************
	// 公有方法

	public static DiskCacheUtil open(Context context) {
		if (mInstance == null) {
			checkCacheDirSize(context);
			mInstance = new DiskCacheUtil();
			mInstance.read(getDiskCacheDir(context).getAbsolutePath()
					+ File.separator
					+ GifTextViewGlobal.DISK_RECORD_FILE_NAME_DOWNLOAD);
		}
		return mInstance;
	}

	public static void close(Context context) {
		if (mInstance != null) {
			mInstance.write(getDiskCacheDir(context).getAbsolutePath()
					+ File.separator
					+ GifTextViewGlobal.DISK_RECORD_FILE_NAME_DOWNLOAD);
			mInstance = null;
		}
	}

	public static File getDiskCacheDir(Context context) {
		return DiskCacheDirUtil.getDiskCacheDir(context,
				GifTextViewGlobal.DISK_CACHE_DIR_NAME_DOWNLOAD);
	}

	public static void clearDiskCache(Context context) {
		DiskCacheDirUtil.clearDiskCache(context,
				GifTextViewGlobal.DISK_CACHE_DIR_NAME_DOWNLOAD);
		if (mInstance != null)
			mInstance.clearCacheMap();
	}

	public void put(String path) {
		File file = new File(path);
		if (file.exists()) {
			int size = (int) file.length();
			if (mCurrentTotalSize + size > MAX_CACHE_SIZE) {
				Iterator<Entry<String, Integer>> iterator = mCacheMap
						.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, Integer> entry = iterator.next();
					File f = new File(entry.getKey());
					f.delete();
					iterator.remove();
					mCurrentTotalSize = mCurrentTotalSize - entry.getValue();
					if (mCurrentTotalSize + size <= MAX_CACHE_SIZE)
						break;
				}
			}
			mCacheMap.put(path, size);
			mCurrentTotalSize = mCurrentTotalSize + size;
		}
	}

	// ********************************************************************
	// 私有方法

	private static void checkCacheDirSize(Context context) {
		File cacheDir = getDiskCacheDir(context);
		File recordFile = new File(cacheDir,
				GifTextViewGlobal.DISK_RECORD_FILE_NAME_DOWNLOAD);
		int offLength = 0;
		if (recordFile.exists())
			offLength = (int) recordFile.length();
		if (getDirTotalSize(cacheDir) - offLength > MAX_CACHE_SIZE)
			clearDiskCache(context);
	}

	private static int getDirTotalSize(File file) {
		int totalSize = (int) file.length();
		if (file.isDirectory()) {
			File[] list = file.listFiles();
			for (File f : list)
				totalSize = totalSize + getDirTotalSize(f);
		}
		return totalSize;
	}

	private boolean read(String filePath) {
		mCacheMap = new LinkedHashMap<String, Integer>(10, 0.75f, true);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] array = line.split(SPLIT_OF_LINE);
				int size = Integer.valueOf(array[1]);
				mCacheMap.put(array[0], size);
				mCurrentTotalSize = mCurrentTotalSize + size;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean write(String filePath) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(filePath);
			Iterator<Entry<String, Integer>> iterator = mCacheMap.entrySet()
					.iterator();
			while (iterator.hasNext()) {
				Entry<String, Integer> entry = iterator.next();
				writer.write(entry.getKey());
				writer.write(SPLIT_OF_LINE);
				writer.write(entry.getValue() + "");
				writer.write(ENTER_STR);
				iterator.remove();
			}
			writer.flush();
			mCurrentTotalSize = 0;
			mCacheMap = null;
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void clearCacheMap() {
		mCacheMap.clear();
		mCurrentTotalSize = 0;
	}

}
