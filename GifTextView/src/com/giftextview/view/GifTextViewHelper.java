package com.giftextview.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;

import com.giftextview.cache.BitmapCache;
import com.giftextview.entity.SameDownloadEntity;
import com.giftextview.entity.SpanEntity;
import com.giftextview.span.GifDrawableSpan;
import com.giftextview.span.LinkClickableSpan;
import com.giftextview.span.OnTextSpanClickListener;
import com.giftextview.util.bitmap.BitmapUtil;
import com.giftextview.util.cache.BitmapDiskCacheUtil;
import com.giftextview.util.cache.DiskCacheUtil;
import com.giftextview.util.common.DecodeRecordUtil;
import com.giftextview.util.common.MD5Util;
import com.giftextview.util.common.ResUtil;
import com.giftextview.util.decode.GifDecodeUtil;
import com.giftextview.util.download.DownloadRecordUtil;
import com.giftextview.util.download.HttpDownloadUtil;
import com.giftextview.util.download.HttpDownloadUtil.HttpDownloadCallback;

/**
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年03月30日
 * 
 *      记得给我个star哦~
 * 
 */
public class GifTextViewHelper {

	private final static int SPAN_FLAG = Spannable.SPAN_INCLUSIVE_EXCLUSIVE;
	private final static String KEY_GIF_TEXT_VIEW = "gifTextView";
	private final static String KEY_SPAN_ENTITY = "spanEntity";
	private final static String KEY_FILE_PATH = "filePath";
	private final static String KEY_OPERATE_TYPE = "operateType";
	private final static int INVALIDATE = 1;
	private final static int RESET_TEXT = 2;
	private final static int MAX_DOWNLOAD_NUMBER = 3;

	private static GifTextViewHelper mInstance;

	private HandlerThread mPlayGifThread;
	private HandlerThread mPlayEmojiThread;
	private HandlerThread mDecodeGifThread;
	private HandlerThread mDecodeEmojiThread;
	private HandlerThread mDownloadThread;

	private Handler mUIHandler;
	private Handler mPlayGifHandler;
	private Handler mPlayEmojiHandler;
	private Handler mDecodeGifHandler;
	private Handler mDecodeEmojiHandler;
	private Handler mDownloadHandler;

	private Boolean mSyncPlayGifFinishFlag;
	private Boolean mSyncPlayEmojiFinishFlag;
	private Boolean mSyncDecodeGifFinishFlag;
	private Boolean mSyncDecodeEmojiFinishFlag;
	private Boolean mSyncDownloadFinishFlag;

	private Bitmap mWaitBitmap;
	private Bitmap mEmojiWaitBitmap;
	private Bitmap mDownloadFailBitmap;
	private Bitmap mZipBitmap;

	private Context mContext;
	private int mBitmapMaxWidth;
	private int mBitmapMaxHeight;
	private boolean mIsAutoDownload;
	private boolean mIsAllowDownload;
	private HashMap<GifTextView, Integer> mViewMap;
	private HashMap<GifTextView, HashMap<String, GifDrawableSpan>> mGifSpanMap;

	private String mDownloadLocalDir;
	private int mCurrentDownloadCount;
	private ArrayList<String> mDownloadUrlList;
	private HashMap<String, ArrayList<SameDownloadEntity>> mSameDownloadMap;
	private HashMap<HttpDownloadUtil, Boolean> mDownloadPoolMap;

	private DiskCacheUtil mDiskCacheUtil;
	private BitmapDiskCacheUtil mBitmapDiskCacheUtil;
	private BitmapCache mBitmapCache;
	private BitmapCache mEmojiCache;
	private ResUtil mResUtil;
	private DecodeRecordUtil mDecodeRecordUtil;

	private OnTextSpanClickListener mSpanClickListener;

	private GifTextViewHelper(Context context, int bitmapMaxWidth,
			int bitmapMaxHeight) {
		mContext = context;
		mBitmapMaxWidth = bitmapMaxWidth;
		mBitmapMaxHeight = bitmapMaxHeight;
		mIsAutoDownload = true;
		mViewMap = new HashMap<GifTextView, Integer>();
		mGifSpanMap = new HashMap<GifTextView, HashMap<String, GifDrawableSpan>>();
		initUtil();
		initDownload();
		initBitmap();
		initThread();
		initFinishFlag();
		uiHandler();
		playGifHandler();
		playEmojiHandler();
		decodeGifHandler();
		decodeEmojiHandler();
		downloadHandler();
	}

	// **************************************************************************************************
	// 公有方法

	public static GifTextViewHelper getInstance(Context context,
			int bitmapMaxWidth, int bitmapMaxHeight) {
		if (mInstance == null)
			mInstance = new GifTextViewHelper(context, bitmapMaxWidth,
					bitmapMaxHeight);
		return mInstance;
	}

