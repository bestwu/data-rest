package cn.bestwu.framework.rest.converter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("xml")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public interface DefaultElementMixIn {
}