package cn.bestwu.framework.mongodb;

import cn.bestwu.framework.mongodb.model.TestUser;
import cn.bestwu.framework.support.client.CustomRestTemplate;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@SpringBootTest(classes = MongoDbRepositoryConfig.class,value = "server.context-path=/tv", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ControllerTests {
	@LocalServerPort
	private int port;

	protected String getBaseUrl() {
		String host = "http://127.0.0.1";
		return host + ":" + port + "/tv";
	}

	@Test
	public void testRest() throws Exception {
		CustomRestTemplate restTemplate = new CustomRestTemplate();

		//save
		TestUser testUser = new TestUser();
		testUser.setFirstName("peter1");
		testUser.setLastName("wu1");
		TestUser testUser1Result = restTemplate.postForObject(getBaseUrl() + "/testUsers", testUser, TestUser.class);
		Assert.assertEquals("peter1", testUser1Result.getFirstName());
		Assert.assertEquals("wu1", testUser1Result.getLastName());

		MultiValueMap<String, Object> testUser2 = new LinkedMultiValueMap<>();
		testUser2.put("firstName", Collections.singletonList("peter2"));
		testUser2.put("lastName", Collections.singletonList("wu2"));
		Map testUser2Result = restTemplate.postForObject(getBaseUrl() + "/testUsers", testUser2, HashMap.class);
		Assert.assertEquals("peter2", testUser2Result.get("firstName"));
		Assert.assertEquals("wu2", testUser2Result.get("lastName"));

		//update
		//update
		//		TestUser testUser1ForUpdate = new TestUser();
		testUser1Result.setFirstName("Peter_modify");
		Map testUser1UpdateResult = restTemplate.putForObject(getBaseUrl() + "/testUsers/" + testUser1Result.getId(), testUser1Result, HashMap.class);
		Assert.assertEquals("Peter_modify", testUser1UpdateResult.get("firstName"));

		MultiValueMap<String, Object> testUser2ForUpdate = new LinkedMultiValueMap<>();
		testUser2ForUpdate.put("lastName", Collections.singletonList("wu2_modify"));
		Map testUser2UpdateResult = restTemplate.putForObject(getBaseUrl() + "/testUsers/" + testUser2Result.get("id"), testUser2ForUpdate, HashMap.class);
		Assert.assertEquals("wu2_modify", testUser2UpdateResult.get("lastName"));

		//get
		Map getResult = restTemplate.getForObject(getBaseUrl() + "/testUsers/" + testUser2Result.get("id"), HashMap.class);
		Assert.assertEquals("wu2_modify", getResult.get("lastName"));

		//index
		Map page = restTemplate.getForObject(getBaseUrl() + "/testUsers", HashMap.class);
		Assert.assertEquals(2, page.get("totalElements"));

		//search
		ResponseEntity<HashMap> entity = restTemplate.getForEntity(getBaseUrl() + "/testUsers/search/fulltext?keyword=wu2_modify", HashMap.class);
		Assert.assertEquals(HttpStatus.OK, entity.getStatusCode());
		Assert.assertEquals(1, entity.getBody().get("totalElements"));

		//delete
		entity = restTemplate.deleteForEntity(getBaseUrl() + "/testUsers/" + testUser1Result.getId());
		Assert.assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
		Map page1 = restTemplate.getForObject(getBaseUrl() + "/testUsers", HashMap.class);
		Assert.assertEquals(1, page1.get("totalElements"));
		entity = restTemplate.deleteForEntity(getBaseUrl() + "/testUsers");
		Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, entity.getStatusCode());
		entity = restTemplate.deleteForEntity(getBaseUrl() + "/testUsers?" + "id=" + testUser1Result.getId() + "&id=" + testUser2Result.get("id"));
		Assert.assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
		Map page2 = restTemplate.getForObject(getBaseUrl() + "/testUsers", HashMap.class);
		Assert.assertEquals(0, page2.get("totalElements"));
	}

}
