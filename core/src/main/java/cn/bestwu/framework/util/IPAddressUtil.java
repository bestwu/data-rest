package cn.bestwu.framework.util;

import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 * IP工具
 *
 * @author Peter Wu
 */
public class IPAddressUtil {
	/**
	 * 获取客户端IP
	 *
	 * @param request http请求
	 * @return ip
	 */
	public static String getClientIp(HttpServletRequest request) {

		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if (ip == null) {
			ip = "192.168.123.321";
		}
		return ip;
	}

	/**
	 * 是否为外网
	 *
	 * @param ipAddress ip
	 * @return 是否为外网
	 */
	public static boolean isExtranet(String ipAddress) {
		Assert.hasText(ipAddress);
		return !ipAddress.matches("(127\\.0\\.0\\.1)|" + "(localhost)|" +
				"(10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|" +
				"(172\\.((1[6-9])|(2\\d)|(3[01]))\\.\\d{1,3}\\.\\d{1,3})|" +
				"(192\\.168\\.\\d{1,3}\\.\\d{1,3})");
	}

	/**
	 * 获取客户端IP对应网卡的MAC地址
	 *
	 * @param ipAddress ip
	 * @return MAC地址
	 */
	public static String getMACAddress(String ipAddress) {
		String str, strMAC = "", macAddress;
		try {
			Process pp = Runtime.getRuntime().exec("nbtstat -a " + ipAddress);
			InputStreamReader ir = new InputStreamReader(pp.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);
			for (int i = 1; i < 100; i++) {
				str = input.readLine();
				if (str != null) {
					if (str.indexOf("MAC Address") > 1) {
						strMAC = str.substring(str.indexOf("MAC Address") + 14,
								str.length());
						break;
					}
				}
			}
		} catch (IOException ex) {
			return "Can't Get MAC Address!";
		}
		if (strMAC.length() < 17) {
			return "Error!";
		}

		macAddress = strMAC.substring(0, 2) + ":" + strMAC.substring(3, 5)
				+ ":" + strMAC.substring(6, 8) + ":" + strMAC.substring(9, 11)
				+ ":" + strMAC.substring(12, 14) + ":"
				+ strMAC.substring(15, 17);
		return macAddress;
	}

}