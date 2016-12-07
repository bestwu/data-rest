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
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

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

	/**
	 * 计算字符串包含子字符串的个数
	 *
	 * @param str 字符串
	 * @param sub 子字符串
	 * @return 个数
	 */
	public static int countSubString(String str, String sub) {
		if (str.contains(sub)) {
			return splitWorker(str, sub, -1, false).length - 1;
		} else {
			return 0;
		}
	}

	/**
	 * 分割字符串
	 *
	 * @param str               字符串
	 * @param separatorChars    分隔符
	 * @param max               最大数量
	 * @param preserveAllTokens preserveAllTokens
	 * @return 分割后数组
	 */
	private static String[] splitWorker(final String str, final String separatorChars, final int max, final boolean preserveAllTokens) {
		// Performance tuned for 2.0 (JDK1.4)
		// Direct code is quicker than StringTokenizer.
		// Also, StringTokenizer uses isSpace() not isWhitespace()

		if (str == null) {
			return null;
		}
		final int len = str.length();
		if (len == 0) {
			return new String[0];
		}
		final List<String> list = new ArrayList<>();
		int sizePlus1 = 1;
		int i = 0, start = 0;
		boolean match = false;
		boolean lastMatch = false;
		if (separatorChars == null) {
			// Null separator means use whitespace
			while (i < len) {
				if (Character.isWhitespace(str.charAt(i))) {
					if (match || preserveAllTokens) {
						lastMatch = true;
						if (sizePlus1++ == max) {
							i = len;
							lastMatch = false;
						}
						list.add(str.substring(start, i));
						match = false;
					}
					start = ++i;
					continue;
				}
				lastMatch = false;
				match = true;
				i++;
			}
		} else if (separatorChars.length() == 1) {
			// Optimise 1 character case
			final char sep = separatorChars.charAt(0);
			while (i < len) {
				if (str.charAt(i) == sep) {
					if (match || preserveAllTokens) {
						lastMatch = true;
						if (sizePlus1++ == max) {
							i = len;
							lastMatch = false;
						}
						list.add(str.substring(start, i));
						match = false;
					}
					start = ++i;
					continue;
				}
				lastMatch = false;
				match = true;
				i++;
			}
		} else {
			// standard case
			while (i < len) {
				if (separatorChars.indexOf(str.charAt(i)) >= 0) {
					if (match || preserveAllTokens) {
						lastMatch = true;
						if (sizePlus1++ == max) {
							i = len;
							lastMatch = false;
						}
						list.add(str.substring(start, i));
						match = false;
					}
					start = ++i;
					continue;
				}
				lastMatch = false;
				match = true;
				i++;
			}
		}
		if (match || preserveAllTokens && lastMatch) {
			list.add(str.substring(start, i));
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * 压缩字符
	 *
	 * @param str 待压缩字符
	 * @return 压缩后字符
	 */
	public static String compress(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DeflaterOutputStream gzip = new DeflaterOutputStream(out);
			gzip.write(str.getBytes());
			gzip.close();
			return new String(out.toByteArray(), "ISO-8859-1");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public static String decompress(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}
		try {
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
			InflaterInputStream zipInputStream = new InflaterInputStream(byteArrayInputStream);
			return StreamUtils.copyToString(zipInputStream, Charset.forName("ISO-8859-1"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
