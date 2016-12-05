package cn.bestwu.framework.data.query.jpa;

import com.fasterxml.jackson.annotation.JsonView;
import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import lombok.Getter;
import lombok.Setter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.constraints.NotBlank;

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
	@NotBlank
	@JsonView(Object.class)
	private String name;

	//--------------------------------------------

	//--------------------------------------------
	@Field(analyzer = @Analyzer(impl = StandardAnalyzer.class))
	public String getPinyin() throws PinyinException {
		return PinyinHelper.convertToPinyinString(name, "", PinyinFormat.WITHOUT_TONE);
	}

	@Field(analyzer = @Analyzer(impl = StandardAnalyzer.class))
	public String getPinyinHead() throws PinyinException {
		return PinyinHelper.getShortPinyin(name);
	}

}
