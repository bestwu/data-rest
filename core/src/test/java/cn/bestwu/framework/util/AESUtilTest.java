package cn.bestwu.framework.util;

import org.junit.Test;

/**
 * @author Peter Wu
 */
public class AESUtilTest {

	@Test
	public void test() throws Exception {

		String content = "我爱你";
		System.out.println("加密前：" + content);

		String key = "12";
		System.out.println("加密密钥和解密密钥：" + key);

		String encrypt = AESUtil.encrypt(content, key);
		System.out.println("加密后：" + encrypt);
		System.out.println("加密后：" + encrypt.length());

		String decrypt = AESUtil.decrypt(encrypt, key);
		System.out.println("解密后：" + decrypt);
	}

}
