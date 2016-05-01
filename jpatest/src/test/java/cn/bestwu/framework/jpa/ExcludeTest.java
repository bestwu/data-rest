package cn.bestwu.framework.jpa;

import cn.bestwu.framework.support.client.CustomRestTemplate;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.web.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

/**
 * @author Peter Wu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = JpaRepositoryConfig.class, value = "server.context-path=/tv", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExcludeTest {
	@LocalServerPort
	private int port;

	CustomRestTemplate restTemplate = new CustomRestTemplate();

	protected String getBaseUrl() {
		String host = "http://127.0.0.1";
		return host + ":" + port + "/tv";
	}

	/*
	 * @RestResource(excludes = CommonAPI.ALL) 测试
	 */
	@Test
	public void testExludeAll() throws Exception {
		ResponseEntity<Map> result = restTemplate.postForEntity(getBaseUrl() + "/testUser2s", null, Map.class);
		Assert.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
		result = restTemplate.putForEntity(getBaseUrl() + "/testUser2s/12", null, Map.class);
		Assert.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
		result = restTemplate.getForEntity(getBaseUrl() + "/testUser2s/12", Map.class);
		Assert.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
		result = restTemplate.getForEntity(getBaseUrl() + "/testUser2s", Map.class);
		Assert.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
		result = restTemplate.deleteForEntity(getBaseUrl() + "/testUser2s/11");
		Assert.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
		result = restTemplate.deleteForEntity(getBaseUrl() + "/testUser2s");
		Assert.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
	}

	/*
		 * @RepositoryRestResource(RepositoryRestResource.POST)
		 * @Override <S extends TestUser3> S save(S entity);
		 */
	@Test
	public void testExludeUpdate() throws Exception {
		//save
		ResponseEntity<Map> result = restTemplate.postForEntity(getBaseUrl() + "/testUser3s", null, Map.class);
		Assert.assertEquals(HttpStatus.CREATED, result.getStatusCode());
		result = restTemplate.putForEntity(getBaseUrl() + "/testUser3s/" + result.getBody().get("id"), null, Map.class);
		Assert.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, result.getStatusCode());
		result = restTemplate.getForEntity(getBaseUrl() + "/testUser3s", Map.class);
		Assert.assertEquals(HttpStatus.OK, result.getStatusCode());

	}

	/*
	 * @RepositoryRestResource(RepositoryRestResource.PUT)
	 * @Override <S extends TestUser4> S save(S entity);
	 */
	@Test
	public void testExlude() throws Exception {
		ResponseEntity<Map> result = restTemplate.postForEntity(getBaseUrl() + "/testUser4s", null, Map.class);
		Assert.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
		result = restTemplate.getForEntity(getBaseUrl() + "/testUser4s", Map.class);
		Assert.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
	}
}
