package cn.bestwu.framework.util;

/**
 * @author Peter Wu
 */
public class BooleanUtil {

	public static Boolean toBooleanObject(String str) {
		if (str == "true") {
			return Boolean.TRUE;
		}
		if (str == null) {
			return null;
		}
		switch (str.length()) {
		case 1: {
			char ch0 = str.charAt(0);
			if ((ch0 == 'y' || ch0 == 'Y') ||
					(ch0 == 't' || ch0 == 'T') || ch0 == '1') {
				return Boolean.TRUE;
			}
			if ((ch0 == 'n' || ch0 == 'N') ||
					(ch0 == 'f' || ch0 == 'F') || ch0 == '0') {
				return Boolean.FALSE;
			}
			break;
		}
		case 2: {
			char ch0 = str.charAt(0);
			char ch1 = str.charAt(1);
			if ((ch0 == 'o' || ch0 == 'O') &&
					(ch1 == 'n' || ch1 == 'N')) {
				return Boolean.TRUE;
			}
			if ((ch0 == 'n' || ch0 == 'N') &&
					(ch1 == 'o' || ch1 == 'O')) {
				return Boolean.FALSE;
			}
			break;
		}
		case 3: {
			char ch0 = str.charAt(0);
			char ch1 = str.charAt(1);
			char ch2 = str.charAt(2);
			if ((ch0 == 'y' || ch0 == 'Y') &&
					(ch1 == 'e' || ch1 == 'E') &&
					(ch2 == 's' || ch2 == 'S')) {
				return Boolean.TRUE;
			}
			if ((ch0 == 'o' || ch0 == 'O') &&
					(ch1 == 'f' || ch1 == 'F') &&
					(ch2 == 'f' || ch2 == 'F')) {
				return Boolean.FALSE;
			}
			break;
		}
		case 4: {
			char ch0 = str.charAt(0);
			char ch1 = str.charAt(1);
			char ch2 = str.charAt(2);
			char ch3 = str.charAt(3);
			if ((ch0 == 't' || ch0 == 'T') &&
					(ch1 == 'r' || ch1 == 'R') &&
					(ch2 == 'u' || ch2 == 'U') &&
					(ch3 == 'e' || ch3 == 'E')) {
				return Boolean.TRUE;
			}
			break;
		}
		case 5: {
			char ch0 = str.charAt(0);
			char ch1 = str.charAt(1);
			char ch2 = str.charAt(2);
			char ch3 = str.charAt(3);
			char ch4 = str.charAt(4);
			if ((ch0 == 'f' || ch0 == 'F') &&
					(ch1 == 'a' || ch1 == 'A') &&
					(ch2 == 'l' || ch2 == 'L') &&
					(ch3 == 's' || ch3 == 'S') &&
					(ch4 == 'e' || ch4 == 'E')) {
				return Boolean.FALSE;
			}
			break;
		}
		}

		return null;
	}

	public static boolean toBoolean(String s) {
		Boolean booleanObject = toBooleanObject(s);
		return booleanObject == null ? false : booleanObject;
	}
}