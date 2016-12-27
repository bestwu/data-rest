package cn.bestwu.framework.rest.mapping;

import cn.bestwu.framework.rest.support.Version;
import cn.bestwu.framework.rest.support.VersionedSerializationView;
import cn.bestwu.framework.util.ResourceUtil;

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
	 * key: api 自定义 Signature: requestMethd+RequestMapping mapping 如：get_users
	 */
	private final Map<String, List<VersionedSerializationView>> cache = new HashMap<>();

	/**
	 * 填充序列化视图缓存
	 */
	private void populateCache() {
		if (serializationViewsClass == null) {
			return;
		}

		Class<?>[] classes = serializationViewsClass.getClasses();
		for (Class<?> clazz : classes) {
			String className = clazz.getSimpleName();//例子：get_users_v_1_0
			String signature = className.replaceAll("^(.*)_v_.*$", "$1");
			String version = className.replaceAll("^.*_v_(.*)$", "$1");
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
			jsonViews.add(new VersionedSerializationView(version, clazz));
			if (firstPut) {
				cache.put(signature, jsonViews);
			}
		}

		for (List<VersionedSerializationView> serializationViews : cache.values()) {
			Collections.sort(serializationViews);
		}
	}

	/**
	 * @return 对应序列化视图
	 */
	public Class<?> getSerializationView() {
		if (serializationViewsClass == null) {
			return null;
		}
		String requestSignature = ResourceUtil.API_SIGNATURE.get();
		if (requestSignature == null) {
			return null;
		}
		List<VersionedSerializationView> jsonViews = cache.get(requestSignature);
		if (jsonViews == null || jsonViews.isEmpty()) {
			//			if ("HEAD".equals(ResourceUtil.REQUEST_METHOD.get())) {
			//				jsonViews = cache.get(requestSignature.replaceFirst("head", "get"));
			//				if (jsonViews == null || jsonViews.isEmpty())
			//					return null;
			//			} else
			return null;
		}
		String requestVersion = ResourceUtil.REQUEST_VERSION.get();
		for (VersionedSerializationView jsonView : jsonViews) {
			if (Version.equals(jsonView.getVersion(), requestVersion)) {
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
