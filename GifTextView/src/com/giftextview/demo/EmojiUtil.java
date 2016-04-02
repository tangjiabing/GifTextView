package com.giftextview.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author tangjiabing
 * 
 * @see 开源时间：2016年03月30日
 * 
 *      记得给我个star哦~
 * 
 */
public class EmojiUtil {

	private static final String ee_static_000 = "[:D]";
	private static final String ee_static_001 = "[;)]";
	private static final String ee_static_002 = "[(|)]";
	private static final String ee_static_003 = "[(u)]";
	private static final String ee_static_004 = "[(S)]";
	private static final String ee_static_005 = "[(*)]";
	private static final String ee_static_006 = "[(#)]";
	private static final String ee_static_007 = "[(R)]";
	private static final String ee_static_008 = "[+o(]";
	private static final String ee_static_009 = "[:'(]";
	private static final String ee_static_010 = "[(k)]";
	private static final String ee_static_011 = "[:-#]";
	private static final String ee_static_012 = "[8-|]";
	private static final String ee_static_013 = "[(F)]";
	private static final String ee_static_014 = "[(W)]";
	private static final String ee_static_015 = "[(D)]";
	private static final String ee_static_016 = "[(K)]";
	private static final String ee_static_017 = "[:(]";
	private static final String ee_static_018 = "[:>]";
	private static final String ee_static_019 = "[({)]";
	private static final String ee_static_020 = "[:##]";
	private static final String ee_static_021 = "[(H)]";
	private static final String ee_static_022 = "[:s]";
	private static final String ee_static_023 = "[):]";
	private static final String ee_static_024 = "[:@]";
	private static final String ee_static_025 = "[:|]";
	private static final String ee_static_026 = "[8'<]";
	private static final String ee_static_027 = "[:'i]";
	private static final String ee_static_028 = "[8i]";
	private static final String ee_static_029 = "[:p]";
	private static final String ee_static_030 = "[::i]";
	private static final String ee_static_031 = "[:i:]";
	private static final String ee_static_032 = "[-})]";
	private static final String ee_static_033 = "[:-o]";
	private static final String ee_static_034 = "[^?-o]";
	private static final String ee_static_035 = "[s|-o]";
	private static final String ee_static_036 = "[(})]";
	private static final String ee_static_037 = "[*)h]";
	private static final String ee_static_038 = "[^|m*]";
	private static final String ee_static_039 = "[s^|%]";
	private static final String ee_static_040 = "[*-)]";
	private static final String ee_static_041 = "[^o)]";
	private static final String ee_static_042 = "[^o*)]";
	private static final String ee_static_043 = "[8-)]";
	private static final String ee_static_044 = "[^o})]";
	private static final String ee_static_045 = "[|-)]";
	private static final String ee_static_046 = "[8o|]";
	private static final String ee_static_047 = "[{o|]";
	private static final String ee_static_048 = "[{|o*}]";
	private static final String ee_static_049 = "[{o|*o}]";
	private static final String ee_static_050 = "[^n|*o}]";
	private static final String ee_static_051 = "[^#|*}]";
	private static final String ee_static_052 = "[*#*k}]";
	private static final String ee_static_053 = "[(#3*}]";
	private static final String ee_static_054 = "[4d*)]";
	private static final String ee_static_055 = "[o@d*)]";
	private static final String ee_static_056 = "[o6i*|]";
	private static final String ee_static_057 = "[1o^i*(]";
	private static final String ee_static_058 = "[2g^j)}]";
	private static final String ee_static_059 = "[ko*h)}]";

	private static final Map<Pattern, String> emojiMap = new HashMap<Pattern, String>();

	static {
		loadIcon();
	}

	public static String convert(String emojiText) {
		for (Entry<Pattern, String> entry : emojiMap.entrySet()) {
			Matcher matcher = entry.getKey().matcher(emojiText);
			if (matcher.find())
				emojiText = matcher.replaceAll(entry.getValue());
		}
		return emojiText;
	}

	public static String remakeEmojiText(String emojiText) {
		for (Entry<Pattern, String> entry : emojiMap.entrySet()) {
			Matcher matcher = entry.getKey().matcher(emojiText);
			if (matcher.find()) {
				String key = entry.getKey().toString();
				key = key.substring("\\Q".length(),
						key.length() - "\\E".length());
				String newKey = "<><5\\$" + key + "><>";
				emojiText = matcher.replaceAll(newKey);
			}
		}
		return emojiText;
	}

