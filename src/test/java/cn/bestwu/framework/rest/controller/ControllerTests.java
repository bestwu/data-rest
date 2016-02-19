package cn.bestwu.framework.rest.controller;

import cn.bestwu.framework.TestApplication;
import cn.bestwu.framework.test.util.VersionSupportRestTemplate;
import cn.bestwu.framework.data.model.TestUser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * rest测试类
 *
 * @author Peter Wu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestApplication.class)
@WebIntegrationTest(value = "server.context-path=/tv", randomPort = true)
public class ControllerTests {
	@Value("${local.server.port}")
	private int port;

	protected String getBaseUrl() {
		String host = "http://127.0.0.1";
		return host + ":" + port + "/tv";
	}

	@Test
	public void testRest() throws Exception {
		VersionSupportRestTemplate restTemplate = new VersionSupportRestTemplate();

		//save
		TestUser testUser = new TestUser();
		testUser.setFirstName("peter1");
		testUser.setLastName("wu1");
		Map testUser1Result = restTemplate.postForObject(getBaseUrl() + "/testUsers", testUser, HashMap.class);
		Assert.assertEquals("peter1", testUser1Result.get("firstName"));
		Assert.assertEquals("wu1", testUser1Result.get("lastName"));

		MultiValueMap<String, Object> testUser2 = new LinkedMultiValueMap<>();
		testUser2.put("firstName", Collections.singletonList("peter2"));
		testUser2.put("lastName", Collections.singletonList("wu2"));
		Map testUser2Result = restTemplate.postForObject(getBaseUrl() + "/testUsers", testUser2, HashMap.class);
		Assert.assertEquals("peter2", testUser2Result.get("firstName"));
		Assert.assertEquals("wu2", testUser2Result.get("lastName"));

		//update
		TestUser testUser1ForUpdate = new TestUser();
		testUser1ForUpdate.setFirstName("Peter_modify");
		Map testUser1UpdateResult = restTemplate.putForObject(getBaseUrl() + "/testUsers/" + testUser1Result.get("id"), testUser1ForUpdate, HashMap.class);
		Assert.assertEquals("Peter_modify", testUser1UpdateResult.get("firstName"));

		MultiValueMap<String, Object> testUser2ForUpdate = new LinkedMultiValueMap<>();
		testUser2ForUpdate.put("lastName", Collections.singletonList("Wu2_modify"));
		Map testUser2UpdateResult = restTemplate.putForObject(getBaseUrl() + "/testUsers/" + testUser2Result.get("id"), testUser2ForUpdate, HashMap.class);
		Assert.assertEquals("Wu2_modify", testUser2UpdateResult.get("lastName"));

		//get
		Map getResult = restTemplate.getForObject(getBaseUrl() + "/testUsers/" + testUser2Result.get("id"), HashMap.class);
		Assert.assertEquals("Wu2_modify", getResult.get("lastName"));

		//index
		Map page = restTemplate.getForObject(getBaseUrl() + "/testUsers", HashMap.class);
		Assert.assertEquals(2, page.get("totalElements"));

		//delete
		restTemplate.delete(getBaseUrl() + "/testUsers/" + testUser1Result.get("id"));
		Map page1 = restTemplate.getForObject(getBaseUrl() + "/testUsers", HashMap.class);
		Assert.assertEquals(1, page1.get("totalElements"));
		restTemplate.delete(getBaseUrl() + "/testUsers");
		restTemplate.delete(getBaseUrl() + "/testUsers?" + "id=" + testUser1Result.get("id") + "&id=" + testUser2Result.get("id"));
		Map page2 = restTemplate.getForObject(getBaseUrl() + "/testUsers", HashMap.class);
		Assert.assertEquals(0, page2.get("totalElements"));
	}

}
