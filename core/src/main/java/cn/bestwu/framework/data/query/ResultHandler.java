package cn.bestwu.framework.data.query;

import java.util.List;

/**
 * 处理查询结果的函数接口
 *
 * @author Peter Wu
 */
@FunctionalInterface
public interface ResultHandler {

	void accept(List result);
}