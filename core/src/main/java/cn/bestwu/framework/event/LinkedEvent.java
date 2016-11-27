package cn.bestwu.framework.event;

/**
 * 事件：关联
 *
 * @author Peter Wu
 */
public abstract class LinkedEvent extends RepositoryEvent {

	private static final long serialVersionUID = -772062121414001949L;
	private final Object linked;

	/**
	 * @param source    实体
	 * @param linked    关联实体
	 * @param domainType 实体类型
	 */
	public LinkedEvent(Object source, Object linked, Class<?> domainType) {
		super(source, domainType);
		this.linked = linked;
	}

	/**
	 * @return 关联实体
	 */
	public Object getLinked() {
		return linked;
	}
}
