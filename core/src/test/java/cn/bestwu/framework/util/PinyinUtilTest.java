package cn.bestwu.framework.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Peter Wu
 */
public class PinyinUtilTest {

	@Test
	public void test() throws Exception {
		Assert.assertEquals("chongqing", PinyinUtil.getPinYin("重庆"));
		Assert.assertEquals("cq", PinyinUtil.getPinYinHead("重庆"));
	}
}
