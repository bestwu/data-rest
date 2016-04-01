package cn.bestwu.framework.rest.support;

/**
 * 接口版本信息
 *
 * @author Peter Wu
 */
public interface Version {
	String DEFAULT_VERSION = "1.0";
	/**
	 * 请求头中Accept中包含的版本参数名
	 */
	String VERSION_PARAM_NAME = "version";

	/**
	 * 比较版本信息
	 *
	 * @param version1 只包含数字
	 * @param version2 只包含数字
	 * @return int
	 */
	static int compareVersion(String version1, String version2) {
		if (version1.equals(version2)) {
			return 0;
		}
		String separator = "[\\.-]";
		String[] version1s = version1.split(separator);
		String[] version2s = version2.split(separator);

		boolean vl = version1s.length < version2s.length;
		int length = vl ? version1s.length : version2s.length;

		for (int i = 0; i < length; i++) {
			try {
				int v2 = Integer.parseInt(version2s[i]);
				int v1 = Integer.parseInt(version1s[i]);
				if (v2 > v1) {
					return -1;
				} else if (v2 < v1) {
					return 1;
				}
			} catch (NumberFormatException e) {
				int result = version1s[i].compareTo(version2s[i]);
				if (result != 0) {
					return result;
				}
			}
			// 相等 比较下一组值
		}

		if (vl)
			return -1;
		else if (version1s.length > version2s.length)
			return 1;
		return 0;
	}

	/**
	 * @param version1 version1
	 * @param version2 version2
	 * @return version1 是否包含 version2
	 */
	static boolean included(String version1, String version2) {
		return version1.contains(version2) || version1.matches(version2);
	}
}
