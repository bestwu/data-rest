package cn.bestwu.framework.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

/**
 * 拼音处理的工具类
 *
 * @author Peter Wu
 */
public class PinyinUtil {
	public enum Case {
		UPPERCASE, LOWERCASE, CAPITALIZE
	}

	/**
	 * unicode编码范围： 汉字：[0x4E00,0x9FA5]（或十进制[19968,40869]）
	 * 数字：[0x0030,0x0039]（或十进制[48, 57]） 小写字母：[0x0061,0x007A]（或十进制[97, 122]）
	 * 大写字母：[0x0041,0x005A]（或十进制[65, 90]）
	 * <p>
	 * 返回汉字的拼音
	 *
	 * @param str  汉字(李莲英)
	 * @param caze 大小写
	 * @return 拼音 (LiLianYing)
	 */
	public static String getPinYin(String str, Case caze) {
		if (!org.springframework.util.StringUtils.hasText(str)) {
			return "";
		}
		// 拼音输出格式
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		// 全部小写
		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		// 设置声调格式:不要声调
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		// 设置特殊拼音ü的显示格式:用V (比如：绿)
		format.setVCharType(HanyuPinyinVCharType.WITH_V);

		StringBuilder pinyin = new StringBuilder();
		try {
			char[] chars = str.trim().toCharArray();
			for (char c : chars) {
				// 返回数组, 是因为可能是多音字, 比如"干" gan gang
				String[] array = PinyinHelper.toHanyuPinyinStringArray(c,
						format);
				if (array != null) {// 代表是汉字
					String string = array[0];
					if (caze == null) {
						caze = Case.CAPITALIZE;
					}
					switch (caze) {
					case UPPERCASE:
						string = string.toUpperCase();
						break;
					case LOWERCASE:
						string = string.toLowerCase();
						break;
					default:
						string = org.springframework.util.StringUtils.capitalize(string);
						break;
					}
					pinyin.append(string);
				} else {// 代表不是汉字, 其他字符
					pinyin.append(c);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pinyin.toString();
	}

	/**
	 * 是否为汉字
	 *
	 * @param c 字符
	 * @return 是否为汉字
	 */
	public boolean isCNChar(char c) {
		return Character.toString(c).matches("[\\u4E00-\\u9FA5]+");
	}

	/**
	 * 是否为大写字母
	 *
	 * @param capital capital
	 * @return 是否为大写字母
	 */
	public boolean isBigCapital(String capital) {
		return capital.matches("[\\u0041-\\u005A]+");
	}

	/**
	 * 是否为汉字字符串(只要包含了一个汉字)
	 *
	 * @param str 字符
	 * @return 是否为汉字字符串
	 */
	public boolean isCNStr(String str) {
		for (char c : str.toCharArray()) {
			if (isCNChar(c)) {// 如果有一个为汉字
				return true;
			}
		}
		// 如果没有一个汉字，全英文字符串
		return false;
	}

	/**
	 * 返回汉字拼音的声母
	 *
	 * @param str 汉字(李莲英)
	 * @return 汉字的头部字母 (LLY)
	 */
	public static String getPinYinHead(String str) {
		StringBuilder head = new StringBuilder();
		for (char c : str.toCharArray()) {
			// 得到拼音字符串
			// "干" ---->>> {"gan1", "gan4"}
			String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
			if (pinyinArray != null) { // 代表是汉字
				head.append(pinyinArray[0].charAt(0));
			} else { // 代表不是汉字
				head.append(c);
			}
		}
		return head.toString().toUpperCase();
	}

	/**
	 * 获得第一个字母
	 *
	 * @param str 干
	 * @return G
	 */
	public String getHeadChar(String str) {
		return getPinYinHead(str).substring(0, 1);
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

	/**
	 * 比较两个字符串的大小
	 *
	 * @param str1 字符1
	 * @param str2 字符2
	 * @return 1代表 str1 大于 str2; -1代表 str2 大于 str1 ;
	 */
	public int compare(String str1, String str2) {// 忽略大小写进行比较
		return getPinYin(str1, null).compareToIgnoreCase(getPinYin(str2, null));
	}

}
