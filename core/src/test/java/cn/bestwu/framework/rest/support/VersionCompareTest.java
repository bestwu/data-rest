package cn.bestwu.framework.rest.support;

import cn.bestwu.framework.rest.mapping.VersionRepositoryRestRequestMappingHandlerMapping;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Peter Wu
 */
public class VersionCompareTest {

	@Test
	public void testVersion() throws Exception {
		HashMap<String, String> parameters = new HashMap<>();
		parameters.put("version", "1.2.2");
		MediaType mediaType = new MediaType("application", "json", parameters);
		HashMap<String, String> parameters2 = new HashMap<>();
		parameters2.put("version", "1.10.3");
		MediaType mediaType2 = new MediaType("application", "json", parameters2);

		List<MediaType> mediaTypes = new ArrayList<>();

		mediaTypes.add(mediaType);
		mediaTypes.add(mediaType2);

		mediaTypes.sort(VersionRepositoryRestRequestMappingHandlerMapping.VERSION_COMPARATOR);

		Assert.assertEquals(mediaType, mediaTypes.get(0));
	}

}
