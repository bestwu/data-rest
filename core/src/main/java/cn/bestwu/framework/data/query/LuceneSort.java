package cn.bestwu.framework.data.query;

import org.springframework.data.domain.Sort;

/**
 * @author Peter Wu
 */
public class LuceneSort extends Sort {
	private static final long serialVersionUID = 5102236954953170762L;

	private org.apache.lucene.search.Sort sort;

	public LuceneSort(org.apache.lucene.search.Sort sort) {
		super("sort");
		this.sort = sort;
	}

	public org.apache.lucene.search.Sort getSort() {
		return sort;
	}

	public void setSort(org.apache.lucene.search.Sort sort) {
		this.sort = sort;
	}
}
