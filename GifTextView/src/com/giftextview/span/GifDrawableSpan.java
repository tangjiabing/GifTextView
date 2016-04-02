package com.giftextview.span;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;
import android.view.View;

import com.giftextview.entity.SpanEntity;
import com.giftextview.view.GifTextView;

/**
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年03月30日
 * 
 *      记得给我个star哦~
 * 
 */
public class GifDrawableSpan extends DynamicDrawableSpan {

	private BitmapDrawable mDrawable;
	private SpanEntity mEntity;
	private OnTextSpanClickListener mListener;
	private boolean mIsResetLayout;
	private int mOldWidth;
	private int mOldHeight;

	public GifDrawableSpan(Context context, Bitmap bitmap, SpanEntity entity,
			OnTextSpanClickListener listener) {
		mDrawable = getDrawable(context, bitmap);
		mEntity = entity;
		mListener = listener;
	}

	@Override
	public Drawable getDrawable() {
		return mDrawable;
	}

	@Override
	public int getSize(Paint paint, CharSequence text, int start, int end,
			Paint.FontMetricsInt fm) {
		Drawable d = getDrawable();
		Rect rect = d.getBounds();

		if (fm != null) {
			fm.ascent = -rect.bottom;
			fm.descent = 0;

			fm.top = fm.ascent;
			fm.bottom = 0;
		}

		return rect.right;
	}

	@Override
	public void draw(Canvas canvas, CharSequence text, int start, int end,
			float x, int top, int y, int bottom, Paint paint) {
		Drawable b = getDrawable();
		canvas.save();

		int transY = bottom - b.getBounds().bottom;
		if (mVerticalAlignment == ALIGN_BASELINE) {
			transY -= paint.getFontMetricsInt().descent;
		}

		canvas.translate(x, transY);
		b.draw(canvas);
		canvas.restore();
	}

	public void nextFrame(Context context, Bitmap bitmap) {
		mDrawable = getDrawable(context, bitmap);
	}

	public boolean isResetLayout() {
		return mIsResetLayout;
	}

	public void onClick(View widget) {
		if (mListener != null) {
			if (mEntity.spanType == SpanEntity.SPAN_TYPE_IMAGE)
				mListener.clickImage((GifTextView) widget, mEntity);
			else if (mEntity.spanType == SpanEntity.SPAN_TYPE_GIF)
				mListener.clickGif((GifTextView) widget, mEntity);
			else if (mEntity.spanType == SpanEntity.SPAN_TYPE_ZIP)
				mListener.clickZip((GifTextView) widget, mEntity);
		}
	}

	// **************************************************************
	// 私有方法

	private BitmapDrawable getDrawable(Context context, Bitmap bitmap) {
		BitmapDrawable drawable = new BitmapDrawable(context.getResources(),
				bitmap);
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		drawable.setBounds(0, 0, width > 0 ? width : 0, height > 0 ? height : 0);

		if (mOldWidth != width || mOldHeight != height) {
			mOldWidth = width;
			mOldHeight = height;
			mIsResetLayout = true;
		} else
			mIsResetLayout = false;

		return drawable;
	}

}
