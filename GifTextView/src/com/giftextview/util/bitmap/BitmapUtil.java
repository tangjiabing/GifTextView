package com.giftextview.util.bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;

/**
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年03月30日
 * 
 *      记得给我个star哦~
 * 
 */
public class BitmapUtil {

	public static Bitmap getBitmap(String localPath, int targetWidth,
			int targetHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(localPath, options);
		options.inJustDecodeBounds = false;
		options.inSampleSize = getSampleSize(options.outWidth,
				options.outHeight, targetWidth, targetHeight);
		options.inMutable = true;
		return BitmapFactory.decodeFile(localPath, options);
	}

	public static Bitmap getBitmap(Context context, int resId, int targetWidth,
			int targetHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(context.getResources(), resId, options);
		options.inJustDecodeBounds = false;
		options.inSampleSize = getSampleSize(options.outWidth,
				options.outHeight, targetWidth, targetHeight);
		options.inMutable = true;
		return BitmapFactory.decodeResource(context.getResources(), resId,
				options);
	}

	public static Bitmap getBitmap(InputStream is) {
		return BitmapFactory.decodeStream(is);
	}

	public static Bitmap getBitmap(Bitmap.Config config, int width, int height) {
		return Bitmap.createBitmap(width, height, config);
	}

	public static Bitmap getBitmap(Bitmap srcBitmap, int targetWidth,
			int targetHeight) {
		int srcWidth = srcBitmap.getWidth();
		int srcHeight = srcBitmap.getHeight();
		int sampleSize = getSampleSize(srcWidth, srcHeight, targetWidth,
				targetHeight);
		if (sampleSize == 1) {
			if (srcBitmap.isMutable())
				return srcBitmap;
			else {
				Bitmap dstBitmap = srcBitmap.copy(srcBitmap.getConfig(), true);
				srcBitmap.recycle();
				return dstBitmap;
			}
		} else {
			int width = srcWidth / sampleSize;
			int height = srcHeight / sampleSize;
			// 当scale==1的时候，createScaledBitmap不会创建新的Bitmap，而是使用原来的
			Bitmap bitmap = Bitmap.createScaledBitmap(srcBitmap, width, height,
					true);
			srcBitmap.recycle();
			if (!bitmap.isMutable()) {
				Bitmap dstBitmap = bitmap.copy(bitmap.getConfig(), true);
				bitmap.recycle();
				return dstBitmap;
			} else
				return bitmap;
		}
	}

	public static Bitmap convertToMutable(Bitmap srcBitmap,
			String cacheDirPath, String tempFileName) {
		try {
			// this is the file going to use temporally to save the bytes.
			// This file will not be a image, it will store the raw image data.
			int index = tempFileName.lastIndexOf(".");
			if (index != -1)
				tempFileName = tempFileName.substring(0, index);
			File file = new File(cacheDirPath + File.separator + tempFileName
					+ ".tmp");

			// Open an RandomAccessFile
			// Make sure you have added uses-permission
			// android:name="android.permission.WRITE_EXTERNAL_STORAGE"
			// into AndroidManifest.xml file
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

			// get the width and height of the source bitmap.
			int width = srcBitmap.getWidth();
			int height = srcBitmap.getHeight();
			Config type = srcBitmap.getConfig();

			// Copy the byte to the file
			// Assume source bitmap loaded using options.inPreferredConfig =
			// Config.ARGB_8888;
			FileChannel channel = randomAccessFile.getChannel();
			MappedByteBuffer map = channel.map(MapMode.READ_WRITE, 0,
					srcBitmap.getRowBytes() * height);
			srcBitmap.copyPixelsToBuffer(map);
			// recycle the source bitmap, this will be no longer used.
			srcBitmap.recycle();
			System.gc();// try to force the bytes from the imgIn to be released

			// Create a new bitmap to load the bitmap again. Probably the memory
			// will be available.
			srcBitmap = Bitmap.createBitmap(width, height, type);
			map.position(0);
			// load it back from temporary
			srcBitmap.copyPixelsFromBuffer(map);
			// close the temporary file and channel , then delete that also
			channel.close();
			randomAccessFile.close();

			// delete the temp file
			file.delete();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return srcBitmap;
	}

	// ***********************************************************************************
	// 私有方法

	private static int getSampleSize(int srcWidth, int srcHeight,
			int targetWidth, int targetHeight) {
		int sampleSize = 1;
		if (srcWidth > targetWidth || srcHeight > targetHeight) {
			while ((srcWidth / sampleSize) > targetWidth
					|| (srcHeight / sampleSize) > targetHeight)
				sampleSize *= 2;
		}
		return sampleSize;
	}

}
