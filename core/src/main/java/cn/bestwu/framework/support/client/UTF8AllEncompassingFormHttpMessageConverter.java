package cn.bestwu.framework.support.client;

import org.springframework.http.converter.*;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;

public class UTF8AllEncompassingFormHttpMessageConverter extends FormHttpMessageConverter {
	private static final boolean jaxb2Present =
			ClassUtils.isPresent("javax.xml.bind.Binder", AllEncompassingFormHttpMessageConverter.class.getClassLoader());

	private static final boolean jackson2Present =
			ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", AllEncompassingFormHttpMessageConverter.class.getClassLoader()) &&
					ClassUtils.isPresent("com.fasterxml.jackson.core.JsonGenerator", AllEncompassingFormHttpMessageConverter.class.getClassLoader());

	private static final boolean jackson2XmlPresent =
			ClassUtils.isPresent("com.fasterxml.jackson.dataformat.xml.XmlMapper", AllEncompassingFormHttpMessageConverter.class.getClassLoader());

	private static final boolean gsonPresent =
			ClassUtils.isPresent("com.google.gson.Gson", AllEncompassingFormHttpMessageConverter.class.getClassLoader());

	public UTF8AllEncompassingFormHttpMessageConverter() {
		List<HttpMessageConverter<?>> partConverters = new ArrayList<>();

		partConverters.add(new ByteArrayHttpMessageConverter());
		StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter(DEFAULT_CHARSET);
		stringHttpMessageConverter.setWriteAcceptCharset(false);
		partConverters.add(stringHttpMessageConverter);
		partConverters.add(new ResourceHttpMessageConverter());

		setPartConverters(partConverters);

		addPartConverter(new SourceHttpMessageConverter<>());

		if (jaxb2Present && !jackson2Present) {
			addPartConverter(new Jaxb2RootElementHttpMessageConverter());
		}

		if (jackson2Present) {
			addPartConverter(new MappingJackson2HttpMessageConverter());
		} else if (gsonPresent) {
			addPartConverter(new GsonHttpMessageConverter());
		}

		if (jackson2XmlPresent) {
			addPartConverter(new MappingJackson2XmlHttpMessageConverter());
		}
	}
}