package com.giftextview.cache;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年03月30日
 * 
 *      记得给我个star哦~
 * 
 */
public class BitmapCache extends LruCache<String, Bitmap> {

	private boolean mIsRecycle = false;

	public BitmapCache(int maxSize) {
		super(maxSize);
	}

	@Override
	protected void entryRemoved(boolean evicted, String key, Bitmap oldValue,
			Bitmap newValue) {
		if (evicted && mIsRecycle)
			oldValue.recycle();
	}

	@Override
	protected int sizeOf(String key, Bitmap value) {
		return value.getByteCount();
	}

	public void recycle() {
		mIsRecycle = true;
		evictAll();
	}

}
