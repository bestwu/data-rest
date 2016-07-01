package cn.bestwu.framework.rest.converter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * 默认xml rootName MixIn
 *
 * @author Peter Wu
 */
@JsonRootName("xml")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public interface DefaultElementMixIn {
}