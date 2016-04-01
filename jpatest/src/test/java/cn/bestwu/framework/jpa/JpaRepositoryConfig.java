package cn.bestwu.framework.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@SpringBootApplication
public class JpaRepositoryConfig extends SpringBootServletInitializer {

	/*
	 * 支持在容器启动
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(JpaRepositoryConfig.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(JpaRepositoryConfig.class, args);
	}

	@Configuration
	protected static class Mvc extends WebMvcConfigurerAdapter {
		@Autowired
		private MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter;

		@Override public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
			converters.add(mappingJackson2XmlHttpMessageConverter);
		}
	}
}