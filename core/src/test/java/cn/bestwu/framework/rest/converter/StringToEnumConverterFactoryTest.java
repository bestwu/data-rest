package cn.bestwu.framework.rest.converter;

import cn.bestwu.framework.rest.support.ResourceType;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Peter Wu
 */
public class StringToEnumConverterFactoryTest {

	@Test
	public void testConvert() throws Exception {
		StringToEnumConverterFactory converterFactory = new StringToEnumConverterFactory();
		Assert.assertEquals(ResourceType.COLLECTION, converterFactory.getConverter(ResourceType.class).convert("0"));
	}
}
