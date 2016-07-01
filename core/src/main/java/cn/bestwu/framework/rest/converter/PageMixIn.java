package cn.bestwu.framework.rest.converter;

import com.fasterxml.jackson.annotation.JsonView;

import java.util.List;

/**
 * Page MixIn
 *
 * @param <T> T
 * @author Peter Wu
 */
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "last", "first", "numberOfElements", "sort" })
public interface PageMixIn<T> {

	@JsonView(Object.class) long getTotalElements();

	@JsonView(Object.class) int getTotalPages();

	@JsonView(Object.class) int getSize();

	@JsonView(Object.class) int getNumber();

	@JsonView(Object.class) List<T> getContent();
}