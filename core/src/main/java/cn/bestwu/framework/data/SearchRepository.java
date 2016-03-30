package cn.bestwu.framework.data;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

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
	 * @param keyword   关键字
	 * @param pageable  分页
	 * @param highLight 是否高亮
	 * @param <T>       t
	 * @return 搜索结果
	 */
	<T> Page search(Class<T> modelType, String keyword, Pageable pageable, boolean highLight);

	/**
	 * 不处理结果的全文搜索
	 *
	 * @param modelType  要搜索的类
	 * @param keyword    关键字
	 * @param pageable   分页
	 * @param fieldArray fieldArray
	 * @param <T>        t
	 * @return 搜索结果
	 */
	<T> Page<T> search(Class<T> modelType, String keyword, Pageable pageable, String[] fieldArray);

	/**
	 * 高亮显示文章
	 *
	 * @param query     {@link Query}
	 * @param analyzer  analyzer
	 * @param data      要高亮的数据
	 * @param modelType modelType
	 * @param fields    需要高亮的字段   @return 高亮数据
	 * @param <T>       t
	 */
	default <T> void jpaHighLight(Query query, Analyzer analyzer, List<T> data, Class<T> modelType, String... fields) {
	}
}