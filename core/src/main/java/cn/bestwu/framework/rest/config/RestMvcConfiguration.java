package cn.bestwu.framework.rest.config;

import cn.bestwu.framework.rest.aspect.LogAspect;
import cn.bestwu.framework.rest.controller.BaseController;
import cn.bestwu.framework.rest.converter.DefaultElementMixIn;
import cn.bestwu.framework.rest.converter.PageMixIn;
import cn.bestwu.framework.rest.filter.OrderedHttpPutFormContentFilter;
import cn.bestwu.framework.rest.filter.ThreadLocalCleanFilter;
import cn.bestwu.framework.rest.mapping.RepositoryResourceMappings;
import cn.bestwu.framework.rest.mapping.SerializationViewMappings;
import cn.bestwu.framework.rest.mapping.VersionRepositoryRestRequestMappingHandlerMapping;
import cn.bestwu.framework.rest.resolver.*;
import cn.bestwu.framework.rest.support.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.HibernateValidator;
import org.springframework.aop.SpringProxy;
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
import org.springframework.boot.web.filter.OrderedRequestContextFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
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
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSenderImpl;
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
@Import({ MessageSourceConfiguration.class })
public class RestMvcConfiguration {

	/**
	 * 默认Controller
	 */
	@Configuration
	@ConditionalOnWebApplication
	@ComponentScan(basePackageClasses = BaseController.class)
	protected static class ControllerConfiguration {
	}

	/**
	 * @return 默认jackson2 module
	 */
	@Bean
	public Module defaultModule() {
		SimpleModule module = new SimpleModule();
		module.setMixInAnnotation(Page.class, PageMixIn.class);
		module.setMixInAnnotation(Object.class, DefaultElementMixIn.class);
		return module;
	}

	/**
	 * 日志
	 */
	@Configuration
	@ConditionalOnWebApplication
	@ConditionalOnClass(SpringProxy.class)
	protected static class LoggerConfiguration {
		@Autowired
		private ApplicationEventPublisher publisher;

		@Bean
		@ConditionalOnProperty(value = "logging.logAspect.enabled")
		@Order(Ordered.HIGHEST_PRECEDENCE + 10)
		public LogAspect logAspect() {
			return new LogAspect(publisher);
		}
	}

	/**
	 * @return 错误处理
	 */
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

	/**
	 * 清理 ThreadLocal 防止线程重用时数据出错
	 *
	 * @return ThreadLocalCleanFilter
	 */
	@Bean
	public ThreadLocalCleanFilter threadLocalCleanFilter() {
		return new ThreadLocalCleanFilter();
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

	/**
	 * 邮件发送
	 */
	@Configuration
	@ConditionalOnBean({ JavaMailSenderImpl.class, MailProperties.class })
	@ConditionalOnMissingBean(MailClient.class)
	protected static class MailSenderAutoConfiguration {

		@Autowired
		private MailProperties properties;
		@Autowired
		private JavaMailSenderImpl mailSender;

		@Bean
		public MailClient mailClient() {
			return new MailClient(mailSender, properties);
		}
	}

	/**
	 * WebMvc
	 */
	@Configuration
	@ConditionalOnWebApplication
	@Import(EnableWebMvcConfiguration.class)
	@EnableConfigurationProperties({ WebMvcProperties.class, ResourceProperties.class })
	@Slf4j
	protected static class WebMvcAutoConfigurationAdapter extends WebMvcConfigurerAdapter {

		@Autowired(required = false)
		private ResourceProperties resourceProperties = new ResourceProperties();

		@Autowired
		private WebMvcProperties mvcProperties = new WebMvcProperties();

		@Autowired
		private ListableBeanFactory beanFactory;

		@Autowired
		private HttpMessageConverters messageConverters;

		/**
		 * @param converters HttpMessageConverter
		 */
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
			getBeansOfType(ConverterFactory.class).forEach(registry::addConverterFactory);
		}

		private <T> Collection<T> getBeansOfType(Class<T> type) {
			return this.beanFactory.getBeansOfType(type).values();
		}

		//		@Override
		//		public void addResourceHandlers(ResourceHandlerRegistry registry) {
		//			if (!this.resourceProperties.isAddMappings()) {
		//				log.debug("Default resource handling disabled");
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

		//		private void registerResourceChain(ResourceHandlerRegistration registration) {
		//			ResourceProperties.Chain properties = this.resourceProperties.getChain();
		//			if (properties.getEnabled()) {
		//				configureResourceChain(properties,
		//						registration.resourceChain(properties.isCache()));
		//			}
		//		}

		//		private void configureResourceChain(ResourceProperties.Chain properties,
		//				ResourceChainRegistration chain) {
		//			ResourceProperties.Strategy strategy = properties.getStrategy();
		//			if (strategy.getFixed().isEnabled() || strategy.getContent().isEnabled()) {
		//				chain.addResolver(getVersionResourceResolver(strategy));
		//			}
		//			if (properties.isHtmlApplicationCache()) {
		//				chain.addTransformer(new AppCacheManifestTransformer());
		//			}
		//		}

		//		private ResourceResolver getVersionResourceResolver(
		//				ResourceProperties.Strategy properties) {
		//			VersionResourceResolver resolver = new VersionResourceResolver();
		//			if (properties.getFixed().isEnabled()) {
		//				String version = properties.getFixed().getVersion();
		//				String[] paths = properties.getFixed().getPaths();
		//				resolver.addFixedVersionStrategy(version, paths);
		//			}
		//			if (properties.getContent().isEnabled()) {
		//				String[] paths = properties.getContent().getPaths();
		//				resolver.addContentVersionStrategy(paths);
		//			}
		//			return resolver;
		//		}

		@Override
		public void addViewControllers(ViewControllerRegistry registry) {
			Resource page = this.resourceProperties.getWelcomePage();
			if (page != null) {
				log.info("Adding welcome page: " + page);
				registry.addViewController("/").setViewName("forward:index.html");
			}
		}
	}

