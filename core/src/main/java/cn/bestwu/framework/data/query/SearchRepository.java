package cn.bestwu.framework.data.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 全文搜索类
 *
 * @author Peter Wu
 */
public interface SearchRepository {

	/**
	 * 可选高亮的全文搜索方法
	 *
	 * @param domainType     要搜索的类
	 * @param keyword       关键字
	 * @param pageable      分页
	 * @param resultHandler 结果处理
	 * @param <T>           t
	 * @return 搜索结果
	 */
	<T> Page search(Class<T> domainType, String keyword, Pageable pageable, ResultHandler resultHandler);

}