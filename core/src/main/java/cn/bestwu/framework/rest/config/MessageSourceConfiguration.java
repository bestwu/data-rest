package cn.bestwu.framework.rest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import static org.springframework.util.StringUtils.commaDelimitedListToStringArray;
import static org.springframework.util.StringUtils.trimAllWhitespace;

/**
 * 信息源配置
 *
 * @author Peter Wu
 */
@Configuration
public class MessageSourceConfiguration {

	/**
	 * properties
	 */
	@ConfigurationProperties(prefix = "spring.messages")
	public static class MessageSourceProperties {
		public static final String BASEMESSAGES = "basemessages";

		/**
		 * Comma-separated list of basenames, each following the ResourceBundle convention.
		 * Essentially a fully-qualified classpath location. If it doesn't contain a package
		 * qualifier (such as "org.mypackage"), it will be resolved from the classpath root.
		 */
		private String basename = "messages";

		/**
		 * Message bundles encoding.
		 */
		private String encoding = "utf-8";

		/**
		 * Loaded resource bundle files cache expiration, in seconds. When set to -1, bundles
		 * are cached forever.
		 */
		private int cacheSeconds = -1;

		public String getBasename() {
			return this.basename;
		}

		public void setBasename(String basename) {
			this.basename = basename;
		}

		public String getEncoding() {
			return this.encoding;
		}

		public void setEncoding(String encoding) {
			this.encoding = encoding;
		}

		public int getCacheSeconds() {
			return this.cacheSeconds;
		}

		public void setCacheSeconds(int cacheSeconds) {
			this.cacheSeconds = cacheSeconds;
		}
	}

	/**
	 * 加入basemessages及配置的信息源
	 */
	@Configuration
	@Order(Ordered.HIGHEST_PRECEDENCE)
	@EnableConfigurationProperties(MessageSourceConfiguration.MessageSourceProperties.class)
	@Conditional(ResourceBundleCondition.class)
	public static class ResourceBundleConditionMessageSourceConfiguration {
		@Autowired
		MessageSourceProperties messageSourceProperties;

		@Bean
		public MessageSource messageSource() {
			ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
			String basename = messageSourceProperties.getBasename() + "," + MessageSourceProperties.BASEMESSAGES;
			messageSource.setBasenames(commaDelimitedListToStringArray(trimAllWhitespace(basename)));
			messageSource.setDefaultEncoding(messageSourceProperties.getEncoding());
			messageSource.setCacheSeconds(messageSourceProperties.getCacheSeconds());
			return messageSource;
		}
	}

	@Configuration
	@Order(Ordered.HIGHEST_PRECEDENCE)
	@EnableConfigurationProperties(MessageSourceConfiguration.MessageSourceProperties.class)
	@Conditional(ResourceNotBundleCondition.class)
	public static class ResourceNotBundleConditionMessageSourceConfiguration {
		@Autowired
		MessageSourceProperties messageSourceProperties;

		@Bean
		public MessageSource messageSource() {
			ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
			messageSource.setBasenames(commaDelimitedListToStringArray(trimAllWhitespace(MessageSourceProperties.BASEMESSAGES)));
			messageSource.setDefaultEncoding(messageSourceProperties.getEncoding());
			messageSource.setCacheSeconds(messageSourceProperties.getCacheSeconds());
			return messageSource;
		}
	}

	private static final Resource[] NO_RESOURCES = {};

	protected static class ResourceNotBundleCondition extends ResourceBundleCondition {
		@Override public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return ConditionOutcome.inverse(super.getMatchOutcome(context, metadata));
		}
	}

	protected static class ResourceBundleCondition extends SpringBootCondition {

		private static final ConcurrentReferenceHashMap<String, ConditionOutcome> cache = new ConcurrentReferenceHashMap<>();

		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context,
				AnnotatedTypeMetadata metadata) {
			String basename = context.getEnvironment().getProperty(
					"spring.messages.basename", "messages");
			ConditionOutcome outcome = cache.get(basename);
			if (outcome == null) {
				outcome = getMatchOutcomeForBasename(context, basename);
				cache.put(basename, outcome);
			}
			return outcome;
		}

		private ConditionOutcome getMatchOutcomeForBasename(ConditionContext context,
				String basename) {
			for (String name : commaDelimitedListToStringArray(trimAllWhitespace(basename))) {
				for (Resource resource : getResources(context.getClassLoader(), name)) {
					if (resource.exists()) {
						return ConditionOutcome.match("Bundle found for "
								+ "spring.messages.basename: " + name);
					}
				}
			}
			return ConditionOutcome.noMatch("No bundle found for "
					+ "spring.messages.basename: " + basename);
		}

		private Resource[] getResources(ClassLoader classLoader, String name) {
			try {
				return new SkipPatternPathMatchingResourcePatternResolver(classLoader)
						.getResources("classpath*:" + name + "*.properties");
			} catch (Exception ex) {
				return NO_RESOURCES;
			}
		}

	}

	/**
	 * {@link PathMatchingResourcePatternResolver} that skips well known JARs that don't
	 * contain messages.properties.
	 */
	private static class SkipPatternPathMatchingResourcePatternResolver extends
			PathMatchingResourcePatternResolver {

		private static final ClassLoader ROOT_CLASSLOADER;

		static {
			ClassLoader classLoader = null;
			try {
				classLoader = ClassLoader.getSystemClassLoader();
				while (classLoader.getParent() != null) {
					classLoader = classLoader.getParent();
				}
			} catch (Throwable ignored) {
			}
			ROOT_CLASSLOADER = classLoader;
		}

		private static final String[] SKIPPED = { "aspectjweaver-", "hibernate-core-",
				"hsqldb-", "jackson-annotations-", "jackson-core-", "jackson-databind-",
				"javassist-", "snakeyaml-", "spring-aop-", "spring-beans-",
				"spring-boot-", "spring-boot-actuator-", "spring-boot-autoconfigure-",
				"spring-core-", "spring-context-", "spring-data-commons-",
				"spring-expression-", "spring-jdbc-", "spring-orm-", "spring-tx-",
				"spring-web-", "spring-webmvc-", "tomcat-embed-", "joda-time-",
				"hibernate-entitymanager-", "hibernate-validator-", "logback-classic-",
				"logback-core-", "thymeleaf-" };

		public SkipPatternPathMatchingResourcePatternResolver(ClassLoader classLoader) {
			super(classLoader);
		}

		@Override
		protected void addAllClassLoaderJarRoots(ClassLoader classLoader,
				Set<Resource> result) {
			if (classLoader != ROOT_CLASSLOADER) {
				super.addAllClassLoaderJarRoots(classLoader, result);
			}
		}

		@Override
		protected Set<Resource> doFindAllClassPathResources(String path)
				throws IOException {
			Set<Resource> resources = super.doFindAllClassPathResources(path);
			for (Iterator<Resource> iterator = resources.iterator(); iterator.hasNext(); ) {
				Resource resource = iterator.next();
				for (String skipped : SKIPPED) {
					if (resource.getFilename().startsWith(skipped)) {
						iterator.remove();
						break;
					}
				}
			}
			return resources;
		}

	}

}
