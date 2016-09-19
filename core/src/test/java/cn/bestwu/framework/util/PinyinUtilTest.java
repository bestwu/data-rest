package cn.bestwu.framework.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Peter Wu
 */
public class PinyinUtilTest {

	@Test
	public void duoyinzi() throws Exception {
		Assert.assertEquals("ChongQing", PinyinUtil.getPinYin("重庆"));
		Assert.assertEquals("cq", PinyinUtil.getPinYinHead("重庆"));
	}

	@Test
	public void name() throws Exception {
		System.err.println(PinyinUtil.getPinYin("崖"));
	}
}