	/**
	 * 在Activity的onResume方法内调用
	 */
	public void onResume() {
		mUIHandler.postAtFrontOfQueue(new Runnable() {
			@Override
			public void run() {
				synchronized (mPlayGifThread) {
					mPlayGifThread.notifyAll();
				}
				synchronized (mPlayEmojiThread) {
					mPlayEmojiThread.notifyAll();
				}
			}
		});
	}

	/**
	 * 在Activity的onPause方法内调用
	 */
	public void onPause() {
		mPlayGifHandler.postAtFrontOfQueue(new Runnable() {
			@Override
			public void run() {
				synchronized (mPlayGifThread) {
					try {
						mPlayGifThread.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		mPlayEmojiHandler.postAtFrontOfQueue(new Runnable() {
			@Override
			public void run() {
				synchronized (mPlayEmojiThread) {
					try {
						mPlayEmojiThread.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		mBitmapDiskCacheUtil.flush();
	}

	/**
	 * 在Activity的onDestroy方法内调用
	 */
	public void onDestroy() {
		quitThread();
		removeAllMessages();
		setFinishFlag();
		recycleBitmap();
		clearViewMap();
		clearGifSpanMap();
		closeUtil();
		cancelDownload();
		setNull();
	}

	/**
	 * 清除所有缓存
	 */
	public void clearAllCache() {
		DiskCacheUtil.clearDiskCache(mContext);
		mBitmapDiskCacheUtil.delete();
		mDecodeRecordUtil.clear();
		DownloadRecordUtil downloadRecordUtil = new DownloadRecordUtil(mContext);
		downloadRecordUtil.clear();
	}

	/**
	 * 优先显示图片
	 * 
	 * @param gifTextView
	 * @param entity
	 */
	public void displayPicture(GifTextView gifTextView, SpanEntity entity) {
		mIsAllowDownload = true;
		File localFile = getDownloadPictureFile(entity);
		if (localFile == null)
			sendMessage2Download(gifTextView, entity,
					gifTextView.mCurrentMsgWhat.get(), true);
		else
			sendMessage2Decode(gifTextView, entity,
					gifTextView.mCurrentMsgWhat.get(),
					localFile.getAbsolutePath());
	}

	/**
	 * 获取已下载好的本地图片
	 * 
	 * @param entity
	 * @return 若为null，则本地无该图片
	 */
	public File getDownloadPictureFile(SpanEntity entity) {
		// 根据HttpDownloadUtil类构造的本地文件路径
		String fileName = MD5Util.getMD5(entity.url)
				+ "_"
				+ entity.url.substring(entity.url.lastIndexOf("/") + 1,
						entity.url.length());
		String path = mDownloadLocalDir + File.separator + fileName;

		File file = new File(path);
		if (file.exists())
			return file;
		else
			return null;
	}

	/**
	 * 在ListView的Adapter调用notifyDataSetChanged方法的前面调用
	 */
	public void notifyDataSetChanged() {
		removeAllMessages();
		// 会导致Adapter在更新列表之后，再次进行更新，因为item的高度可能发生了变化
		// for (Map.Entry<GifTextView, Integer> entry : mViewMap.entrySet()) {
		// GifTextView gifTextView = entry.getKey();
		// gifTextView.getEditableText().clearSpans();
		// }
		mViewMap.clear();
		mGifSpanMap.clear();
	}

	/**
	 * 在ListView的Adapter调用notifyDataSetChanged方法的前面调用
	 * 
	 * @param groupIndex
	 *            ListView所属的组序号
	 */
	public void notifyDataSetChanged(int groupIndex) {
		ArrayList<GifTextView> gifTextViewList = new ArrayList<GifTextView>();
		for (Map.Entry<GifTextView, Integer> entry : mViewMap.entrySet()) {
			GifTextView gifTextView = entry.getKey();
			if (gifTextView.mCurrentGroupIndex.get() == groupIndex) {
				removeMessages(gifTextView.mCurrentMsgWhat.get());
				gifTextViewList.add(gifTextView);
			}
		}
		for (GifTextView gifTextView : gifTextViewList) {
			mViewMap.remove(gifTextView);
			mGifSpanMap.remove(gifTextView);
		}
	}

	/**
	 * 设置是否自动下载图片
	 * 
	 * @param auto
	 */
	public void setAutoDownload(boolean auto) {
		mIsAutoDownload = auto;
	}

	// **************************************************************************************************
	// 保护方法

	protected void execute(GifTextView gifTextView, int msgWhat,
			CharSequence content, ArrayList<SpanEntity> spanList,
			OnTextSpanClickListener listener) {

		if (mViewMap.containsKey(gifTextView))
			removeMessages(gifTextView.mCurrentMsgWhat.get());
		if (mGifSpanMap.containsKey(gifTextView))
			mGifSpanMap.remove(gifTextView);
		gifTextView.mCurrentMsgWhat.set(msgWhat);
		mViewMap.put(gifTextView, msgWhat);
		gifTextView.setText(new SpannableString(content));
		if (spanList == null || spanList.isEmpty())
			return;

		mSpanClickListener = listener;

		for (SpanEntity entity : spanList) {
			if (entity.spanType == SpanEntity.SPAN_TYPE_LINK)
				addLinkSpan(gifTextView, entity);
			else if (entity.spanType == SpanEntity.SPAN_TYPE_ZIP)
				addGifSpan(gifTextView, entity, mZipBitmap);
			else if (entity.spanType == SpanEntity.SPAN_TYPE_IMAGE)
				displayNormalPicture(gifTextView, entity, msgWhat);
			else if (entity.spanType == SpanEntity.SPAN_TYPE_GIF)
				displayGifPicture(gifTextView, entity, msgWhat, false);
			else if (entity.spanType == SpanEntity.SPAN_TYPE_EMOJI)
				displayGifPicture(gifTextView, entity, msgWhat, true);
		}

	}

	// **************************************************************************************************
	// 私有方法

	private void initUtil() {
		mDiskCacheUtil = DiskCacheUtil.open(mContext);
		mBitmapDiskCacheUtil = BitmapDiskCacheUtil.getInstance(mContext);
		mResUtil = ResUtil.getInstance(mContext);
		mDecodeRecordUtil = new DecodeRecordUtil(mContext);

		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheSize = maxMemory / 8;
		mBitmapCache = new BitmapCache(cacheSize);
		cacheSize = maxMemory / 16;
		mEmojiCache = new BitmapCache(cacheSize);
	}

	private void initDownload() {
		mDownloadLocalDir = DiskCacheUtil.getDiskCacheDir(mContext)
				.getAbsolutePath();
		mDownloadUrlList = new ArrayList<String>();
		mSameDownloadMap = new HashMap<String, ArrayList<SameDownloadEntity>>();
		mDownloadPoolMap = new HashMap<HttpDownloadUtil, Boolean>(
				MAX_DOWNLOAD_NUMBER);
		for (int i = 0; i < MAX_DOWNLOAD_NUMBER; i++) {
			HttpDownloadUtil downloadUtil = new HttpDownloadUtil(mContext);
			mDownloadPoolMap.put(downloadUtil, false);
		}
	}

	private void initBitmap() {
		mWaitBitmap = BitmapUtil.getBitmap(mContext, mResUtil
				.getIdFromDrawable(com.giftextview.res.DrawableRes.icon_wait),
				mBitmapMaxWidth, mBitmapMaxHeight);
		mEmojiWaitBitmap = BitmapUtil
				.getBitmap(
						mContext,
						mResUtil.getIdFromDrawable(com.giftextview.res.DrawableRes.icon_emoji_wait),
						mBitmapMaxWidth, mBitmapMaxHeight);
		mDownloadFailBitmap = BitmapUtil
				.getBitmap(
						mContext,
						mResUtil.getIdFromDrawable(com.giftextview.res.DrawableRes.icon_download_fail),
						mBitmapMaxWidth, mBitmapMaxHeight);
		mZipBitmap = BitmapUtil.getBitmap(mContext, mResUtil
				.getIdFromDrawable(com.giftextview.res.DrawableRes.icon_zip),
				mBitmapMaxWidth, mBitmapMaxHeight);
	}

	private void initThread() {
		mPlayGifThread = new HandlerThread("mPlayGifThread");
		mPlayEmojiThread = new HandlerThread("mPlayEmojiThread");
		mDecodeGifThread = new HandlerThread("mDecodeGifThread");
		mDecodeEmojiThread = new HandlerThread("mDecodeEmojiThread");
		mDownloadThread = new HandlerThread("mDownloadThread");

		mPlayGifThread.start();
		mPlayEmojiThread.start();
		mDecodeGifThread.start();
		mDecodeEmojiThread.start();
		mDownloadThread.start();
	}

	private void initFinishFlag() {
		mSyncPlayGifFinishFlag = false;
		mSyncPlayEmojiFinishFlag = false;
		mSyncDecodeGifFinishFlag = false;
		mSyncDecodeEmojiFinishFlag = false;
		mSyncDownloadFinishFlag = false;
	}

	private void uiHandler() {
		mUIHandler = new Handler(mContext.getMainLooper()) {
			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				HashMap<String, Object> map = (HashMap<String, Object>) msg.obj;
				GifTextView gifTextView = (GifTextView) map
						.get(KEY_GIF_TEXT_VIEW);
				int operateType = (int) map.get(KEY_OPERATE_TYPE);
				if (gifTextView.mCurrentMsgWhat.get() == msg.what) {
					if (operateType == INVALIDATE)
						gifTextView.invalidate();
					else
						gifTextView.setText(gifTextView.getText());
				}
			}
		};
	}

	private void playGifHandler() {
		mPlayGifHandler = new Handler(mPlayGifThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				synchronized (mSyncPlayGifFinishFlag) {
					if (mSyncPlayGifFinishFlag == false)
						handlePlayMessage(msg);
				}
			}
		};
	}

	private void playEmojiHandler() {
		mPlayEmojiHandler = new Handler(mPlayEmojiThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				synchronized (mSyncPlayEmojiFinishFlag) {
					if (mSyncPlayEmojiFinishFlag == false)
						handlePlayMessage(msg);
				}
			}
		};
	}

	private void decodeGifHandler() {
		mDecodeGifHandler = new Handler(mDecodeGifThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				synchronized (mSyncDecodeGifFinishFlag) {
					if (mSyncDecodeGifFinishFlag == false)
						handleDecodeMessage(msg);
				}
			}
		};
	}

	private void decodeEmojiHandler() {
		mDecodeEmojiHandler = new Handler(mDecodeEmojiThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				synchronized (mSyncDecodeEmojiFinishFlag) {
					if (mSyncDecodeEmojiFinishFlag == false)
						handleDecodeMessage(msg);
				}
			}
		};
	}

	private void downloadHandler() {
		mDownloadHandler = new Handler(mDownloadThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				synchronized (mSyncDownloadFinishFlag) {
					if (mSyncDownloadFinishFlag == false)
						handleDownloadMessage(msg);
				}
			}
		};
	}

	@SuppressWarnings("unchecked")
	private void handlePlayMessage(Message msg) {
		HashMap<String, Object> map = (HashMap<String, Object>) msg.obj;
		GifTextView gifTextView = (GifTextView) map.get(KEY_GIF_TEXT_VIEW);
		if (gifTextView.mCurrentMsgWhat.get() == msg.what) {
			SpanEntity entity = (SpanEntity) map.get(KEY_SPAN_ENTITY);
			boolean isEmoji = false;
			if (entity.spanType == SpanEntity.SPAN_TYPE_EMOJI)
				isEmoji = true;
			Bitmap bitmap = getBitmapFromCache(getFrameCacheKey(entity),
					isEmoji);
			if (bitmap != null) {
				sendMessage2UI(gifTextView, entity, msg.what, bitmap);
				sendMessage2Loop(gifTextView, entity, msg.what);
			} else
				sendMessage2Download(gifTextView, entity, msg.what, false);
		}
	}

	@SuppressWarnings("unchecked")
	private void handleDecodeMessage(Message msg) {
		HashMap<String, Object> map = (HashMap<String, Object>) msg.obj;
		GifTextView gifTextView = (GifTextView) map.get(KEY_GIF_TEXT_VIEW);
		if (gifTextView.mCurrentMsgWhat.get() == msg.what) {
			SpanEntity entity = (SpanEntity) map.get(KEY_SPAN_ENTITY);
			String filePath = (String) map.get(KEY_FILE_PATH);
			InputStream is = null;
			boolean isEmoji = false;
			try {
				if (entity.spanType == SpanEntity.SPAN_TYPE_GIF)
					is = new FileInputStream(filePath);
				else {
					is = mContext.getResources().openRawResource(
							mResUtil.getIdFromDrawable(entity.url));
					isEmoji = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (is != null) {
				GifDecodeUtil gifDecodeUtil = new GifDecodeUtil();
				boolean isDecodeSuccess = gifDecodeUtil.decode(is,
						mBitmapMaxWidth, mBitmapMaxHeight);
				if (isDecodeSuccess) {
					int frameCount = gifDecodeUtil.getFrameCount();
					Bitmap nextFrameBmp = null;
					for (int i = msg.arg1; i < frameCount; i++) {
						String key = getFrameCacheKey(entity.url, i);
						Bitmap bitmap = mBitmapCache.get(key);
						if (bitmap == null || bitmap.isRecycled()) {
							bitmap = gifDecodeUtil.getNextFrame(i);
							if (isEmoji)
								mEmojiCache.put(key, bitmap);
							else
								mBitmapCache.put(key, bitmap);
						}
						if (msg.arg1 == i)
							nextFrameBmp = bitmap;
						is = mBitmapDiskCacheUtil.get(key);
						if (is == null)
							mBitmapDiskCacheUtil.put(key, bitmap);
						else {
							try {
								is.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					entity.frameCount = frameCount;
					entity.delays = gifDecodeUtil.getDelays();
					saveGifDelays(MD5Util.getMD5(entity.url), entity.delays);
					sendMessage2UI(gifTextView, entity, msg.what, nextFrameBmp);
					sendMessage2Loop(gifTextView, entity, msg.what);
				}
				gifDecodeUtil.clear();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void handleDownloadMessage(Message msg) {
		HashMap<String, Object> map = (HashMap<String, Object>) msg.obj;
		GifTextView gifTextView = (GifTextView) map.get(KEY_GIF_TEXT_VIEW);
		if (gifTextView.mCurrentMsgWhat.get() == msg.what) {
			HttpDownloadUtil downloadUtil = getHttpDownloadUtil();
			SpanEntity entity = (SpanEntity) map.get(KEY_SPAN_ENTITY);
			if (downloadUtil != null) {
				if (mDownloadUrlList.contains(entity.url) == false) {
					mDownloadUrlList.add(entity.url);
					downloadUtil.downloadFile(entity.url, mDownloadLocalDir,
							new HttpDownloadListener(downloadUtil, gifTextView,
									entity, msg.what));
				} else
					recycleHttpDownloadUtil(gifTextView, entity, msg.what,
							downloadUtil, entity.url, false);
			} else {
				synchronized (mDownloadThread) {
					try {
						mDownloadThread.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				sendMessage2Download(gifTextView, entity, msg.what, false);
			}
		}
	}

	private void addLinkSpan(GifTextView gifTextView, SpanEntity entity) {
		SpannableString spanStr = (SpannableString) gifTextView.getText();
		for (int i = 0; i < entity.starts.size(); i++)
			spanStr.setSpan(new LinkClickableSpan(entity, mSpanClickListener),
					entity.starts.get(i), entity.ends.get(i), SPAN_FLAG);
		gifTextView.setText(spanStr);
	}

	private void addGifSpan(GifTextView gifTextView, SpanEntity entity,
			Bitmap bitmap) {
		HashMap<String, GifDrawableSpan> map;
		if (mGifSpanMap.containsKey(gifTextView))
			map = mGifSpanMap.get(gifTextView);
		else {
			map = new HashMap<String, GifDrawableSpan>();
			mGifSpanMap.put(gifTextView, map);
		}

		SpannableString spanStr = (SpannableString) gifTextView.getText();
		for (int i = 0; i < entity.starts.size(); i++) {
			GifDrawableSpan span = new GifDrawableSpan(mContext, bitmap,
					entity, mSpanClickListener);
			int start = entity.starts.get(i);
			int end = entity.ends.get(i);
			String key = getSpanKey(gifTextView.mCurrentMsgWhat.get(), start,
					end);
			map.put(key, span);
			spanStr.setSpan(span, start, end, SPAN_FLAG);
		}
		gifTextView.setText(spanStr);
	}

	private int updateGifSpan(GifTextView gifTextView, SpanEntity entity,
			int msgWhat, Bitmap bitmap) {
		HashMap<String, GifDrawableSpan> spanMap = mGifSpanMap.get(gifTextView);
		if (spanMap == null || spanMap.isEmpty())
			return -1;
		int operateType = INVALIDATE;
		for (int i = 0; i < entity.starts.size(); i++) {
			int start = entity.starts.get(i);
			int end = entity.ends.get(i);
			String key = getSpanKey(msgWhat, start, end);
			GifDrawableSpan span = spanMap.get(key);
			if (span != null) {
				span.nextFrame(mContext, bitmap);
				if (span.isResetLayout())
					operateType = RESET_TEXT;
			}
		}
		return operateType;
	}

	private void displayNormalPicture(GifTextView gifTextView,
			SpanEntity entity, int msgWhat) {
		String key = MD5Util.getMD5(entity.url);
		Bitmap bitmap = getBitmapFromCache(key, false);
		if (bitmap == null) {
			bitmap = mWaitBitmap;
			sendMessage2Download(gifTextView, entity, msgWhat, false);
		}
		addGifSpan(gifTextView, entity, bitmap);
	}

	private void displayGifPicture(GifTextView gifTextView, SpanEntity entity,
			int msgWhat, boolean isEmoji) {
		boolean isExist = true;
		String key = getFrameCacheKey(entity);
		Bitmap bitmap = getBitmapFromCache(key, isEmoji);
		if (bitmap == null) {
			if (isEmoji) {
				bitmap = mEmojiWaitBitmap;
				sendMessage2Decode(gifTextView, entity, msgWhat, null);
			} else {
				bitmap = mWaitBitmap;
				sendMessage2Download(gifTextView, entity, msgWhat, false);
			}
			isExist = false;
		}
		addGifSpan(gifTextView, entity, bitmap);
		if (isExist) {
			if (entity.delays == null) {
				int[] delays = getGifDelays(MD5Util.getMD5(entity.url));
				if (delays != null) {
					entity.delays = delays;
					entity.frameCount = delays.length;
				}
			}
			if (entity.delays != null)
				sendMessage2Loop(gifTextView, entity, msgWhat);
			else {
				if (entity.spanType == SpanEntity.SPAN_TYPE_GIF)
					sendMessage2Download(gifTextView, entity, msgWhat, false);
				else
					sendMessage2Decode(gifTextView, entity, msgWhat, null);
			}
		}
	}

	private void displayImage(GifTextView gifTextView, SpanEntity entity,
			int msgWhat, String filePath) {
		Bitmap bitmap = BitmapUtil.getBitmap(filePath, mBitmapMaxWidth,
				mBitmapMaxHeight);
		if (bitmap == null) // 文件可能已损坏，导致解析失败
			return;
		String key = MD5Util.getMD5(entity.url);
		mBitmapCache.put(key, bitmap);
		sendMessage2UI(gifTextView, entity, msgWhat, bitmap);
	}

	private void sendMessage2UI(GifTextView gifTextView, SpanEntity entity,
			int msgWhat, Bitmap bitmap) {
		if (gifTextView.mCurrentMsgWhat.get() == msgWhat) {
			int operateType = updateGifSpan(gifTextView, entity, msgWhat,
					bitmap);
			if (operateType == -1)
				return;
			Message msg = mUIHandler.obtainMessage();
			msg.what = msgWhat;
			HashMap<String, Object> map = new HashMap<String, Object>(2);
			map.put(KEY_GIF_TEXT_VIEW, gifTextView);
			map.put(KEY_OPERATE_TYPE, operateType);
			msg.obj = map;
			mUIHandler.sendMessage(msg);
		}
	}

	private void sendMessage2Download(GifTextView gifTextView,
			SpanEntity entity, int msgWhat, boolean isAtFront) {
		if (mIsAllowDownload || mIsAutoDownload) {
			mIsAllowDownload = false;
			Message msg = mDownloadHandler.obtainMessage();
			msg.what = msgWhat;
			HashMap<String, Object> map = new HashMap<String, Object>(2);
			map.put(KEY_GIF_TEXT_VIEW, gifTextView);
			map.put(KEY_SPAN_ENTITY, entity);
			msg.obj = map;
			if (isAtFront)
				mDownloadHandler.sendMessageAtFrontOfQueue(msg);
			else
				mDownloadHandler.sendMessage(msg);
		}
	}

	private void sendMessage2Decode(GifTextView gifTextView, SpanEntity entity,
			int msgWhat, String filePath) {
		Handler handler;
		if (entity.spanType == SpanEntity.SPAN_TYPE_GIF)
			handler = mDecodeGifHandler;
		else
			handler = mDecodeEmojiHandler;
		Message msg = handler.obtainMessage();
		msg.what = msgWhat;
		msg.arg1 = entity.nextFrameIndex;
		HashMap<String, Object> map = new HashMap<String, Object>(3);
		map.put(KEY_GIF_TEXT_VIEW, gifTextView);
		map.put(KEY_SPAN_ENTITY, entity);
		map.put(KEY_FILE_PATH, filePath);
		msg.obj = map;
		handler.sendMessage(msg);
	}

	private void sendMessage2Loop(GifTextView gifTextView, SpanEntity entity,
			int msgWhat) {
		Handler handler;
		if (entity.spanType == SpanEntity.SPAN_TYPE_GIF)
			handler = mPlayGifHandler;
		else
			handler = mPlayEmojiHandler;
		Message msg = handler.obtainMessage();
		msg.what = msgWhat;
		synchronized (entity) {
			entity.nextFrameIndex++;
			if (entity.nextFrameIndex == entity.frameCount)
				entity.nextFrameIndex = 0;
			msg.arg1 = entity.nextFrameIndex;
		}
		HashMap<String, Object> map = new HashMap<String, Object>(2);
		map.put(KEY_GIF_TEXT_VIEW, gifTextView);
		map.put(KEY_SPAN_ENTITY, entity);
		msg.obj = map;
		handler.sendMessageDelayed(msg, entity.delays[msg.arg1]);
	}

	private synchronized HttpDownloadUtil getHttpDownloadUtil() {
		HttpDownloadUtil downloadUtil = null;
		if (mCurrentDownloadCount < MAX_DOWNLOAD_NUMBER) {
			for (Entry<HttpDownloadUtil, Boolean> entry : mDownloadPoolMap
					.entrySet()) {
				if (entry.getValue() == false) {
					downloadUtil = entry.getKey();
					mDownloadPoolMap.put(downloadUtil, true);
					mCurrentDownloadCount++;
					break;
				}
			}
		}
		return downloadUtil;
	}

	private synchronized void recycleHttpDownloadUtil(GifTextView gifTextView,
			SpanEntity entity, int msgWhat, HttpDownloadUtil downloadUtil,
			String downloadUrl, boolean isRemoveUrl) {
		if (isRemoveUrl) {
			mDownloadUrlList.remove(downloadUrl);
			if (mSameDownloadMap.containsKey(entity.url)) {
				ArrayList<SameDownloadEntity> list = mSameDownloadMap
						.get(entity.url);
				Iterator<SameDownloadEntity> iterator = list.iterator();
				while (iterator.hasNext()) {
					SameDownloadEntity sameEntity = iterator.next();
					if (sameEntity.gifTextView == gifTextView)
						iterator.remove();
					else
						sendMessage2Download(sameEntity.gifTextView,
								sameEntity.entity, sameEntity.msgWhat, false);
				}
			}
		} else {
			ArrayList<SameDownloadEntity> list = null;
			if (mSameDownloadMap.containsKey(entity.url))
				list = mSameDownloadMap.get(entity.url);
			else {
				list = new ArrayList<SameDownloadEntity>(5);
				mSameDownloadMap.put(entity.url, list);
			}

			boolean isExist = false;
			for (SameDownloadEntity sameEntity : list) {
				if (sameEntity.gifTextView == gifTextView) {
					isExist = true;
					break;
				}
			}
			if (isExist == false) {
				SameDownloadEntity sameEntity = new SameDownloadEntity();
				sameEntity.gifTextView = gifTextView;
				sameEntity.entity = entity;
				sameEntity.msgWhat = msgWhat;
				list.add(sameEntity);
			}
		}

		mDownloadPoolMap.put(downloadUtil, false);
		mCurrentDownloadCount--;
		if (mCurrentDownloadCount == MAX_DOWNLOAD_NUMBER - 1) {
			synchronized (mDownloadThread) {
				mDownloadThread.notifyAll();
			}
		}
	}

	private Bitmap getBitmapFromCache(String key, boolean isEmoji) {
		Bitmap bitmap;
		if (isEmoji)
			bitmap = mEmojiCache.get(key);
		else
			bitmap = mBitmapCache.get(key);

		if (bitmap == null || bitmap.isRecycled()) {
			InputStream is = mBitmapDiskCacheUtil.get(key);
			if (is != null) {
				bitmap = BitmapUtil.getBitmap(is);
				if (isEmoji)
					mEmojiCache.put(key, bitmap);
				else
					mBitmapCache.put(key, bitmap);
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	}

	private void saveGifDelays(String key, int[] delays) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < delays.length; i++)
			builder.append(delays[i]).append(",");
		builder.deleteCharAt(builder.length() - 1);
		synchronized (mDecodeRecordUtil) {
			mDecodeRecordUtil.set(key, builder.toString());
		}
	}

	private int[] getGifDelays(String key) {
		String value;
		synchronized (mDecodeRecordUtil) {
			value = mDecodeRecordUtil.get(key);
		}
		int[] delays = null;
		if (value != null) {
			String[] array = value.split(",");
			delays = new int[array.length];
			for (int i = 0; i < array.length; i++)
				delays[i] = Integer.valueOf(array[i]);
		}
		return delays;
	}

	private String getSpanKey(int msgWhat, int start, int end) {
		return msgWhat + "-" + start + "-" + end;
	}

	private String getFrameCacheKey(SpanEntity entity) {
		if (entity.spanType == SpanEntity.SPAN_TYPE_GIF
				|| entity.spanType == SpanEntity.SPAN_TYPE_EMOJI)
			return getFrameCacheKey(entity.url, entity.nextFrameIndex);
		else
			throw new IllegalArgumentException(
					"SpanEntity.spanType is not GIF or EMOJI");
	}

	private String getFrameCacheKey(String url, int frameIndex) {
		StringBuilder builder = new StringBuilder();
		builder.append(MD5Util.getMD5(url)).append("_").append(frameIndex);
		return builder.toString();
	}

	private void quitThread() {
		mPlayGifThread.quit();
		mPlayEmojiThread.quit();
		mDecodeGifThread.quit();
		mDecodeEmojiThread.quit();
		mDownloadThread.quit();
	}

	private void removeMessages(int msgWhat) {
		mPlayGifHandler.removeMessages(msgWhat);
		mPlayEmojiHandler.removeMessages(msgWhat);
		mDecodeGifHandler.removeMessages(msgWhat);
		mDecodeEmojiHandler.removeMessages(msgWhat);
		mDownloadHandler.removeMessages(msgWhat);
		mUIHandler.removeMessages(msgWhat);
	}

	private void removeAllMessages() {
		mPlayGifHandler.removeCallbacksAndMessages(null);
		mPlayEmojiHandler.removeCallbacksAndMessages(null);
		mDecodeGifHandler.removeCallbacksAndMessages(null);
		mDecodeEmojiHandler.removeCallbacksAndMessages(null);
		mDownloadHandler.removeCallbacksAndMessages(null);
		mUIHandler.removeCallbacksAndMessages(null);
	}

	private void setFinishFlag() {
		synchronized (mSyncPlayGifFinishFlag) {
			mSyncPlayGifFinishFlag = true;
		}
		synchronized (mSyncPlayEmojiFinishFlag) {
			mSyncPlayEmojiFinishFlag = true;
		}
		synchronized (mSyncDecodeGifFinishFlag) {
			mSyncDecodeGifFinishFlag = true;
		}
		synchronized (mSyncDecodeEmojiFinishFlag) {
			mSyncDecodeEmojiFinishFlag = true;
		}
		synchronized (mSyncDownloadFinishFlag) {
			mSyncDownloadFinishFlag = true;
		}
	}

	private void recycleBitmap() {
		mWaitBitmap.recycle();
		mEmojiWaitBitmap.recycle();
		mDownloadFailBitmap.recycle();
		mZipBitmap.recycle();
	}

	private void clearViewMap() {
		for (Map.Entry<GifTextView, Integer> entry : mViewMap.entrySet()) {
			GifTextView gifTextView = entry.getKey();
			CharSequence text = gifTextView.getText();
			if (text instanceof Spanned) {
				SpannableString spanStr = (SpannableString) text;
				GifDrawableSpan[] oldSpans = spanStr.getSpans(0,
						spanStr.length(), GifDrawableSpan.class);
				for (int i = 0; oldSpans != null && i < oldSpans.length; i++) {
					spanStr.removeSpan(oldSpans[i]);
					((BitmapDrawable) oldSpans[i].getDrawable()).getBitmap()
							.recycle();
				}
			}
		}
		mViewMap.clear();
	}

	private void clearGifSpanMap() {
		for (Map.Entry<GifTextView, HashMap<String, GifDrawableSpan>> pEntry : mGifSpanMap
				.entrySet()) {
			HashMap<String, GifDrawableSpan> map = pEntry.getValue();
			for (Map.Entry<String, GifDrawableSpan> entry : map.entrySet()) {
				GifDrawableSpan span = entry.getValue();
				((BitmapDrawable) span.getDrawable()).getBitmap().recycle();
			}
			map.clear();
		}
		mGifSpanMap.clear();
	}

	private void closeUtil() {
		DiskCacheUtil.close(mContext);
		mBitmapDiskCacheUtil.close();
		mBitmapCache.recycle();
		mEmojiCache.recycle();
		mResUtil.clear();
	}

	private void cancelDownload() {
		for (Map.Entry<HttpDownloadUtil, Boolean> entry : mDownloadPoolMap
				.entrySet()) {
			HttpDownloadUtil downloadUtil = entry.getKey();
			downloadUtil.cancel();
		}
		mDownloadPoolMap.clear();
		mDownloadUrlList.clear();
		mSameDownloadMap.clear();
	}

	private void setNull() {
		mPlayGifThread = null;
		mPlayEmojiThread = null;
		mDecodeGifThread = null;
		mDecodeEmojiThread = null;
		mDownloadThread = null;

		mUIHandler = null;
		mPlayGifHandler = null;
		mPlayEmojiHandler = null;
		mDecodeGifHandler = null;
		mDecodeEmojiHandler = null;
		mDownloadHandler = null;

		mWaitBitmap = null;
		mEmojiWaitBitmap = null;
		mDownloadFailBitmap = null;
		mZipBitmap = null;

		mDiskCacheUtil = null;
		mBitmapDiskCacheUtil = null;
		mBitmapCache = null;
		mEmojiCache = null;
		mResUtil = null;
		mDecodeRecordUtil = null;

		mDownloadPoolMap = null;
		mDownloadUrlList = null;
		mSameDownloadMap = null;

		mContext = null;
		mViewMap = null;
		mGifSpanMap = null;
		mSpanClickListener = null;
		mInstance = null;
	}

	// **************************************************************************************************
	// 自定义的类

	private class HttpDownloadListener implements HttpDownloadCallback {

		private HttpDownloadUtil downloadUtil = null;
		private GifTextView gifTextView = null;
		private SpanEntity entity = null;
		private int msgWhat = 0;

		public HttpDownloadListener(HttpDownloadUtil downloadUtil,
				GifTextView gifTextView, SpanEntity entity, int msgWhat) {
			this.downloadUtil = downloadUtil;
			this.gifTextView = gifTextView;
			this.entity = entity;
			this.msgWhat = msgWhat;
		}

		@Override
		public void onResult(int flag, File file, boolean isFileWhole) {
			if (mSyncDownloadFinishFlag == false) {
				if (gifTextView.mCurrentMsgWhat.get() == msgWhat) {
					if (file != null && isFileWhole == true) {
						if (entity.spanType == SpanEntity.SPAN_TYPE_IMAGE)
							displayImage(gifTextView, entity, msgWhat,
									file.getAbsolutePath());
						else if (entity.spanType == SpanEntity.SPAN_TYPE_GIF)
							sendMessage2Decode(gifTextView, entity, msgWhat,
									file.getAbsolutePath());
						mDiskCacheUtil.put(file.getAbsolutePath());
					} else
						sendMessage2UI(gifTextView, entity, flag,
								mDownloadFailBitmap);
				}
				recycleHttpDownloadUtil(gifTextView, entity, msgWhat,
						downloadUtil, entity.url, true);
			}
		}

		@Override
		public void onStart(String localPath) {
		}

		@Override
		public void onDownloading(long total, long current) {
		}

		@Override
		public void onCancelled() {
			if (mSyncDownloadFinishFlag == false)
				recycleHttpDownloadUtil(gifTextView, entity, msgWhat,
						downloadUtil, entity.url, true);
		}

	}

}
