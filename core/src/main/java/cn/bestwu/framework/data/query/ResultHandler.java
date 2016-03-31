package cn.bestwu.framework.data.query;

import java.util.List;

/**
 * 处理搜索结果的函数接口
 */
@FunctionalInterface
public interface ResultHandler {

	void accept(List result);
}