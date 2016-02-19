package cn.bestwu.framework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

/**
 * 应用启动入口
 */
@SpringBootApplication
@EnableDataRest
public class TestApplication extends SpringBootServletInitializer {

	/*
	 * 支持在容器启动
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(TestApplication.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(TestApplication.class, args);
	}

}
