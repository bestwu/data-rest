package cn.bestwu.framework.rest.converter;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * ResponseEntity MixIn
 *
 * @author Peter Wu
 */
public interface ResponseEntityMixIn<T> {

	@JsonView(Object.class) HttpStatus getStatusCode();

	@JsonView(Object.class) T getBody();

	@JsonView(Object.class) HttpHeaders getHeaders();
}
