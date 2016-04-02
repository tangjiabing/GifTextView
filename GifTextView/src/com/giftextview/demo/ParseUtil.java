package com.giftextview.demo;

import java.util.ArrayList;

import android.text.Html;

import com.giftextview.entity.SpanEntity;

/**
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年03月30日
 * 
 *      记得给我个star哦~
 * 
 */
public class ParseUtil {

	public static final int CONTENT_TYPE_VIDEO = 0x101;
	public static final int CONTENT_TYPE_AUDIO = 0x102;
	public static final int CONTENT_TYPE_TEXT = 0x103;

	public static ArrayList<ViewDataEntity> parsingByAutoSplit(String data) {
		if (data == null || "".equals(data))
			return null;
		ArrayList<ViewDataEntity> resultEntityList = new ArrayList<ViewDataEntity>();
		byte[] b3 = { 3 };
		String chr3 = new String(b3);
		StringBuilder builder = new StringBuilder();
		String[] lineArray = data.split("<n>");
		for (int k = 0; k < lineArray.length; k++) {
			builder.delete(0, builder.length());
			ArrayList<SpanEntity> spanList = new ArrayList<SpanEntity>();
			String[] array = lineArray[k].split("<>");
			for (int i = 0; i < array.length; i++) {
				if (array[i].startsWith("<") && array[i].endsWith(">")) {
					String type = array[i].substring(array[i].indexOf("<") + 1,
							array[i].indexOf("$"));
					String url = array[i].substring(array[i].indexOf("$") + 1,
							array[i].lastIndexOf(">"));
					if (array[i].contains("$")) {
						if ("1".equals(type)) { // 链接
							String str = array[i].substring(
									array[i].indexOf("><") + 2,
									array[i].lastIndexOf(">"));
							int start = builder.length();
							int end = start + str.length();
							builder.append(str);
							int spanType = SpanEntity.SPAN_TYPE_LINK;
							addSpanEntity(spanList, url, start, end, spanType);
						} else if ("2".equals(type)) { // 图片
							// 文本末尾添加中文空格符，就不会因为图片的宽度大于剩余宽度而使得最末尾的一个文字也需要随图片进行换行
							builder.append(" ");
							int start = builder.length();
							builder.append(" "); // 用空格符替换成图片
							int end = start + 1;
							String suffix = url.substring(
									url.lastIndexOf(".") + 1, url.length());
							int spanType = SpanEntity.SPAN_TYPE_IMAGE;
							if ("gif".equalsIgnoreCase(suffix))
								spanType = SpanEntity.SPAN_TYPE_GIF;
							addSpanEntity(spanList, url, start, end, spanType);
							// 防止图片在所在行的宽度已经不够的情况下自动隐藏掉而不换行显示，
							// 多添加了一个中文空格符，当第二张图片的宽度大于剩余宽度时，不会使第一张图片也跟着换行
							builder.append(chr3 + " ");
						} else if ("3".equals(type)) { // 音频
							int length = builder.length();
							if (length > 0) {
								ViewDataEntity resultEntity = new ViewDataEntity();
								resultEntity.contentType = CONTENT_TYPE_TEXT;
								if (!spanList.isEmpty())
									resultEntity.spanList = spanList;
								resultEntity.text = builder.toString();
								resultEntityList.add(resultEntity);
								builder.delete(0, length);
								spanList = new ArrayList<SpanEntity>();
							}
							ViewDataEntity resultEntity = new ViewDataEntity();
							resultEntity.contentType = CONTENT_TYPE_AUDIO;
							resultEntity.mediaUrl = url;
							resultEntityList.add(resultEntity);
						} else if ("4".equals(type)) { // 视频
							int length = builder.length();
							if (length > 0) {
								ViewDataEntity resultEntity = new ViewDataEntity();
								resultEntity.contentType = CONTENT_TYPE_TEXT;
								if (!spanList.isEmpty())
									resultEntity.spanList = spanList;
								resultEntity.text = builder.toString();
								resultEntityList.add(resultEntity);
								builder.delete(0, length);
								spanList = new ArrayList<SpanEntity>();
							}
							ViewDataEntity resultEntity = new ViewDataEntity();
							resultEntity.contentType = CONTENT_TYPE_VIDEO;
							resultEntity.mediaUrl = url;
							resultEntityList.add(resultEntity);
						} else if ("5".equals(type)) { // 表情
							// 文本末尾添加中文空格符，就不会因为图片的宽度大于剩余宽度而使得最末尾的一个文字也需要随图片进行换行
							builder.append(" ");
							int start = builder.length();
							builder.append(" "); // 用空格符替换成图片
							int end = start + 1;
							int spanType = SpanEntity.SPAN_TYPE_EMOJI;
							url = EmojiUtil.convert(url);
							addSpanEntity(spanList, url, start, end, spanType);
							// 防止图片在所在行的宽度已经不够的情况下自动隐藏掉而不换行显示，
							// 多添加了一个中文空格符，当第二张图片的宽度大于剩余宽度时，不会使第一张图片也跟着换行
							builder.append(chr3 + " ");
						} else if ("6".equals(type)) { // 附件
							// 文本末尾添加中文空格符，就不会因为图片的宽度大于剩余宽度而使得最末尾的一个文字也需要随图片进行换行
							builder.append(" ");
							int start = builder.length();
							builder.append(" "); // 用空格符替换成图片
							int end = start + 1;
							int spanType = SpanEntity.SPAN_TYPE_ZIP;
							addSpanEntity(spanList, url, start, end, spanType);
							// 防止图片在所在行的宽度已经不够的情况下自动隐藏掉而不换行显示，
							// 多添加了一个中文空格符，当第二张图片的宽度大于剩余宽度时，不会使第一张图片也跟着换行
							builder.append(chr3 + " ");
						}
					}
				} else {
					String str = Html.fromHtml(array[i]).toString();
					builder.append(str);
				}
			}
			if (builder.length() == 0) {
				builder.append("\n"); // 补回之前因以<n>分割而被去掉的换行符
				if (!resultEntityList.isEmpty()) {
					ViewDataEntity resultEntity = resultEntityList
							.get(resultEntityList.size() - 1);
					resultEntity.text = resultEntity.text + builder.toString();
					continue;
				} else {
					if (k + 1 < lineArray.length) {
						lineArray[k + 1] = builder.toString()
								+ lineArray[k + 1];
						continue;
					}
				}
			}
			ViewDataEntity resultEntity = new ViewDataEntity();
			resultEntity.contentType = CONTENT_TYPE_TEXT;
			if (!spanList.isEmpty())
				resultEntity.spanList = spanList;
			resultEntity.text = builder.toString();
			resultEntityList.add(resultEntity);
		}
		return resultEntityList;
	}

