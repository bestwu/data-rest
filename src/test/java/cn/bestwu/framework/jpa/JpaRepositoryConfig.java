package cn.bestwu.framework.jpa;

import cn.bestwu.framework.EnableDataRest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

@SpringBootApplication
@EnableDataRest
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
}