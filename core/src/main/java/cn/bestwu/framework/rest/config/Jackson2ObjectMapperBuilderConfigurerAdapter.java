package cn.bestwu.framework.rest.config;

import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * @author Peter Wu
 */
public interface Jackson2ObjectMapperBuilderConfigurerAdapter {

	void configure(Jackson2ObjectMapperBuilder builder);

}
