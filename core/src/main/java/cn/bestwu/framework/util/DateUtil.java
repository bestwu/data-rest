package cn.bestwu.framework.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 日期工具类
 *
 * @author Peter Wu
 */
public class DateUtil {

	/**
	 * 格式化时间
	 *
	 * @param millis  毫秒数
	 * @param pattern 格式
	 * @return 格式化后的字符
	 */
	public static String format(Long millis, String pattern) {
		return format(millis, ZoneOffset.of("+8"), DateTimeFormatter.ofPattern(pattern));
	}

	/**
	 * 格式化时间
	 *
	 * @param millis    毫秒数
	 * @param formatter 格式
	 * @return 格式化后的字符
	 */
	public static String format(Long millis, DateTimeFormatter formatter) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.of("+8")).format(formatter);
	}

	/**
	 * 格式化时间
	 *
	 * @param millis    毫秒数
	 * @param zone      时区
	 * @param formatter 格式
	 * @return 格式化后的字符
	 */
	public static String format(Long millis, ZoneId zone, DateTimeFormatter formatter) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), zone).format(formatter);
	}
}
