package cn.bestwu.framework.util.keyword.filter;

import java.util.Collection;

/**
 * 关键字过滤器
 */
public interface KeywordFilter {

	/**
	 * 根据指定策略替换关键字，使用不同的策略可实现高亮功能。
	 * 
	 * @param text
	 *            待匹配文本
	 * @return 替换后的结果字符串
	 */
	String replace(String text);

	void compile(Collection<String> keywords);
}
