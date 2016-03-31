package cn.bestwu.framework.rest.mapping;

import cn.bestwu.framework.rest.support.ResourceUtil;
import cn.bestwu.framework.rest.support.Version;
import cn.bestwu.framework.rest.support.VersionedSerializationView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 接口结果View 映射类
 *
 * @author Peter Wu
 */
public class SerializationViewMappings {

	private final Class<?> serializationViewsClass;

	public SerializationViewMappings(Class<?> serializationViewsClass) {
		this.serializationViewsClass = serializationViewsClass;
		populateCache();
	}

	/**
	 * key: api 自定义 Signature: requestMethd+RequestMapping mapping 如：GET_IMAGES
	 */
	private final Map<String, List<VersionedSerializationView>> cache = new HashMap<>();

	private void populateCache() {
		if (serializationViewsClass == null) {
			return;
		}

		Class<?>[] classes = serializationViewsClass.getDeclaredClasses();
		Arrays.stream(classes).forEach(clazz -> {
			String className = clazz.getSimpleName();//例子：GET_IMAGES_V_1_0
			String signature = className.replaceAll("^(.*)_V_.*$", "$1");
			String version = className.replaceAll("^.*_V_(.*)$", "$1");
			if (version.equals(signature)) {
				version = Version.DEFAULT_VERSION;
			} else {
				version = version.replace("_", ".");
			}
			List<VersionedSerializationView> jsonViews = cache.get(signature);
			boolean firstPut = jsonViews == null;
			if (firstPut) {
				jsonViews = new ArrayList<>();
			}
			jsonViews.add(new VersionedSerializationView(version.toLowerCase(), clazz));
			if (firstPut) {
				cache.put(signature, jsonViews);
			}
		});

		cache.values().forEach(Collections::sort);
	}

	public Class<?> getSerializationView(HttpServletRequest request) {
		if (serializationViewsClass == null) {
			return null;
		}
		String requestSignature = ResourceUtil.getRequestSignature(request);
		if (requestSignature == null) {
			return null;
		}
		List<VersionedSerializationView> jsonViews = cache.get(requestSignature);
		if (jsonViews == null || jsonViews.isEmpty()) {
			return null;
		}
		String requestVersion = ResourceUtil.getRequestVersion(request);
		for (VersionedSerializationView jsonView : jsonViews) {
			if (jsonView.getVersion().equalsIgnoreCase(requestVersion)) {
				return jsonView.getSerializationView();
			}
		}
		for (VersionedSerializationView jsonView : jsonViews) {
			String version = jsonView.getVersion();
			if (version.contains(requestVersion) || version.matches(requestVersion)) {
				return jsonView.getSerializationView();
			}
		}

		return jsonViews.get(0).getSerializationView();
	}

}
