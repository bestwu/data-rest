package cn.bestwu.framework.util;

import cn.bestwu.framework.data.model.TestUser;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Peter Wu
 */
public class StringUtilsTest {

	@Test
	public void testValueOf() throws Exception {
		TestUser testUser = new TestUser();
		testUser.setFirstName("peter");
		testUser.setLastName("wu");

		Assert.assertEquals("{\"firstName\":\"peter\",\"lastName\":\"wu\"}", StringUtil.valueOf(testUser));
	}
}
