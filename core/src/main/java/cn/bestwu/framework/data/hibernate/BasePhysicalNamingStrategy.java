package cn.bestwu.framework.data.hibernate;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;

public class BasePhysicalNamingStrategy extends SpringPhysicalNamingStrategy {

	/**
	 * 前缀,一般定义为项目代码
	 */
	private final String tableNamePrefix;

	public BasePhysicalNamingStrategy(String tableNamePrefix) {
		this.tableNamePrefix = tableNamePrefix;
	}

	@Override public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return super.toPhysicalCatalogName(name, jdbcEnvironment);
	}

	@Override public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return super.toPhysicalSchemaName(name, jdbcEnvironment);
	}

	@Override
	public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
		return Identifier.toIdentifier(this.tableNamePrefix + super.toPhysicalTableName(name, context).getText(), name.isQuoted());
	}

	@Override public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return super.toPhysicalSequenceName(name, jdbcEnvironment);
	}

	@Override public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return super.toPhysicalColumnName(name, jdbcEnvironment);
	}
}