package cn.bestwu.framework.mongodb;

import cn.bestwu.framework.EnableDataRest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

@SpringBootApplication
@EnableDataRest
public class MongoDbRepositoryConfig extends SpringBootServletInitializer {

	/*
	 * 支持在容器启动
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(MongoDbRepositoryConfig.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(MongoDbRepositoryConfig.class, args);
	}
}
