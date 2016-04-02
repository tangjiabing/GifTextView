package com.giftextview.span;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
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
public class LinkClickableSpan extends ClickableSpan {

	private SpanEntity entity = null;
	private OnTextSpanClickListener listener = null;

	public LinkClickableSpan(SpanEntity entity, OnTextSpanClickListener listener) {
		this.entity = entity;
		this.listener = listener;
	}

	@Override
	public void onClick(View widget) {
		if (listener != null)
			listener.clickLink((GifTextView) widget, entity);
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setColor(ds.linkColor);
	}

}
