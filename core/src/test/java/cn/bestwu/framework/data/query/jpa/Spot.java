package cn.bestwu.framework.data.query.jpa;

import cn.bestwu.framework.rest.annotation.TrimNotBlank;
import cn.bestwu.framework.util.PinyinUtil;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

/**
 * 目的地/景点
 *
 * @author Peter Wu
 */
@Getter @Setter
@Indexed
public class Spot {

	/**
	 * 名称
	 */
	@Field
	@TrimNotBlank
	@JsonView(Object.class)
	private String name;

	//--------------------------------------------

	//--------------------------------------------
	@Field(analyzer = @Analyzer(impl = StandardAnalyzer.class))
	public String getPinyin() {
		return PinyinUtil.getPinYin(name);
	}

	@Field(analyzer = @Analyzer(impl = StandardAnalyzer.class))
	public String getPinyinHead() {
		return PinyinUtil.getPinYinHead(name);
	}

}
