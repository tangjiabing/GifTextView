package com.giftextview.util.cache;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;

import com.giftextview.cache.DiskLruCache;
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
public class BitmapDiskCacheUtil {

	private static BitmapDiskCacheUtil mInstance = null;
	private DiskLruCache mCache = null;

	private BitmapDiskCacheUtil(Context context) {
		int maxSize = 70 * 1024 * 1024;
		File cacheDir = DiskCacheDirUtil.getDiskCacheDir(context,
				GifTextViewGlobal.DISK_CACHE_DIR_NAME_BITMAP);
		int appVersion = getAppVersion(context);
		try {
			// 参数1（directory）：缓存目录
			// 参数2（appVersion）：当前应用程序的版本号
			// 参数3（valueCount）：同一个key可以对应多少个缓存文件
			// 参数4（maxSize）：最多可以缓存多少字节的数据
			mCache = DiskLruCache.open(cacheDir, appVersion, 1, maxSize);
			// 注意：每当版本号改变，缓存路径下存储的所有数据都会被清除掉，
			// 因为DiskLruCache认为当应用程序有版本更新的时候，所有的数据都应该从网上重新获取。
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// *******************************************************************************
	// 公有方法

	public synchronized static BitmapDiskCacheUtil getInstance(Context context) {
		if (mInstance == null)
			mInstance = new BitmapDiskCacheUtil(context);
		return mInstance;
	}

	public boolean put(String key, Bitmap bitmap) {
		boolean result = false;
		OutputStream outStream = null;
		try {
			DiskLruCache.Editor editor = null;
			synchronized (mCache) {
				if (mCache != null)
					editor = mCache.edit(key);
			}
			if (editor != null) {
				// 由于前面在设置valueCount的时候指定的是1，所以在这里index传0就可以了
				outStream = editor.newOutputStream(0);
				result = bitmap.compress(Bitmap.CompressFormat.PNG, 100,
						outStream);
				// 在写入操作执行完之后，还需要调用一下commit()方法进行提交才能使写入生效，
				// 调用abort()方法的话则表示放弃此次写入
				if (result == true)
					editor.commit();
				else
					editor.abort();
				// mCache.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (outStream != null)
					outStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public InputStream get(String key) {
		InputStream is = null;
		try {
			DiskLruCache.Snapshot snapShot = null;
			synchronized (mCache) {
				if (mCache != null)
					snapShot = mCache.get(key);
			}
			if (snapShot != null)
				is = snapShot.getInputStream(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return is;
	}

	public boolean remove(String key) {
		boolean result = false;
		try {
			synchronized (mCache) {
				if (mCache != null)
					result = mCache.remove(key);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 
	 * @return 当前缓存路径下所有缓存数据的总字节数，以byte为单位
	 */
	public long size() {
		synchronized (mCache) {
			if (mCache != null)
				return mCache.size();
		}
		return 0;
	}

	/**
	 * 将内存中的操作记录同步到日志文件（也就是journal文件）， 比较标准的做法就是在Activity的onPause()方法中去调用一次就可以了
	 */
	public boolean flush() {
		try {
			// 这个方法非常重要，因为DiskLruCache能够正常工作的前提就是要依赖于journal文件中的内容。
			// 其实并不是每次写入缓存都要调用一次flush()方法的，频繁地调用并不会带来任何好处，只会额外增加同步journal文件的时间。
			synchronized (mCache) {
				if (mCache != null) {
					mCache.flush();
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 将DiskLruCache关闭掉，之后就不能再调用DiskLruCache中任何操作缓存数据的方法了。
	 * 通常只应该在Activity的onDestroy()方法中去调用。必须确保所有线程都不再使用该类的其他方法时才能调用此方法
	 */
	public boolean close() {
		try {
			synchronized (mCache) {
				if (mCache != null) {
					mCache.flush();
					mCache.close(); // 和open()方法对应的一个方法
					mCache = null;
					mInstance = null;
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 关闭掉DiskLruCache，将所有的缓存数据全部删除。必须确保所有线程都不再使用该类的其他方法时才能调用此方法
	 */
	public boolean delete() {
		try {
			synchronized (mCache) {
				if (mCache != null) {
					mCache.delete(); // 内部调用了close()方法
					mCache = null;
					mInstance = null;
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// *******************************************************************************
	// 私有方法

	private int getAppVersion(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return info.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}

}
