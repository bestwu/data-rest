package cn.bestwu.framework.rest.support;

import org.springframework.util.AntPathMatcher;

import java.util.HashMap;

/**
 * 代理路径映射
 *
 * @author Peter Wu
 */
public class ProxyPathMapper extends HashMap<String, String> {
	private static final long serialVersionUID = -8964364431586106821L;
	private AntPathMatcher pathMatcher = new AntPathMatcher();
	private HashMap<String, String> proxyPathCache = new HashMap<>();

	public String getProxyPath(String path) {
		String proxyPath = proxyPathCache.get(path);
		if (proxyPath == null) {
			for (String k : keySet()) {
				if (pathMatcher.match(k, path)) {
					proxyPath = path.replace(k.replace("*", ""), get(k).replace("*", ""));
					proxyPathCache.put(path, proxyPath);
					break;
				}
			}
		}

		return proxyPath == null ? path : proxyPath;
	}
}
