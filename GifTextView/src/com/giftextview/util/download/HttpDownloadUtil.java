package com.giftextview.util.download;

import java.io.File;

import android.content.Context;

import com.giftextview.util.common.MD5Util;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

/**
 * 整个系统中，正在下载的线程最多有3个，多余的下载任务会放入栈中，等待依次执行下载。
 * 当磁盘容量满时，下载不会取消，但一直处于停止状态，直到磁盘有多余的空间存储，线程会自动恢复下载
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年03月30日
 * 
 *      记得给我个star哦~
 * 
 */
public class HttpDownloadUtil {

	/** 无网络 */
	public static final int FLAG_NO_NETWORK = -10;
	/** 下载失败，并且不存在缓存文件 */
	public static final int FLAG_FAIL_NOT_EXIST = -1;
	/** 下载失败，存在缓存文件，但不知道文件是否完整 */
	public static final int FLAG_FAIL_EXIST = 0;
	/** 下载成功 */
	public static final int FLAG_SUCCESS = 1;
	/** 默认的连接超时时间 */
	private static final int DEFAULT_CONNECT_TIMEOUT = 10 * 1000;

	private HttpUtils mHttpUtils;
	private HttpHandler mHttpHandler;
	private DownloadRecordUtil mRecordUtil;
	private Context mContext;
	private boolean mIsCallback_onCancelled;
	private boolean mIsFinished_download;
	private boolean mIsRecord_totalSize;

	public HttpDownloadUtil(Context context) {
		this(context, DEFAULT_CONNECT_TIMEOUT);
	}

	/**
	 * 
	 * @param context
	 * @param connTimeout
	 *            连接超时时间，单位为毫秒
	 */
	public HttpDownloadUtil(Context context, int connTimeout) {
		mContext = context;
		mHttpUtils = new HttpUtils(connTimeout);
		mRecordUtil = new DownloadRecordUtil(mContext);
	}

	/**
	 * 下载文件
	 * 
	 * @param url
	 *            文件的下载网址
	 * @param localDir
	 *            保存文件的本地目录
	 * @param httpCallback
	 *            监听下载过程的接口对象
	 */
	public void downloadFile(final String url, String localDir,
			final HttpDownloadCallback httpCallback) {

		if (mHttpHandler != null && mHttpHandler.isCancelled() == false) {
			mIsCallback_onCancelled = false;
			mHttpHandler.cancel();
		}

		String fileName = MD5Util.getMD5(url) + "_"
				+ url.substring(url.lastIndexOf("/") + 1, url.length());
		final String localPath = localDir + File.separator + fileName;

		mHttpHandler = mHttpUtils.download(url, localPath, true, // 为true的话，如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将重新下载。
				false, // 为true的话，如果从请求返回信息中获取到文件名，下载完成后自动重命名。
				new RequestCallBack<File>() {

					@Override
					public void onSuccess(ResponseInfo<File> info) {
						mIsFinished_download = true;
						File file = new File(localPath);
						if (file.exists() == false) // 在下载过程中，如果本地文件被删除了，还是会继续下载，但磁盘中不存在该文件
							httpCallback.onResult(FLAG_SUCCESS, null, false);
						else
							httpCallback.onResult(FLAG_SUCCESS, file, true);
					}

					@Override
					public void onFailure(HttpException exception, String msg) { // 当下载过程中手机突然无网络、服务端异常导致连接断开等，也是回调此方法
						mIsFinished_download = true;
						File file = new File(localPath);
						boolean isExistFile = file.exists();
						long fileSize = 0;
						if (isExistFile)
							fileSize = file.length();
						long totalSize = mRecordUtil.get(url);
						boolean isFileWhole = false;
						if (fileSize == totalSize)
							isFileWhole = true;
						if (NetworkUtil.getNetworkState(mContext) == NetworkUtil.STATE_NO_NETWORK) { // 无网络
							if (isExistFile == false)
								httpCallback.onResult(FLAG_NO_NETWORK, null,
										false);
							else
								httpCallback.onResult(FLAG_NO_NETWORK, file,
										isFileWhole);
						} else { // 下载失败（服务端异常导致连接断开）
							if (isExistFile == false)
								httpCallback.onResult(FLAG_FAIL_NOT_EXIST,
										null, false);
							else
								httpCallback.onResult(FLAG_FAIL_EXIST, file,
										isFileWhole);
						}
					}

					@Override
					public void onStart() {
						mIsFinished_download = false;
						mIsRecord_totalSize = false;
						httpCallback.onStart(localPath);
					}

					@Override
					public void onLoading(long total, long current,
							boolean isUploading) {
						if (mIsRecord_totalSize == false) {
							mRecordUtil.set(url, total);
							mIsRecord_totalSize = true;
						}
						httpCallback.onDownloading(total, current);
					}

					@Override
					public void onCancelled() {
						mIsFinished_download = true;
						if (mIsCallback_onCancelled == true)
							httpCallback.onCancelled();
					}

				});

	}

	/**
	 * 取消下载
	 */
	public void cancel() {
		if (mHttpHandler != null && mHttpHandler.isCancelled() == false) {
			if (mIsFinished_download == false)
				mIsCallback_onCancelled = true;
			else
				mIsCallback_onCancelled = false;
			mHttpHandler.cancel(); // 取消下载
		}
	}

	/**
	 * 清空对网络文件的大小的记录
	 */
	public void clearRecord() {
		mRecordUtil.clear();
	}

	/**
	 * 移除对指定网络文件的大小的记录
	 * 
	 * @param url
	 *            网络文件的地址
	 */
	public void removeRecord(String url) {
		mRecordUtil.remove(url);
	}

	// ****************************************************************************************
	// 自定义的类

	public interface HttpDownloadCallback {

		/**
		 * 监听文件下载结果，当下载过程中手机突然无网络、服务端异常导致连接断开等，也是回调此方法
		 * 
		 * @param flag
		 *            FLAG_NO_NETWORK表示无网络，但会查找本地磁盘是否存在该文件（可能不完整）；
		 *            FLAG_FAIL_NOT_EXIST和FLAG_FAIL_EXIST均表示下载失败，
		 *            为FLAG_FAIL_EXIST时本地磁盘存在该文件（可能不完整）；
		 *            FLAG_SUCCESS表示下载成功，但还需要判断本地是否存在该文件，如果存在，那它就是下载完整的
		 * @param file
		 *            本地的文件对象，若为null则表示本地磁盘无该文件
		 * @param isFileWhole
		 *            如果为false，则表示本地的文件还没有下载完整；为true，则表示本地的文件已经下载好了
		 */
		public void onResult(int flag, File file, boolean isFileWhole);

		/**
		 * 开始下载
		 * 
		 * @param localPath
		 *            下载文件的本地路径
		 */
		public void onStart(String localPath);

		/**
		 * 监听文件下载过程
		 * 
		 * @param total
		 *            下载文件的总长度
		 * @param current
		 *            当前已下载的长度
		 */
		public void onDownloading(long total, long current);

		/**
		 * 取消下载
		 */
		public void onCancelled();

	}

}
