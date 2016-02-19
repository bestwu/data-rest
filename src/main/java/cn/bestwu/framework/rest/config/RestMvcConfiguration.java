package cn.bestwu.framework.rest.config;

import cn.bestwu.framework.rest.aspect.LogAspect;
import cn.bestwu.framework.rest.converter.DefaultElementMixIn;
import cn.bestwu.framework.rest.converter.PageMixIn;
import cn.bestwu.framework.rest.converter.StringToEnumConverterFactory;
import cn.bestwu.framework.rest.filter.OrderedHttpPutFormContentFilter;
import cn.bestwu.framework.rest.mapping.RepositoryResourceMappings;
import cn.bestwu.framework.rest.mapping.SerializationViewMappings;
import cn.bestwu.framework.rest.mapping.VersionRepositoryRestRequestMappingHandlerMapping;
import cn.bestwu.framework.rest.resolver.*;
import cn.bestwu.framework.rest.support.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.WebMvcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.web.OrderedRequestContextFilter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.querydsl.QueryDslUtils;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.FixQuerydslPredicateBuilder;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.repository.support.DefaultRepositoryInvokerFactory;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.ClassUtils;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.HttpPutFormContentFilter;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.resource.AppCacheManifestTransformer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.*;

/**
 * spring mvc 配置文件
 *
 * @author Peter Wu
 */
@Configuration
@ConditionalOnWebApplication
public class RestMvcConfiguration {

	@Autowired
	private ApplicationEventPublisher publisher;

	@Bean
	public LogAspect logAspect() {
		return new LogAspect(publisher);
	}

	@Bean
	public Jackson2ObjectMapperBuilder jacksonBuilder() {
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		builder.serializationInclusion(JsonInclude.Include.NON_NULL);
		builder.featuresToEnable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
		builder.featuresToDisable(SerializationFeature.WRITE_NULL_MAP_VALUES, SerializationFeature.FAIL_ON_EMPTY_BEANS);
		builder.mixIn(Page.class, PageMixIn.class);
		builder.mixIn(Object.class, DefaultElementMixIn.class);
		return builder;
	}

	@Bean
	@ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
	public BaseErrorAttributes errorAttributes() {
		return new BaseErrorAttributes();
	}

	/*
	 * PUT DELETE form 提交
	 */
	@Bean
	@ConditionalOnMissingBean(HttpPutFormContentFilter.class)
	public OrderedHttpPutFormContentFilter httpFormContentFilterRegistration() {
		return new OrderedHttpPutFormContentFilter();
	}

	/*
	 * 上传请求处理
	 */
	@Bean
	public MultipartResolver multipartResolver() {
		StandardServletMultipartResolver standardServletMultipartResolver = new StandardServletMultipartResolver();
		standardServletMultipartResolver.setResolveLazily(true);
		return standardServletMultipartResolver;
	}

	@Configuration
	@ConditionalOnBean({ JavaMailSenderImpl.class, MailProperties.class })
	@ConditionalOnMissingBean(MailClient.class)
	public static class MailSenderAutoConfiguration {

		@Autowired
		private MailProperties properties;
		@Autowired
		private JavaMailSenderImpl mailSender;

		@Bean
		public MailClient mailClient() {
			return new MailClient(mailSender, properties);
		}
	}

	@Configuration
	@ConditionalOnWebApplication
	@Import(EnableWebMvcConfiguration.class)
	@EnableConfigurationProperties({ WebMvcProperties.class, ResourceProperties.class })
	public static class WebMvcAutoConfigurationAdapter extends WebMvcConfigurerAdapter {

		private static final Log logger = LogFactory.getLog(WebMvcConfigurerAdapter.class);

		@Autowired(required = false)
		private ResourceProperties resourceProperties = new ResourceProperties();

		@Autowired
		private WebMvcProperties mvcProperties = new WebMvcProperties();

		@Autowired
		private ListableBeanFactory beanFactory;

		@Autowired
		private HttpMessageConverters messageConverters;

		@Override
		public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
			converters.addAll(this.messageConverters.getConverters());
		}

