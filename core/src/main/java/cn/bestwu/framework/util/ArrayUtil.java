package cn.bestwu.framework.util;

import java.lang.reflect.Array;

/**
 * Array 工具类
 *
 * @author Peter Wu
 */
public class ArrayUtil {
	private static final int INDEX_NOT_FOUND = -1;

	/**
	 * @param array        数组
	 * @param objectToFind 要查询的内容
	 * @return 是否包含
	 */
	public static boolean contains(final Object[] array, final Object objectToFind) {
		return indexOf(array, objectToFind) != INDEX_NOT_FOUND;
	}

	/**
	 * @param array        数组
	 * @param objectToFind 要查询的内容
	 * @return 内容所在索引
	 */
	public static int indexOf(final Object[] array, final Object objectToFind) {
		return indexOf(array, objectToFind, 0);
	}

	/**
	 * @param array        数组
	 * @param objectToFind 要查询的内容
	 * @param startIndex   开始搜索的索引
	 * @return 内容所在索引
	 */
	private static int indexOf(final Object[] array, final Object objectToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		if (objectToFind == null) {
			for (int i = startIndex; i < array.length; i++) {
				if (array[i] == null) {
					return i;
				}
			}
		} else if (array.getClass().getComponentType().isInstance(objectToFind)) {
			for (int i = startIndex; i < array.length; i++) {
				if (objectToFind.equals(array[i])) {
					return i;
				}
			}
		}
		return INDEX_NOT_FOUND;
	}

	/**
	 * 转换为数组
	 *
	 * @param items items
	 * @param <T>   T
	 * @return 数组
	 */
	@SafeVarargs public static <T> T[] toArray(final T... items) {
		return items;
	}

	/**
	 * @param array 数组
	 * @return 是否不为空
	 */
	public static boolean isNotEmpty(Object[] array) {
		return !isEmpty(array);
	}

	/**
	 * @param array 数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(Object[] array) {
		return (array == null || array.length == 0);
	}

	/**
	 * @param separator 分隔符
	 * @param array     数组
	 * @return toString
	 */
	public static String toString(String separator, Object... array) {
		int length = Array.getLength(array);
		int iMax = length - 1;
		if (iMax == -1)
			return "";

		StringBuilder b = new StringBuilder();
		for (int i = 0; ; i++) {
			b.append(Array.get(array, i));
			if (i == iMax)
				return b.toString();
			b.append(separator);
		}
	}

	/**
	 * @param array 数组
	 * @return 默认 “,” 分隔的toString
	 */
	public static String toString(Object... array) {
		return toString(",", array);
	}
}
