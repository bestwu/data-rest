package cn.bestwu.framework.event;

import org.hibernate.Criteria;

/**
 * 全文搜索前设置其他条件
 *
 * @author Peter Wu
 */
public class BeforeSearchEvent extends RepositoryEvent {

	private static final long serialVersionUID = -4178548774162112340L;

	public BeforeSearchEvent(Criteria criteria, Class<?> modelType) {
		super(criteria, modelType);
	}
}