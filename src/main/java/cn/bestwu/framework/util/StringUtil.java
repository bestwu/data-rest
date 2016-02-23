package cn.bestwu.framework.util;

import cn.bestwu.framework.rest.converter.DefaultElementMixIn;
import cn.bestwu.framework.rest.converter.PageMixIn;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * 字符串工具类
 *
 * @author Peter Wu
 */
public class StringUtil {

	private static Logger logger = LoggerFactory.getLogger(StringUtil.class);
	private static ObjectMapper objectMapper = new ObjectMapper();

	static {
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		builder.serializationInclusion(JsonInclude.Include.NON_NULL);
		builder.featuresToEnable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
		builder.featuresToDisable(SerializationFeature.WRITE_NULL_MAP_VALUES, SerializationFeature.FAIL_ON_EMPTY_BEANS);
		builder.mixIn(PageImpl.class, PageMixIn.class);
		builder.mixIn(Map.class, DefaultElementMixIn.class);
		builder.configure(objectMapper);
	}

	/**
	 * @param s 字符串
	 * @return 转换为带下划线的小写字符
	 */
	public static String addUnderscores(String s) {
		StringBuilder buf = new StringBuilder(s.replace('.', '_'));
		for (int i = 1; i < buf.length() - 1; i++) {
			if (
					Character.isLowerCase(buf.charAt(i - 1)) &&
							Character.isUpperCase(buf.charAt(i)) &&
							Character.isLowerCase(buf.charAt(i + 1))
					) {
				buf.insert(i++, '_');
			}
		}
		return buf.toString().toLowerCase(Locale.ROOT);
	}

	/**
	 * 转换为字符串
	 *
	 * @param object 对象
	 * @return 字符串
	 */
	public static String valueOf(Object object) {
		if (object == null)
			return "null";
		Class<?> clazz = object.getClass();
		if (clazz.isArray()) {
			int length = Array.getLength(object);
			int iMax = length - 1;
			if (iMax == -1)
				return "[]";

			StringBuilder b = new StringBuilder();
			b.append('[');
			for (int i = 0; ; i++) {
				b.append(valueOf(Array.get(object, i)));
				if (i == iMax)
					return b.append(']').toString();
				b.append(", ");
			}
		} else if (Enumeration.class.isAssignableFrom(clazz)) {
			Enumeration<?> es = (Enumeration<?>) object;
			if (!es.hasMoreElements()) {
				return "[]";
			}
			StringBuilder b = new StringBuilder();
			b.append('[');
			while (es.hasMoreElements()) {
				Object e = es.nextElement();
				b.append(valueOf(e));
				if (!es.hasMoreElements())
					return b.append(']').toString();
				b.append(", ");
			}
		} else {
			try {
				return objectMapper.writeValueAsString(object);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return String.valueOf(object);
	}

	/*
	 * 截取一定长度的字符
	 */
	public static String subString(String str, int length) {
		if (str == null) {
			return null;
		}
		int l = str.length();
		if (l > length) {
			return str.substring(0, length);
		} else {
			return str;
		}
	}

}
