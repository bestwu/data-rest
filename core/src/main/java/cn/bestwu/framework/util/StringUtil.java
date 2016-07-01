package cn.bestwu.framework.util;

import cn.bestwu.framework.rest.converter.DefaultElementMixIn;
import cn.bestwu.framework.rest.converter.PageMixIn;
import cn.bestwu.framework.rest.converter.ResponseEntityMixIn;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Locale;

/**
 * 字符串工具类
 *
 * @author Peter Wu
 */
@Slf4j
public class StringUtil {

	private static ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
		objectMapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES, SerializationFeature.FAIL_ON_EMPTY_BEANS);
		if (ClassUtils.isPresent("org.springframework.data.domain.Page", StringUtil.class.getClassLoader()))
			objectMapper.addMixIn(Page.class, PageMixIn.class);
		objectMapper.addMixIn(Object.class, DefaultElementMixIn.class);
		objectMapper.addMixIn(ResponseEntity.class, ResponseEntityMixIn.class);
		objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
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
		return valueOf(object, false);
	}

	/**
	 * 转换为字符串
	 *
	 * @param object 对象
	 * @param format 是否格式化输出
	 * @return 字符串
	 */
	public static String valueOf(Object object, boolean format) {
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
				b.append(valueOf(Array.get(object, i), format));
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
				b.append(valueOf(e, format));
				if (!es.hasMoreElements())
					return b.append(']').toString();
				b.append(", ");
			}
		} else {
			try {
				if (format)
					objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
				String string = null;
				if (object instanceof MappingJacksonValue) {
					MappingJacksonValue container = (MappingJacksonValue) object;
					Class<?> serializationView = container.getSerializationView();
					object = container.getValue();
					if (serializationView != null) {
						string = objectMapper.writerWithView(serializationView).writeValueAsString(object);
					}
				}
				if (string == null)
					string = objectMapper.writeValueAsString(object);
				if (format)
					objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
				return string;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return String.valueOf(object);
	}

	/**
	 * 截取一定长度的字符
	 *
	 * @param str    字符串
	 * @param length 长度
	 * @return 截取后的字符串
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

	/**
	 * 截取一定长度的字符，结果以...结尾
	 *
	 * @param str    字符串
	 * @param length 长度
	 * @return 截取后的字符串
	 */
	public static String subStringWithEllipsis(String str, int length) {
		if (str == null) {
			return null;
		}
		int l = str.length();
		if (l > length) {
			return str.substring(0, length - 3) + "...";
		} else {
			return str;
		}
	}

}