	public static ArrayList<ViewDataEntity> parsing(String data) {
		if (data == null || "".equals(data))
			return null;
		ArrayList<ViewDataEntity> resultEntityList = new ArrayList<ViewDataEntity>();
		if (data != null) {
			byte[] b3 = { 3 };
			String chr3 = new String(b3);
			StringBuilder builder = new StringBuilder();
			ArrayList<SpanEntity> spanList = new ArrayList<SpanEntity>();
			String[] array = data.split("<>");
			for (int i = 0; i < array.length; i++) {
				array[i] = array[i].replaceAll("<n>", "\n");
				if (array[i].startsWith("<") && array[i].endsWith(">")) {
					String type = array[i].substring(array[i].indexOf("<") + 1,
							array[i].indexOf("$"));
					String url = array[i].substring(array[i].indexOf("$") + 1,
							array[i].lastIndexOf(">"));
					if (array[i].contains("$")) {
						if ("1".equals(type)) { // 链接
							String str = array[i].substring(
									array[i].indexOf("><") + 2,
									array[i].lastIndexOf(">"));
							int start = builder.length();
							int end = start + str.length();
							builder.append(str);
							int spanType = SpanEntity.SPAN_TYPE_LINK;
							addSpanEntity(spanList, url, start, end, spanType);
						} else if ("2".equals(type)) { // 图片
							// 文本末尾添加中文空格符，就不会因为图片的宽度大于剩余宽度而使得最末尾的一个文字也需要随图片进行换行
							builder.append(" ");
							int start = builder.length();
							builder.append(" "); // 用空格符替换成图片
							int end = start + 1;
							String suffix = url.substring(
									url.lastIndexOf(".") + 1, url.length());
							int spanType = SpanEntity.SPAN_TYPE_IMAGE;
							if ("gif".equalsIgnoreCase(suffix))
								spanType = SpanEntity.SPAN_TYPE_GIF;
							addSpanEntity(spanList, url, start, end, spanType);
							// 防止图片在所在行的宽度已经不够的情况下自动隐藏掉而不换行显示，
							// 多添加了一个中文空格符，当第二张图片的宽度大于剩余宽度时，不会使第一张图片也跟着换行
							builder.append(chr3 + " ");
						} else if ("3".equals(type)) { // 音频
							int length = builder.length();
							if (length > 0) {
								ViewDataEntity resultEntity = new ViewDataEntity();
								resultEntity.contentType = CONTENT_TYPE_TEXT;
								if (!spanList.isEmpty())
									resultEntity.spanList = spanList;
								resultEntity.text = builder.toString();
								resultEntityList.add(resultEntity);
								builder.delete(0, length);
								spanList = new ArrayList<SpanEntity>();
							}
							ViewDataEntity resultEntity = new ViewDataEntity();
							resultEntity.contentType = CONTENT_TYPE_AUDIO;
							resultEntity.mediaUrl = url;
							resultEntityList.add(resultEntity);
						} else if ("4".equals(type)) { // 视频
							int length = builder.length();
							if (length > 0) {
								ViewDataEntity resultEntity = new ViewDataEntity();
								resultEntity.contentType = CONTENT_TYPE_TEXT;
								if (!spanList.isEmpty())
									resultEntity.spanList = spanList;
								resultEntity.text = builder.toString();
								resultEntityList.add(resultEntity);
								builder.delete(0, length);
								spanList = new ArrayList<SpanEntity>();
							}
							ViewDataEntity resultEntity = new ViewDataEntity();
							resultEntity.contentType = CONTENT_TYPE_VIDEO;
							resultEntity.mediaUrl = url;
							resultEntityList.add(resultEntity);
						} else if ("5".equals(type)) { // 表情
							// 文本末尾添加中文空格符，就不会因为图片的宽度大于剩余宽度而使得最末尾的一个文字也需要随图片进行换行
							builder.append(" ");
							int start = builder.length();
							builder.append(" "); // 用空格符替换成图片
							int end = start + 1;
							int spanType = SpanEntity.SPAN_TYPE_EMOJI;
							url = EmojiUtil.convert(url);
							addSpanEntity(spanList, url, start, end, spanType);
							// 防止图片在所在行的宽度已经不够的情况下自动隐藏掉而不换行显示，
							// 多添加了一个中文空格符，当第二张图片的宽度大于剩余宽度时，不会使第一张图片也跟着换行
							builder.append(chr3 + " ");
						} else if ("6".equals(type)) { // 附件
							// 文本末尾添加中文空格符，就不会因为图片的宽度大于剩余宽度而使得最末尾的一个文字也需要随图片进行换行
							builder.append(" ");
							int start = builder.length();
							builder.append(" "); // 用空格符替换成图片
							int end = start + 1;
							int spanType = SpanEntity.SPAN_TYPE_ZIP;
							addSpanEntity(spanList, url, start, end, spanType);
							// 防止图片在所在行的宽度已经不够的情况下自动隐藏掉而不换行显示，
							// 多添加了一个中文空格符，当第二张图片的宽度大于剩余宽度时，不会使第一张图片也跟着换行
							builder.append(chr3 + " ");
						}
					}
				} else {
					array[i] = array[i].replaceAll("\n", "<br/>"); // 防止“\n”被去掉
					String str = Html.fromHtml(array[i]).toString();
					builder.append(str);
				}
			}
			ViewDataEntity resultEntity = new ViewDataEntity();
			resultEntity.contentType = CONTENT_TYPE_TEXT;
			if (!spanList.isEmpty())
				resultEntity.spanList = spanList;
			resultEntity.text = builder.toString();
			resultEntityList.add(resultEntity);
		}
		return resultEntityList;
	}

	private static void addSpanEntity(ArrayList<SpanEntity> spanList,
			String url, int start, int end, int spanType) {
		boolean isExist = false;
		for (SpanEntity entity : spanList) {
			if (entity.spanType == spanType) {
				if (entity.url.equals(url)) {
					entity.starts.add(start);
					entity.ends.add(end);
					isExist = true;
					break;
				}
			}
		}
		if (isExist == false) {
			SpanEntity spanEntity = new SpanEntity();
			spanEntity.spanType = spanType;
			spanEntity.starts = new ArrayList<Integer>(5);
			spanEntity.starts.add(start);
			spanEntity.ends = new ArrayList<Integer>(5);
			spanEntity.ends.add(end);
			spanEntity.url = url;
			spanList.add(spanEntity);
		}
	}

	// *********************************************************************
	// 自定义的类

	public static class ViewDataEntity {
		public int contentType = 0;
		public ArrayList<SpanEntity> spanList = null;
		public String mediaUrl = null;
		public String text = null;
	}

}
