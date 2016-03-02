package cn.bestwu.framework.data;

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
	 * @param modelType 要搜索的类
	 * @param keyword    关键字
	 * @param pageable   分页
	 * @param highLight  是否高亮
	 * @param <T>        t
	 * @return 搜索结果
	 */
	<T> Page search(Class<T> modelType, String keyword, Pageable pageable, boolean highLight);

	/**
	 * 不处理结果的全文搜索
	 *
	 * @param modelType 要搜索的类
	 * @param keyword    关键字
	 * @param pageable   分页
	 * @param fieldArray fieldArray
	 * @param <T>        t
	 * @return 搜索结果
	 */
	<T> Page<T> search(Class<T> modelType, String keyword, Pageable pageable, String[] fieldArray);

}