package cn.bestwu.framework.data.query.jpa;

import cn.bestwu.framework.data.query.ResultHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.util.List;

/**
 * 高亮查询结果
 *
 * @author Peter Wu
 */
@Slf4j
public class HighlightResultHandler implements ResultHandler {

	private Query query;
	private Analyzer analyzer;
	private Class<?> domainType;
	/**
	 * 需要高亮的字段
	 */
	private String[] highLightFields;
	/**
	 * //<span style=\"color: #ff0000; \"> </span>
	 * <p>
	 * <font></font> 客户端兼容性更高
	 */
	private String preTag = "<font color=\"#ff0000\">";
	private String postTag = "</font>";

	//--------------------------------------------

	public HighlightResultHandler() {
	}

	public HighlightResultHandler(String preTag, String postTag) {
		this.preTag = preTag;
		this.postTag = postTag;
	}

	//--------------------------------------------
	public void setQuery(Query query) {
		this.query = query;
	}

	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	public void setDomainType(Class<?> domainType) {
		this.domainType = domainType;
	}

	public void setHighLightFields(String[] highLightFields) {
		this.highLightFields = highLightFields;
	}

	//--------------------------------------------
	@Override public void accept(List result) {
		QueryScorer queryScorer = new QueryScorer(query);
		Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(preTag, postTag), queryScorer);
		for (Object t : result) {
			for (String fieldName : highLightFields) {
				try {
					if (fieldName.contains(".")) {
						hightLightField(analyzer, domainType, highlighter, t, fieldName);
					} else {
						Object fieldValue = ReflectionUtils.invokeMethod(BeanUtils.getPropertyDescriptor(domainType, fieldName).getReadMethod(), t);
						String highLightFieldValue = highlighter.getBestFragment(analyzer, fieldName, String.valueOf(fieldValue));
						if (highLightFieldValue != null) {
							ReflectionUtils.invokeMethod(BeanUtils.getPropertyDescriptor(domainType, fieldName).getWriteMethod(), t, highLightFieldValue);
						}
					}
				} catch (Exception e) {
					//不处理，只记录日志
					log.error("高亮显示关键字失败", e);
				}

			}
		}
	}

	/**
	 * 高亮某个字段
	 *
	 * @param analyzer    analyzer
	 * @param domainType   domainType
	 * @param highlighter highlighter
	 * @param t           t
	 * @param fieldName   fieldName
	 * @throws NoSuchFieldException NoSuchFieldException
	 * @throws IOException IOException
	 * @throws InvalidTokenOffsetsException InvalidTokenOffsetsException
	 */
	private void hightLightField(Analyzer analyzer, Class<?> domainType, Highlighter highlighter, Object t, String fieldName) throws NoSuchFieldException, IOException, InvalidTokenOffsetsException {
		if (fieldName.contains(".")) {
			String[] split = fieldName.split("\\.");
			String pfieldName = split[0];
			Object fieldValue = ReflectionUtils.invokeMethod(BeanUtils.getPropertyDescriptor(domainType, pfieldName).getReadMethod(), t);
			Class<?> fieldType = domainType.getDeclaredField(pfieldName).getType();
			String propertyName = split[1];
			if (fieldValue != null) {
				hightLightField(analyzer, fieldType, highlighter, fieldValue, propertyName);
			}
		} else {
			Object fieldValue = ReflectionUtils.invokeMethod(BeanUtils.getPropertyDescriptor(domainType, fieldName).getReadMethod(), t);
			String text = String.valueOf(fieldValue);
			if (!text.contains(preTag)) {
				String highLightFieldValue = highlighter.getBestFragment(analyzer, fieldName, text);
				if (highLightFieldValue != null) {
					ReflectionUtils.invokeMethod(BeanUtils.getPropertyDescriptor(domainType, fieldName).getWriteMethod(), t, highLightFieldValue);
				}
			}
		}
	}

}