		@Override
		public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
			Long timeout = this.mvcProperties.getAsync().getRequestTimeout();
			if (timeout != null) {
				configurer.setDefaultTimeout(timeout);
			}
		}

		@Override
		public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
			Map<String, MediaType> mediaTypes = this.mvcProperties.getMediaTypes();
			for (String extension : mediaTypes.keySet()) {
				configurer.mediaType(extension, mediaTypes.get(extension));
			}
		}

		@Bean
		@ConditionalOnMissingBean
		public InternalResourceViewResolver defaultViewResolver() {
			InternalResourceViewResolver resolver = new InternalResourceViewResolver();
			resolver.setPrefix(this.mvcProperties.getView().getPrefix());
			resolver.setSuffix(this.mvcProperties.getView().getSuffix());
			return resolver;
		}

		@Bean
		@ConditionalOnMissingBean({ RequestContextListener.class, RequestContextFilter.class })
		public RequestContextFilter requestContextFilter() {
			return new OrderedRequestContextFilter();
		}

		@Bean
		@ConditionalOnBean(View.class)
		public BeanNameViewResolver beanNameViewResolver() {
			BeanNameViewResolver resolver = new BeanNameViewResolver();
			resolver.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
			return resolver;
		}

		@Bean
		@ConditionalOnBean(ViewResolver.class)
		@ConditionalOnMissingBean(name = "viewResolver", value = ContentNegotiatingViewResolver.class)
		public ContentNegotiatingViewResolver viewResolver(BeanFactory beanFactory) {
			ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
			resolver.setContentNegotiationManager(
					beanFactory.getBean(ContentNegotiationManager.class));
			// ContentNegotiatingViewResolver uses all the other view resolvers to locate
			// a view so it should have a high precedence
			resolver.setOrder(Ordered.HIGHEST_PRECEDENCE);
			return resolver;
		}

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnProperty(prefix = "spring.mvc", name = "locale")
		public LocaleResolver localeResolver() {
			return new FixedLocaleResolver(this.mvcProperties.getLocale());
		}

		@Bean
		@ConditionalOnProperty(prefix = "spring.mvc", name = "date-format")
		public Formatter<Date> dateFormatter() {
			return new DateFormatter(this.mvcProperties.getDateFormat());
		}

		@Override
		public MessageCodesResolver getMessageCodesResolver() {
			if (this.mvcProperties.getMessageCodesResolverFormat() != null) {
				DefaultMessageCodesResolver resolver = new DefaultMessageCodesResolver();
				resolver.setMessageCodeFormatter(
						this.mvcProperties.getMessageCodesResolverFormat());
				return resolver;
			}
			return null;
		}

		@Override
		public void addFormatters(FormatterRegistry registry) {
			getBeansOfType(Converter.class).forEach(registry::addConverter);
			getBeansOfType(GenericConverter.class).forEach(registry::addConverter);
			getBeansOfType(Formatter.class).forEach(registry::addFormatter);
		}

		private <T> Collection<T> getBeansOfType(Class<T> type) {
			return this.beanFactory.getBeansOfType(type).values();
		}

		//		@Override
		//		public void addResourceHandlers(ResourceHandlerRegistry registry) {
		//			if (!this.resourceProperties.isAddMappings()) {
		//				logger.debug("Default resource handling disabled");
		//				return;
		//			}
		//			Integer cachePeriod = this.resourceProperties.getCachePeriod();
		//			if (!registry.hasMappingForPattern("/webjars/**")) {
		//				registerResourceChain(registry.addResourceHandler("/webjars/**")
		//						.addResourceLocations("classpath:/META-INF/resources/webjars/")
		//						.setCachePeriod(cachePeriod));
		//			}
		//			String staticPathPattern = this.mvcProperties.getStaticPathPattern();
		//			if (!registry.hasMappingForPattern(staticPathPattern)) {
		//				registerResourceChain(registry.addResourceHandler(staticPathPattern)
		//						.addResourceLocations(
		//								this.resourceProperties.getStaticLocations())
		//						.setCachePeriod(cachePeriod));
		//			}
		//		}

		private void registerResourceChain(ResourceHandlerRegistration registration) {
			ResourceProperties.Chain properties = this.resourceProperties.getChain();
			if (properties.getEnabled()) {
				configureResourceChain(properties,
						registration.resourceChain(properties.isCache()));
			}
		}

		private void configureResourceChain(ResourceProperties.Chain properties,
				ResourceChainRegistration chain) {
			ResourceProperties.Strategy strategy = properties.getStrategy();
			if (strategy.getFixed().isEnabled() || strategy.getContent().isEnabled()) {
				chain.addResolver(getVersionResourceResolver(strategy));
			}
			if (properties.isHtmlApplicationCache()) {
				chain.addTransformer(new AppCacheManifestTransformer());
			}
		}

		private ResourceResolver getVersionResourceResolver(
				ResourceProperties.Strategy properties) {
			VersionResourceResolver resolver = new VersionResourceResolver();
			if (properties.getFixed().isEnabled()) {
				String version = properties.getFixed().getVersion();
				String[] paths = properties.getFixed().getPaths();
				resolver.addFixedVersionStrategy(version, paths);
			}
			if (properties.getContent().isEnabled()) {
				String[] paths = properties.getContent().getPaths();
				resolver.addContentVersionStrategy(paths);
			}
			return resolver;
		}

		@Override
		public void addViewControllers(ViewControllerRegistry registry) {
			Resource page = this.resourceProperties.getWelcomePage();
			if (page != null) {
				logger.info("Adding welcome page: " + page);
				registry.addViewController("/").setViewName("forward:index.html");
			}
		}
	}

	@Configuration
	@ConditionalOnWebApplication
	public static class EnableWebMvcConfiguration extends DelegatingWebMvcConfiguration {

		@Autowired(required = false)
		private WebMvcProperties mvcProperties;
		@Autowired
		private MessageSource messageSource;
		@Autowired
		private Repositories repositories;
		@Autowired
		private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;
		@Autowired
		private StringHttpMessageConverter stringHttpMessageConverter;

		@Bean
		@Override public FormattingConversionService mvcConversionService() {
			FormattingConversionService conversionService = super.mvcConversionService();
			conversionService.addConverterFactory(new StringToEnumConverterFactory());
			return conversionService;
		}

		@Bean
		public RepositoryInvokerFactory repositoryInvokerFactory() {
			return new UnwrappingRepositoryInvokerFactory(
					new DefaultRepositoryInvokerFactory(repositories, mvcConversionService()));
		}

		@Lazy
		@Bean
		public QuerydslBindingsFactory querydslBindingsFactory() {
			return new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE);
		}

		@Lazy
		@Bean
		public FixQuerydslPredicateBuilder querydslPredicateBuilder() {
			return new FixQuerydslPredicateBuilder(mvcConversionService(), querydslBindingsFactory().getEntityPathResolver());
		}

		@Bean
		public RepositoryResourceMetadataHandlerMethodArgumentResolver repositoryResourceMetadataHandlerMethodArgumentResolver() {
			return new RepositoryResourceMetadataHandlerMethodArgumentResolver(repositoryResourceMappings());
		}

		@Autowired
		protected ApplicationEventPublisher publisher;

		@Bean
		public RootResourceInformationHandlerMethodArgumentResolver repoRequestArgumentResolver() {

			if (QueryDslUtils.QUERY_DSL_PRESENT) {
				return new QuerydslAwareRootResourceInformationHandlerMethodArgumentResolver(repositories,
						repositoryInvokerFactory(), repositoryResourceMetadataHandlerMethodArgumentResolver(), querydslPredicateBuilder(), querydslBindingsFactory(), publisher);
			}

			return new RootResourceInformationHandlerMethodArgumentResolver(repositoryInvokerFactory(),
					repositoryResourceMetadataHandlerMethodArgumentResolver());
		}

		@Override protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
			super.addArgumentResolvers(argumentResolvers);

			argumentResolvers.add(repoRequestArgumentResolver());
			argumentResolvers.add(new DomainMethodArgumentResolver(repositoryInvokerFactory(), mappingJackson2HttpMessageConverter));
			argumentResolvers
					.add(new ResourceMethodArgumentResolver(repositoryResourceMetadataHandlerMethodArgumentResolver(), repositoryInvokerFactory(), mappingJackson2HttpMessageConverter));
			if (QueryDslUtils.QUERY_DSL_PRESENT)
				argumentResolvers.add(0, new QuerydslPredicateArgumentResolver(querydslBindingsFactory(), querydslPredicateBuilder(), publisher));
			argumentResolvers.add(new ETagArgumentResolver());
			argumentResolvers.add(new MultipartFileHolderMethodArgumentResolver());
			if (ClassUtils.isPresent("org.apache.lucene.search.Sort", RestMvcConfiguration.class.getClassLoader()))
				argumentResolvers.add(new SearchSortHandlerMethodArgumentResolver());
		}

		/*
		 * 验证器
		 */
		@Override protected Validator getValidator() {
			final LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
			localValidatorFactoryBean.setProviderClass(HibernateValidator.class);
			localValidatorFactoryBean.setValidationMessageSource(messageSource);
			return localValidatorFactoryBean;
		}

		@Override protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
			converters.add(mappingJackson2HttpMessageConverter);
			converters.add(stringHttpMessageConverter);
		}

		@Autowired(required = false)
		private SerializationViewMappings serializationViewMappings;

		/*
		 * 配置请求映射适配器
		 */
		@Bean
		@Override
		public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
			RequestMappingHandlerAdapter adapter = super.requestMappingHandlerAdapter();
			adapter.setIgnoreDefaultModelOnRedirect(this.mvcProperties == null || this.mvcProperties.isIgnoreDefaultModelOnRedirect());

			if (serializationViewMappings != null)
				adapter.setResponseBodyAdvice(Collections.singletonList(new RequestJsonViewResponseBodyAdvice(serializationViewMappings)));
			return adapter;
		}

		@Override protected void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
			configurer.enable();
			super.configureDefaultServletHandling(configurer);
		}

		@Bean
		public RepositoryResourceMappings repositoryResourceMappings() {
			return new RepositoryResourceMappings(repositories);
		}

		@Autowired(required = false)
		private ProxyPathMapper proxyPathMapper;

		@Override protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
			return new VersionRepositoryRestRequestMappingHandlerMapping(mvcContentNegotiationManager(),
					repositoryResourceMappings(), proxyPathMapper);
		}

		@Override public RequestMappingHandlerMapping requestMappingHandlerMapping() {
			return super.requestMappingHandlerMapping();
		}
	}
}
