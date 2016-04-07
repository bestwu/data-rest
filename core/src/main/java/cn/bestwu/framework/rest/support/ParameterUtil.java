package cn.bestwu.framework.rest.support;

import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author Peter Wu
 */
public class ParameterUtil {

	public static boolean hasParameter(Map<String, String[]> params, String key) {
		return params.containsKey(key) && StringUtils.hasText(params.get(key)[0]);
	}

	public static boolean hasParameterKey(Map<String, String[]> params, String key) {
		return params.containsKey(key);
	}

	public static boolean hasParameter(MultiValueMap<String, String> params, String key) {
		return params.containsKey(key) && StringUtils.hasText(params.getFirst(key));
	}

	public static boolean hasParameterKey(MultiValueMap<String, String> params, String key) {
		return params.containsKey(key);
	}

}
