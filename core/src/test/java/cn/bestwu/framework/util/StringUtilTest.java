package cn.bestwu.framework.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试{@code StringUtil.valueOf}方法
 *
 * @author Peter Wu
 */
public class StringUtilTest {

	@Test
	public void testValueOf() throws Exception {
		Map<String, String> testUser = new HashMap<>();
		testUser.put("firstName", "peter");
		testUser.put("lastName", "wu");

		Assert.assertEquals("{\"firstName\":\"peter\",\"lastName\":\"wu\"}", StringUtil.valueOf(testUser));
	}

	@Test
	public void countSubString() throws Exception {
		Assert.assertEquals(1, StringUtil.countSubString("a.b", "."));
		Assert.assertEquals(0, StringUtil.countSubString("ab", "."));
	}

	@Test
	public void compress() throws Exception {
		String str = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE0ODE0MjAzNjAsInVzZXJfbmFtZSI6ImlkIzEiLCJqdGkiOiJlZTFkOWRkYi1lODZkLTRhMGYtOWNiZC04Y2IwOGI1ZTQ4NzAiLCJjbGllbnRfaWQiOiJxTTdWOVRaWGRVN2lYVGd2Iiwic2NvcGUiOlsidHJ1c3QiXX0.QS_lOkBdKy_lI9UjILwdl4afBAfnXp0_UqyDIi6ZngVnNYeDSXwQW7wzHzwo6CCDapRCiZLT7NgT4BU0k8jzaoQLURR_Qufc0xsywBmXX0LfpExq0Pv2UqnvaRmv_jHLTZLiAO9QwN3T5JSmsot41Ha5eRwAT8vvCLj2TJa7zEVBkJ_QFttuCbwS0lJOsndTL8-T0GMqE9pzMmvuONAJkzwDp8N4YrRJ8qY8RA5QWxEwE_CVNnikCkxa95TubwPXuT19BThmsVeJtWjX3bc-Uas5dWAuxTvORsiWGZquqUc_Owl8wd6xX7FaLEeZpydgXbBd5g-uZwM6TM31MFKlcg";
		System.err.println(str);
		String compress = StringUtil.compress(str);
		System.err.println(compress);
		System.err.println(str.length());
		System.err.println(compress.length());
		String decompress = StringUtil.decompress(compress);
		System.err.println(decompress);
	}
}
