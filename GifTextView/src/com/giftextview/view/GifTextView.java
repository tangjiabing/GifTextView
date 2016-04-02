package com.giftextview.view;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.giftextview.entity.SpanEntity;
import com.giftextview.span.CustomLinkMovementMethod;
import com.giftextview.span.OnTextSpanClickListener;

/**
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年03月30日
 * 
 *      记得给我个star哦~
 * 
 */
public class GifTextView extends TextView {

	private final static int MAX_MEMBER_NUMBER_OF_GROUP = 10000;
	protected AtomicInteger mCurrentMsgWhat;
	protected AtomicInteger mCurrentGroupIndex;
	private GifTextViewHelper mHelper;

	public GifTextView(Context context) {
		this(context, null);
	}

	public GifTextView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.textViewStyle);
	}

	public GifTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	// **********************************************************
	// 公有方法

	/**
	 * 传入数据，初始化控件
	 * 
	 * @param groupIndex
	 *            组序号，可能有多个ListView，每个ListView所属的组序号不一样
	 * @param position
	 *            ListView的item位置
	 * @param content
	 *            文本内容
	 * @param spanList
	 *            图片、链接在文本中的具体信息
	 * @param bitmapMaxWidth
	 *            显示图片最大的宽度
	 * @param bitmapMaxHeight
	 *            显示图片最大的高度
	 * @param listener
	 *            监听点击图片、链接的事件
	 */
	public void initView(int groupIndex, int position, CharSequence content,
			ArrayList<SpanEntity> spanList, int bitmapMaxWidth,
			int bitmapMaxHeight, OnTextSpanClickListener listener) {
		int msgWhat = groupIndex * MAX_MEMBER_NUMBER_OF_GROUP + position;
		mCurrentGroupIndex.set(groupIndex);
		if (mHelper == null)
			mHelper = GifTextViewHelper.getInstance(getContext(),
					bitmapMaxWidth, bitmapMaxHeight);
		mHelper.execute(this, msgWhat, content, spanList, listener);
	}

	// **********************************************************
	// 私有方法

	private void init() {
		setMovementMethod(new CustomLinkMovementMethod());
		mCurrentMsgWhat = new AtomicInteger(-1);
		mCurrentGroupIndex = new AtomicInteger(-1);
	}

}
