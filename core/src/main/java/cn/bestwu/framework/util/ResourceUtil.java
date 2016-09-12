package cn.bestwu.framework.util;

import cn.bestwu.framework.data.annotation.PathName;
import cn.bestwu.framework.rest.support.Version;
import lombok.extern.slf4j.Slf4j;
import org.atteo.evo.inflector.English;
import org.springframework.util.StringUtils;

/**
 * 资源 工具类
 *
 * @author Peter Wu
 */
@Slf4j
public class ResourceUtil {

	public static final String SEPARATOR = ",";

	/**
	 * 得到资源名
	 *
	 * @param clazz 类
	 * @return 资源名
	 */
	public static String getRepositoryBasePathName(Class<?> clazz) {
		PathName pathName = clazz.getAnnotation(PathName.class);
		if (pathName != null) {
			return pathName.value();
		}
		return English.plural(StringUtils.uncapitalize(clazz.getSimpleName()));
	}

	/**
	 * 请求版本号
	 */
	public static ThreadLocal<String> REQUEST_VERSION = new ThreadLocal<>();
	/**
	 * 请求签名
	 */
	public static ThreadLocal<String> API_SIGNATURE = new ThreadLocal<>();
	/**
	 * 请求方法
	 */
	public static ThreadLocal<String> REQUEST_METHOD = new ThreadLocal<>();

	/**
	 * @param verion 比较的版本号，不能为null
	 * @return 请求的版本号是否匹配 version
	 */
	public static boolean equalsVersion(String verion) {
		return Version.equals(verion, REQUEST_VERSION.get());
	}

}