	/**
	 * EnableWebMvc
	 */
	@Configuration
	@ConditionalOnWebApplication
	protected static class EnableWebMvcConfiguration extends DelegatingWebMvcConfiguration {

		@Autowired(required = false)
		private WebMvcProperties mvcProperties;
		@Autowired
		private MessageSource messageSource;
		@Autowired
		private StringHttpMessageConverter stringHttpMessageConverter;

		@Autowired
		private ApplicationContext applicationContext;

		@Bean
		@ConditionalOnMissingBean(Repositories.class)
		public Repositories repositories() {
			return new Repositories(applicationContext);
		}

		@Bean
		public RepositoryInvokerFactory repositoryInvokerFactory() {
			return new UnwrappingRepositoryInvokerFactory(
					new DefaultRepositoryInvokerFactory(repositories(), mvcConversionService()));
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
				return new QuerydslAwareRootResourceInformationHandlerMethodArgumentResolver(repositories(),
						repositoryInvokerFactory(), repositoryResourceMetadataHandlerMethodArgumentResolver(), querydslPredicateBuilder(), querydslBindingsFactory(), publisher);
			}

			return new RootResourceInformationHandlerMethodArgumentResolver(repositoryInvokerFactory(),
					repositoryResourceMetadataHandlerMethodArgumentResolver());
		}

		@Autowired
		private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;
		@Autowired(required = false)
		private MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter;

		private List<AbstractJackson2HttpMessageConverter> messageConverters() {
			List<AbstractJackson2HttpMessageConverter> messageConverters = new ArrayList<>(2);
			messageConverters.add(mappingJackson2HttpMessageConverter);
			if (mappingJackson2XmlHttpMessageConverter != null) {
				messageConverters.add(mappingJackson2XmlHttpMessageConverter);
			}
			return messageConverters;
		}

		@Override protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
			super.addArgumentResolvers(argumentResolvers);

			argumentResolvers.add(repoRequestArgumentResolver());
			argumentResolvers.add(new ModelMethodArgumentResolver(repositoryInvokerFactory(), messageConverters()));
			argumentResolvers
					.add(new ResourceMethodArgumentResolver(repositoryResourceMetadataHandlerMethodArgumentResolver(), repositoryInvokerFactory(), messageConverters()));
			if (QueryDslUtils.QUERY_DSL_PRESENT)
				argumentResolvers.add(0, new QuerydslPredicateArgumentResolver(querydslBindingsFactory(), querydslPredicateBuilder()));
			argumentResolvers.add(new ETagArgumentResolver());
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
			if (mappingJackson2XmlHttpMessageConverter != null) {
				converters.add(mappingJackson2XmlHttpMessageConverter);
			}
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
				adapter.setResponseBodyAdvice(Collections.singletonList(requestJsonViewResponseBodyAdvice()));
			return adapter;
		}

		@Bean
		@ConditionalOnBean(SerializationViewMappings.class)
		public RequestJsonViewResponseBodyAdvice requestJsonViewResponseBodyAdvice() {
			return new RequestJsonViewResponseBodyAdvice(serializationViewMappings);
		}

		@Override protected void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
			configurer.enable();
			super.configureDefaultServletHandling(configurer);
		}

		@Override
		protected void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
			configurer.defaultContentType(MediaType.APPLICATION_JSON);
			configurer.favorParameter(true);
			configurer.parameterName("_format");
		}

		@Bean
		public RepositoryResourceMappings repositoryResourceMappings() {
			return new RepositoryResourceMappings(repositories());
		}

		@Autowired(required = false)
		private ProxyPathMapper proxyPathMapper;

		@Override protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
			return new VersionRepositoryRestRequestMappingHandlerMapping(repositoryResourceMappings(), proxyPathMapper);
		}

		@Override public RequestMappingHandlerMapping requestMappingHandlerMapping() {
			return super.requestMappingHandlerMapping();
		}
	}
}
