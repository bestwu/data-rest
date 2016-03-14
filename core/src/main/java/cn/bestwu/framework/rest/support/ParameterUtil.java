package cn.bestwu.framework.rest.support;

import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * @author Peter Wu
 */
public class ParameterUtil {

	public static boolean hasParameter(Map<String, String[]> params, String key) {
		return params.containsKey(key) && StringUtils.hasText(params.get(key)[0]);
	}

	public static boolean hasParameter(MultiValueMap<String, String> params, String key) {
		return params.containsKey(key);
	}

	public static void setPredicateDefaultActiveParameter(MultiValueMap<String, String> parameters) {
		String activeKey = "active";
		if (!ParameterUtil.hasParameter(parameters, activeKey)) {
			parameters.put(activeKey, Collections.singletonList("true"));
		}
	}
}
