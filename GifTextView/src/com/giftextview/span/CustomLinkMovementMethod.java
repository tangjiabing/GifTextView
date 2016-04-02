package com.giftextview.span;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年03月30日
 * 
 *      记得给我个star哦~
 * 
 */
public class CustomLinkMovementMethod extends LinkMovementMethod {
	@Override
	public boolean onTouchEvent(TextView widget, Spannable buffer,
			MotionEvent event) {

		int action = event.getAction();

		if (action == MotionEvent.ACTION_UP
				|| action == MotionEvent.ACTION_DOWN) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			x -= widget.getTotalPaddingLeft();
			y -= widget.getTotalPaddingTop();

			x += widget.getScrollX();
			y += widget.getScrollY();

			Layout layout = widget.getLayout();
			int line = layout.getLineForVertical(y);
			int off = layout.getOffsetForHorizontal(line, x);

			ClickableSpan[] link = buffer.getSpans(off, off,
					ClickableSpan.class);
			GifDrawableSpan[] image = buffer.getSpans(off, off,
					GifDrawableSpan.class);

			if (link.length != 0) {
				if (action == MotionEvent.ACTION_UP) {
					link[0].onClick(widget);
					Selection.removeSelection(buffer);
				} else if (action == MotionEvent.ACTION_DOWN) {
					Selection.setSelection(buffer,
							buffer.getSpanStart(link[0]),
							buffer.getSpanEnd(link[0]));
				}

				return true;
			} else if (image.length != 0) {
				if (action == MotionEvent.ACTION_UP) {
					image[0].onClick(widget);
					Selection.removeSelection(buffer);
				} else if (action == MotionEvent.ACTION_DOWN) {
					Selection.setSelection(buffer,
							buffer.getSpanStart(image[0]),
							buffer.getSpanEnd(image[0]));
				}

				return true;
			} else {
				Selection.removeSelection(buffer);
			}
		}

		return super.onTouchEvent(widget, buffer, event);
	}
}
