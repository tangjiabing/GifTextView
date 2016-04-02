package com.giftextview.span;

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
public interface OnTextSpanClickListener {

	public void clickLink(GifTextView gifTextView, SpanEntity entity);

	public void clickImage(GifTextView gifTextView, SpanEntity entity);

	public void clickGif(GifTextView gifTextView, SpanEntity entity);

	public void clickZip(GifTextView gifTextView, SpanEntity entity);

}
