package com.giftextview.util.decode;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import com.giftextview.decode.GifDecoder;
import com.giftextview.decode.GifDecoder.BitmapProvider;
import com.giftextview.decode.GifHeader;
import com.giftextview.decode.GifHeaderParser;
import com.giftextview.util.bitmap.BitmapUtil;

/**
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年03月30日
 * 
 *      记得给我个star哦~
 * 
 */
public class GifDecodeUtil {

	private GifHeaderParser mHeaderParser;
	private GifDecoder mDecoder;
	private int[] mDelays;
	private int mFrameCount;
	private int mCacheSize;
	private boolean mIsDecodeSuccess;
	private boolean mIsRecycle;

	// ****************************************************************************
	// 公有方法

	public boolean decode(InputStream is) {
		return decode(is, -1, -1, 1);
	}

	public boolean decode(InputStream is, int targetWidth, int targetHeight) {
		return decode(is, targetWidth, targetHeight, 1);
	}

	public boolean decode(InputStream is, int sampleSize) {
		return decode(is, -1, -1, sampleSize);
	}

	public int[] getDelays() {
		mDelays = new int[mFrameCount];
		for (int i = 0; i < mFrameCount; i++)
			mDelays[i] = mDecoder.getDelay(i);
		return mDelays;
	}

	public Bitmap getNextFrame() {
		mDecoder.advance();
		return mDecoder.getNextFrame();
	}

	public Bitmap getNextFrame(int index) {
		resetFrameIndex();
		for (int i = -1; i < index; i++)
			mDecoder.advance();
		return mDecoder.getNextFrame();
	}

	public int getCurrentFrameIndex() {
		return mDecoder.getCurrentFrameIndex();
	}

	public void resetFrameIndex() {
		mDecoder.resetFrameIndex();
	}

	public int getFrameCount() {
		return mFrameCount;
	}

	public int getCacheSize() {
		return mCacheSize;
	}

	public boolean isDecodeSuccess() {
		return mIsDecodeSuccess;
	}

	public boolean isRecycle() {
		return mIsRecycle;
	}

	public void clear() {
		if (mHeaderParser != null) {
			mHeaderParser.clear();
			mHeaderParser = null;
		}
		if (mDecoder != null) {
			mDecoder.clear();
			mDecoder = null;
		}
		mDelays = null;
		mIsRecycle = true;
		mIsDecodeSuccess = false;
	}

	// ****************************************************************************
	// 私有方法

	private boolean decode(InputStream is, int targetWidth, int targetHeight,
			int sampleSize) {
		if (mIsDecodeSuccess == true)
			reset();
		boolean result = false;
		byte[] data = inputStreamToBytes(is);
		if (data == null)
			result = false;
		else {
			ByteBuffer byteBuffer = ByteBuffer.wrap(data);
			mHeaderParser = new GifHeaderParser();
			mHeaderParser.setData(byteBuffer);
			GifHeader header = mHeaderParser.parseHeader();
			if (header.getNumFrames() < 0
					|| header.getStatus() != GifDecoder.STATUS_OK)
				result = false;
			else {
				GifBitmapProvider provider = new GifBitmapProvider();
				if (sampleSize <= 0)
					sampleSize = 1;
				if (targetWidth > 0 && targetHeight > 0)
					sampleSize = getSampleSize(header, targetWidth,
							targetHeight);
				mDecoder = new GifDecoder(provider, header, byteBuffer,
						sampleSize);
				mFrameCount = mDecoder.getFrameCount();
				setCacheSize(data);
				result = true;
			}
		}
		mIsDecodeSuccess = result;
		if (result == false)
			reset();
		return result;
	}

	private byte[] inputStreamToBytes(InputStream is) {
		ByteArrayOutputStream out = null;
		try {
			int bufferSize = 16384;
			out = new ByteArrayOutputStream(bufferSize);
			int len = 0;
			byte[] data = new byte[bufferSize];
			while ((len = is.read(data)) != -1)
				out.write(data, 0, len);
			out.flush();
			data = out.toByteArray();
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (out != null)
					out.close();
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private int getSampleSize(GifHeader header, int targetWidth,
			int targetHeight) {
		int exactSampleSize = Math.min(header.getHeight() / targetHeight,
				header.getWidth() / targetWidth);
		int powerOfTwoSampleSize = exactSampleSize == 0 ? 0 : Integer
				.highestOneBit(exactSampleSize);
		// Although functionally equivalent to 0 for BitmapFactory, 1 is a safer
		// default for our code than 0.
		int sampleSize = Math.max(1, powerOfTwoSampleSize);
		return sampleSize;
	}

	private void setCacheSize(byte[] data) {
		mCacheSize = data.length + 16384 + 255;
	}

	private void reset() {
		clear();
		mFrameCount = 0;
		mCacheSize = 0;
		mIsRecycle = false;
	}

	// ****************************************************************************
	// 自定义的类

	private class GifBitmapProvider implements BitmapProvider {

		@Override
		public Bitmap obtain(int width, int height, Config config) {
			config = config == Config.ARGB_8888 ? Config.ARGB_4444
					: Config.RGB_565;
			return BitmapUtil.getBitmap(config, width, height);
		}

		@Override
		public void release(Bitmap bitmap) {
		}

		@Override
		public byte[] obtainByteArray(int size) {
			return new byte[size];
		}

		@Override
		public void release(byte[] bytes) {
		}
	}

}