package cn.bestwu.framework.util;

import org.springframework.util.Assert;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author Peter Wu
 */
public class DateUtil {
	/**
	 * @param timeMillis timeMillis
	 * @return 距现在多长时间
	 */
	public static String formatBefore(Long timeMillis) {
		Assert.notNull(timeMillis);

		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("0"));
		calendar.setTimeInMillis(System.currentTimeMillis() - timeMillis);
		int before = calendar.get(Calendar.YEAR) - 1970;
		if (before == 0) {
			before = calendar.get(Calendar.MONTH);
		} else {
			return before + "年前";
		}
		if (before == 0) {
			before = calendar.get(Calendar.DAY_OF_MONTH) - 1;
		} else {
			return before + "个月前";
		}
		if (before == 0) {
			before = calendar.get(Calendar.HOUR_OF_DAY);
		} else {
			return before + "天前";
		}
		if (before == 0) {
			before = calendar.get(Calendar.MINUTE);
		} else {
			return before + "个小时前";
		}
		if (before == 0) {
			before = calendar.get(Calendar.SECOND);
		} else {
			return before + "分钟前";
		}
		return before + "刚刚";

	}

}
