package cn.bestwu.framework.util;

import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 参数工具类
 *
 * @author Peter Wu
 */
public class ParameterUtil {

	/**
	 * @param params 容器
	 * @param key    参数名
	 * @return 是否有名为key且内容不为空的参数
	 */
	public static boolean hasParameter(Map<String, String[]> params, String key) {
		return params.containsKey(key) && StringUtils.hasText(params.get(key)[0]);
	}

	/**
	 * @param params 容器
	 * @param key    参数名
	 * @return 是否有名为key的参数
	 */
	public static boolean hasParameterKey(Map<String, String[]> params, String key) {
		return params.containsKey(key);
	}

	/**
	 * @param params 容器
	 * @param key    参数名
	 * @return 是否有名为key且内容不为空的参数
	 */
	public static boolean hasParameter(MultiValueMap<String, String> params, String key) {
		return params.containsKey(key) && StringUtils.hasText(params.getFirst(key));
	}

	/**
	 * @param params 容器
	 * @param key    参数名
	 * @return 是否有名为key的参数
	 */
	public static boolean hasParameterKey(MultiValueMap<String, String> params, String key) {
		return params.containsKey(key);
	}

}
