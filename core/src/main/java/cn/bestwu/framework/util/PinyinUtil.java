package cn.bestwu.framework.util;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 拼音处理的工具类
 *
 * @author Peter Wu
 */
@Slf4j
public class PinyinUtil {

	/**
	 * unicode编码范围： 汉字：[0x4E00,0x9FA5]（或十进制[19968,40869]）
	 * 数字：[0x0030,0x0039]（或十进制[48, 57]） 小写字母：[0x0061,0x007A]（或十进制[97, 122]）
	 * 大写字母：[0x0041,0x005A]（或十进制[65, 90]）
	 * <p>
	 * 返回汉字的拼音
	 *
	 * @param str 汉字(李莲英)
	 * @return 拼音 (LiLianYing)
	 */
	public static String getPinYin(String str) {
		if (!StringUtils.hasText(str)) {
			return null;
		}
		try {
			char[] chs = str.toCharArray();
			StringBuilder result = new StringBuilder();
			for (int i = 0; i < chs.length; i++) {
				String[] pinyins = chineseToPinYin(chs[i]);
				if (pinyins == null) {
					result.append("");
				} else if (pinyins.length == 1) {
					result.append(StringUtils.capitalize(pinyins[0]));
				} else {
					String prim = str.substring(i, i + 1);

					String lst = null, rst = null;

					if (i <= str.length() - 2) {
						rst = str.substring(i, i + 2);
					}
					if (i >= 1 && i + 1 <= str.length()) {
						lst = str.substring(i - 1, i + 1);
					}

					String answer = null;
					for (String py : pinyins) {

						if (StringUtils.isEmpty(py)) {
							continue;
						}

						if ((lst != null && py.equals(dictionary.get(lst))) || (rst != null && py.equals(dictionary.get(rst)))) {
							answer = py;
							break;
						}

						if (py.equals(dictionary.get(prim))) {
							answer = py;
						}
					}
					if (answer == null)
						answer = pinyins[0];
					result.append(StringUtils.capitalize(answer));
				}
			}

			return result.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

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
	public boolean isBigCapital(String capital) {
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
	 * 返回汉字拼音的声母
	 *
	 * @param str 汉字(李莲英)
	 * @return 汉字的头部字母 (lly)
	 */
	public static String getPinYinHead(String str) {
		try {
			if (StringUtils.isEmpty(str)) {
				return null;
			}

			char[] chs = str.toCharArray();

			StringBuilder result = new StringBuilder();

			for (int i = 0; i < chs.length; i++) {
				String[] pinyins = chineseToPinYin(chs[i]);
				if (pinyins == null) {
					result.append("");
				} else if (pinyins.length == 1) {
					result.append(pinyins[0].charAt(0));
				} else {

					String prim = str.substring(i, i + 1);

					String lst = null, rst = null;

					if (i <= str.length() - 2) {
						rst = str.substring(i, i + 2);
					}
					if (i >= 1 && i + 1 <= str.length()) {
						lst = str.substring(i - 1, i + 1);
					}

					String answer = null;
					for (String py : pinyins) {

						if (StringUtils.isEmpty(py)) {
							continue;
						}

						if ((lst != null && py.equals(dictionary.get(lst))) || (rst != null && py.equals(dictionary.get(rst)))) {
							answer = py;
							break;
						}

						if (py.equals(dictionary.get(prim))) {
							answer = py;
						}
					}
					if (answer == null)
						answer = pinyins[0];
					result.append(answer.charAt(0));
				}
			}

			return result.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
		return getPinYin(str1).compareToIgnoreCase(getPinYin(str2));
	}

	/**
	 * 多音字词典
	 */
	public static Map<String, String> dictionary = new HashMap<>();

	//加载多音字词典
	static {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(PinyinUtil.class.getResourceAsStream("/duoyinzi_pinyin.txt"), "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) {
				String[] arr = line.split("#");
				if (StringUtils.hasText(arr[1])) {
					String[] sems = arr[1].split(" ");
					for (String sem : sems) {
						if (StringUtils.hasText(sem)) {
							dictionary.put(sem, arr[0]);
						}
					}
				}
			}

		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}

	}

	private static String[] chineseToPinYin(char chineseCharacter) throws BadHanyuPinyinOutputFormatCombination {
		HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
		outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		outputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);

		if (isCNChar(chineseCharacter)) {
			return PinyinHelper.toHanyuPinyinStringArray(chineseCharacter, outputFormat);
		} else {
			return new String[] { String.valueOf(chineseCharacter) };
		}
	}

}
