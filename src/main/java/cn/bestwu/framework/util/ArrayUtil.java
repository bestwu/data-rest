package cn.bestwu.framework.util;

import java.lang.reflect.Array;

/**
 * Array 工具类
 */
public class ArrayUtil {
	private static final int INDEX_NOT_FOUND = -1;

	public static boolean contains(final Object[] array, final Object objectToFind) {
		return indexOf(array, objectToFind) != INDEX_NOT_FOUND;
	}

	public static int indexOf(final Object[] array, final Object objectToFind) {
		return indexOf(array, objectToFind, 0);
	}

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

	@SafeVarargs public static <T> T[] toArray(final T... items) {
		return items;
	}

	public static boolean isNotEmpty(Object[] array) {
		return !isEmpty(array);
	}

	public static boolean isEmpty(Object[] array) {
		return (array == null || array.length == 0);
	}

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

	public static String toString(Object... array) {
		return toString(",", array);
	}
}
