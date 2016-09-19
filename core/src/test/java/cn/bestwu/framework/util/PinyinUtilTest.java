package cn.bestwu.framework.util;

import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Peter Wu
 */
public class PinyinUtilTest {

	@Test
	public void duoyinzi() throws Exception {
		String pinyin = PinyinHelper.convertToPinyinString("重庆", "", PinyinFormat.WITHOUT_TONE);
		System.err.println(pinyin);
		Assert.assertEquals("chongqing", pinyin);
		Assert.assertEquals("cq", PinyinHelper.getShortPinyin("重庆"));
	}

	@Test
	public void name() throws Exception {
		System.err.println(PinyinHelper.convertToPinyinString("崖", "", PinyinFormat.WITHOUT_TONE));
	}
}
