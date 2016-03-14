package cn.bestwu.framework.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
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
}
