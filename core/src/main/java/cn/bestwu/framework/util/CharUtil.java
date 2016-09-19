package cn.bestwu.framework.util;

/**
 * 字符工具类
 *
 * @author Peter Wu
 */
public class CharUtil {

	/**
	 * 是否为汉字
	 *
	 * @param c 字符
	 * @return 是否为汉字
	 */

	public static boolean isCNChar(char c) {
		return Character.toString(c).matches("[\\u4E00-\\u9FA5]+");
	}

	/**
	 * 是否为大写字母
	 *
	 * @param capital capital
	 * @return 是否为大写字母
	 */
	public static boolean isBigCapital(String capital) {
		return capital.matches("[\\u0041-\\u005A]+");
	}

	/**
	 * 是否为汉字字符串(只要包含了一个汉字)
	 *
	 * @param str 字符
	 * @return 是否为汉字字符串
	 */
	public static boolean hasCNStr(String str) {
		for (char c : str.toCharArray()) {
			if (isCNChar(c)) {// 如果有一个为汉字
				return true;
			}
		}
		// 如果没有一个汉字，全英文字符串
		return false;
	}

	/**
	 * 将字符串转移为ASCII码
	 *
	 * @param str 字符串
	 * @return ASCII码
	 */
	public String getCnASCII(String str) {
		StringBuilder sb = new StringBuilder();
		byte[] strByte = str.getBytes();
		for (byte aStrByte : strByte) {
			sb.append(Integer.toHexString(aStrByte & 0xff));
		}
		return sb.toString();
	}
}
