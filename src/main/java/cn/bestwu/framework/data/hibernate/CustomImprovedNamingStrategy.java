package cn.bestwu.framework.data.hibernate;

import org.hibernate.cfg.ImprovedNamingStrategy;

/**
 * 自定义hibernate数据库表、字段、键名等名字生成策略
 */
public abstract class CustomImprovedNamingStrategy extends ImprovedNamingStrategy {
	private static final long serialVersionUID = -4585465139909400531L;

	public static final String SUFFIX = "_id";
	/**
	 * 前缀,一般定义为项目代码
	 */
	private final String prefix;

	public CustomImprovedNamingStrategy(String prefix) {
		this.prefix = prefix;
	}

	/*
	 * 类名转表名
	 *
	 */
	@Override public String classToTableName(String className) {
		return this.prefix + super.classToTableName(className);
	}

	/*
	 * 集合转表名
	 *
	 */
	@Override public String collectionTableName(String ownerEntity, String ownerEntityTable, String associatedEntity, String associatedEntityTable, String propertyName) {
		return this.prefix + super.collectionTableName(ownerEntity, ownerEntityTable, associatedEntity, associatedEntityTable, propertyName);
	}

	/*
	 * joinKey 列名
	 *
	 */
	@Override public String joinKeyColumnName(String joinedColumn, String joinedTable) {
		return super.joinKeyColumnName(joinedColumn, joinedTable) + SUFFIX;
	}

	/*
	 * 外键 列名
	 *
	 */
	@Override public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName, String referencedColumnName) {
		return super.foreignKeyColumnName(propertyName, propertyEntityName, propertyTableName, referencedColumnName) + SUFFIX;
	}
}
