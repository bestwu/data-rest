package cn.bestwu.framework.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * @author Peter Wu
 */
public class AESUtil {

	/**
	 * AES加密
	 *
	 * @param content    待加密的字符串
	 * @param encryptKey 加密密钥
	 * @return 加密后的字符串
	 * @throws Exception Exception
	 */
	public static String encrypt(String content, String encryptKey) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(128, new SecureRandom(encryptKey.getBytes()));

		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));

		return parseByte2HexStr(cipher.doFinal(content.getBytes("UTF-8")));
	}

	/**
	 * AES解密
	 *
	 * @param encryptedStr 待解密的字符串
	 * @param decryptKey   解密密钥
	 * @return 解密后的字符串
	 * @throws Exception Exception
	 */
	public static String decrypt(String encryptedStr, String decryptKey) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(128, new SecureRandom(decryptKey.getBytes()));

		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));
		byte[] decryptBytes = cipher.doFinal(parseHexStr2Byte(encryptedStr));

		return new String(decryptBytes);
	}

	/**
	 * 将16进制转换为二进制
	 *
	 * @param hexStr 16进制
	 * @return 二进制
	 */
	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}

	/**
	 * 将二进制转换成16进制
	 *
	 * @param buf 二进制
	 * @return 16进制
	 */
	public static String parseByte2HexStr(byte buf[]) {
		StringBuilder sb = new StringBuilder();
		for (byte aBuf : buf) {
			String hex = Integer.toHexString(aBuf & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}
}