	private static void addPattern(Map<Pattern, String> map, String emoji,
			String resource) {
		map.put(Pattern.compile(Pattern.quote(emoji)), resource);
	}

	private static void loadIcon() {
		addPattern(emojiMap, ee_static_000, "ee_000");
		addPattern(emojiMap, ee_static_001, "ee_001");
		addPattern(emojiMap, ee_static_002, "ee_002");
		addPattern(emojiMap, ee_static_003, "ee_003");
		addPattern(emojiMap, ee_static_004, "ee_004");
		addPattern(emojiMap, ee_static_005, "ee_005");
		addPattern(emojiMap, ee_static_006, "ee_006");
		addPattern(emojiMap, ee_static_007, "ee_007");
		addPattern(emojiMap, ee_static_008, "ee_008");
		addPattern(emojiMap, ee_static_009, "ee_009");
		addPattern(emojiMap, ee_static_010, "ee_010");
		addPattern(emojiMap, ee_static_011, "ee_011");
		addPattern(emojiMap, ee_static_012, "ee_012");
		addPattern(emojiMap, ee_static_013, "ee_013");
		addPattern(emojiMap, ee_static_014, "ee_014");
		addPattern(emojiMap, ee_static_015, "ee_015");
		addPattern(emojiMap, ee_static_016, "ee_016");
		addPattern(emojiMap, ee_static_017, "ee_017");
		addPattern(emojiMap, ee_static_018, "ee_018");
		addPattern(emojiMap, ee_static_019, "ee_019");
		addPattern(emojiMap, ee_static_020, "ee_020");
		addPattern(emojiMap, ee_static_021, "ee_021");
		addPattern(emojiMap, ee_static_022, "ee_022");
		addPattern(emojiMap, ee_static_023, "ee_023");
		addPattern(emojiMap, ee_static_024, "ee_024");
		addPattern(emojiMap, ee_static_025, "ee_025");
		addPattern(emojiMap, ee_static_026, "ee_026");
		addPattern(emojiMap, ee_static_027, "ee_027");
		addPattern(emojiMap, ee_static_028, "ee_028");
		addPattern(emojiMap, ee_static_029, "ee_029");
		addPattern(emojiMap, ee_static_030, "ee_030");
		addPattern(emojiMap, ee_static_031, "ee_031");
		addPattern(emojiMap, ee_static_032, "ee_032");
		addPattern(emojiMap, ee_static_033, "ee_033");
		addPattern(emojiMap, ee_static_034, "ee_034");
		addPattern(emojiMap, ee_static_035, "ee_035");
		addPattern(emojiMap, ee_static_036, "ee_036");
		addPattern(emojiMap, ee_static_037, "ee_037");
		addPattern(emojiMap, ee_static_038, "ee_038");
		addPattern(emojiMap, ee_static_039, "ee_039");
		addPattern(emojiMap, ee_static_040, "ee_040");
		addPattern(emojiMap, ee_static_041, "ee_041");
		addPattern(emojiMap, ee_static_042, "ee_042");
		addPattern(emojiMap, ee_static_043, "ee_043");
		addPattern(emojiMap, ee_static_044, "ee_044");
		addPattern(emojiMap, ee_static_045, "ee_045");
		addPattern(emojiMap, ee_static_046, "ee_046");
		addPattern(emojiMap, ee_static_047, "ee_047");
		addPattern(emojiMap, ee_static_048, "ee_048");
		addPattern(emojiMap, ee_static_049, "ee_049");
		addPattern(emojiMap, ee_static_050, "ee_050");
		addPattern(emojiMap, ee_static_051, "ee_051");
		addPattern(emojiMap, ee_static_052, "ee_052");
		addPattern(emojiMap, ee_static_053, "ee_053");
		addPattern(emojiMap, ee_static_054, "ee_054");
		addPattern(emojiMap, ee_static_055, "ee_055");
		addPattern(emojiMap, ee_static_056, "ee_056");
		addPattern(emojiMap, ee_static_057, "ee_057");
		addPattern(emojiMap, ee_static_058, "ee_058");
		addPattern(emojiMap, ee_static_059, "ee_059");
	}

}
