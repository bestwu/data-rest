package cn.bestwu.framework.rest.converter;

import cn.bestwu.lang.util.Sha1DigestUtil;
import org.junit.Test;
import org.springframework.core.serializer.DefaultSerializer;
import org.springframework.core.serializer.Serializer;
import org.springframework.core.serializer.support.SerializationFailedException;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Wu
 */
public class TestSerializer {
	Serializer<Object> serializer = new DefaultSerializer();

	@Test
	public void test() throws Exception {
		List<String> bean = new ArrayList<>();
		for (int i = 0; i < 10240; i++)
			bean.add("ddddddddddddddddddddddddddddd");

		long start = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024);
			try {
				serializer.serialize(bean, byteStream);
				Sha1DigestUtil.shaHex(new String(byteStream.toByteArray()));
			} catch (Throwable ex) {
				throw new SerializationFailedException("Failed to serialize object using " + serializer.getClass().getSimpleName(), ex);
			}
		}
		long end = System.currentTimeMillis();
		System.err.println(end - start);
	}
}
