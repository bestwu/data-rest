package cn.bestwu.framework.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Sha1加密工具类
 *
 * @author Peter Wu
 */
public class Sha1DigestUtil {

	/**
	 * Returns an SHA digest.
	 *
	 * @return An SHA digest instance.
	 * @throws RuntimeException when a {@link NoSuchAlgorithmException} is
	 *                          caught.
	 */
	private static MessageDigest getSha1Digest() {
		try {
			return MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * Calculates the SHA digest and returns the value as a {@code byte[]}.
	 *
	 * @param data Data to digest
	 * @return SHA digest
	 */
	public static byte[] sha(byte[] data) {
		return getSha1Digest().digest(data);
	}

	/**
	 * Calculates the SHA digest and returns the value as a {@code byte[]}.
	 *
	 * @param data Data to digest
	 * @return SHA digest
	 */
	public static byte[] sha(String data) {
		return sha(data.getBytes());
	}

	/**
	 * Calculates the SHA digest and returns the value as a hex string.
	 *
	 * @param data Data to digest
	 * @return SHA digest as a hex string
	 */
	public static String shaHex(byte[] data) {
		return new String(Hex.encode(sha(data)));
	}

	/**
	 * Calculates the SHA digest and returns the value as a hex string.
	 *
	 * @param data Data to digest
	 * @return SHA digest as a hex string
	 */
	public static String shaHex(String data) {
		return new String(Hex.encode(sha(data)));
	}

}