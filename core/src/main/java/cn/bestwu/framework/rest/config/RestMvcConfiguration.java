package cn.bestwu.framework.rest.config;

import cn.bestwu.framework.rest.aspect.LogAspect;
import cn.bestwu.framework.rest.controller.BaseController;
import cn.bestwu.framework.rest.converter.DefaultElementMixIn;
import cn.bestwu.framework.rest.converter.PageMixIn;
import cn.bestwu.framework.rest.filter.AuthenticationFailureListener;
import cn.bestwu.framework.rest.filter.AuthorizationFailureListener;
import cn.bestwu.framework.rest.filter.OrderedHttpPutFormContentFilter;
import cn.bestwu.framework.rest.filter.ThreadLocalCleanFilter;
import cn.bestwu.framework.rest.mapping.RepositoryResourceMappings;
import cn.bestwu.framework.rest.mapping.SerializationViewMappings;
import cn.bestwu.framework.rest.mapping.VersionRepositoryRestRequestMappingHandlerMapping;
import cn.bestwu.framework.rest.resolver.*;
import cn.bestwu.framework.rest.support.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.hibernate.validator.HibernateValidator;
import org.springframework.aop.SpringProxy;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.WebMvcRegistrationsAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.querydsl.QueryDslUtils;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.FixQuerydslPredicateBuilder;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.repository.support.DefaultRepositoryInvokerFactory;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.filter.HttpPutFormContentFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.ArrayList;
import java.util.List;

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
	@ConditionalOnProperty(value = "logging.logAspect.enabled")
	@ConditionalOnWebApplication
	@ConditionalOnClass(SpringProxy.class)
	protected static class LoggerConfiguration {
		@Autowired
		private ApplicationEventPublisher publisher;

		@Bean
		@Order(Ordered.HIGHEST_PRECEDENCE + 10)
		public LogAspect logAspect() {
			return new LogAspect(publisher);
		}

		@Configuration
		@ConditionalOnProperty(value = "logging.logAspect.enabled")
		@ConditionalOnWebApplication
		@ConditionalOnClass(AbstractAuthenticationFailureEvent.class)
		protected static class FailureListener {

			@Bean
			public AuthenticationFailureListener authenticationFailureListener() {
				return new AuthenticationFailureListener();
			}

			@Bean
			public AuthorizationFailureListener authorizationFailureListener() {
				return new AuthorizationFailureListener();
			}
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
	public OrderedHttpPutFormContentFilter httpPutFormContentFilter() {
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

	@Autowired
	private ApplicationContext applicationContext;

	@Bean
	@ConditionalOnMissingBean(Repositories.class)
	public Repositories repositories() {
		return new Repositories(applicationContext);
	}

	@Bean
	public RepositoryResourceMappings repositoryResourceMappings() {
		return new RepositoryResourceMappings(repositories());
	}

	@Configuration
	@ConditionalOnWebApplication
	protected static class CustomWebMvcConfiguration extends WebMvcConfigurerAdapter {

		@Autowired
		private MessageSource messageSource;
		@Autowired
		private Repositories repositories;
		@Autowired
		private RepositoryResourceMappings repositoryResourceMappings;
		@Autowired
		private FormattingConversionService conversionService;

		/*
		 * 验证器
		 */
		@Override public Validator getValidator() {
			final LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
			localValidatorFactoryBean.setProviderClass(HibernateValidator.class);
			localValidatorFactoryBean.setValidationMessageSource(messageSource);
			return localValidatorFactoryBean;
		}

		@Bean
		public RepositoryInvokerFactory repositoryInvokerFactory() {
			return new UnwrappingRepositoryInvokerFactory(
					new DefaultRepositoryInvokerFactory(repositories, conversionService));
		}

		@Lazy
		@Bean
		public QuerydslBindingsFactory querydslBindingsFactory() {
			return new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE);
		}

		@Lazy
		@Bean
		public FixQuerydslPredicateBuilder querydslPredicateBuilder() {
			return new FixQuerydslPredicateBuilder(conversionService, querydslBindingsFactory().getEntityPathResolver());
		}

		@Bean
		public RepositoryResourceMetadataHandlerMethodArgumentResolver repositoryResourceMetadataHandlerMethodArgumentResolver() {
			return new RepositoryResourceMetadataHandlerMethodArgumentResolver(repositoryResourceMappings);
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

		@Override public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
			argumentResolvers.add(repoRequestArgumentResolver());
			argumentResolvers.add(new DomainMethodArgumentResolver(repositoryInvokerFactory(), messageConverters()));
			argumentResolvers
					.add(new ResourceMethodArgumentResolver(repositoryResourceMetadataHandlerMethodArgumentResolver(), repositoryInvokerFactory(), messageConverters()));
			if (QueryDslUtils.QUERY_DSL_PRESENT)
				argumentResolvers.add(0, new QuerydslPredicateArgumentResolver(querydslBindingsFactory(), querydslPredicateBuilder()));
			argumentResolvers.add(new ETagArgumentResolver());
		}

		@Override public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
			configurer.defaultContentType(MediaType.APPLICATION_JSON);
			configurer.favorParameter(true);
			configurer.parameterName("_format");
		}
	}

	@Configuration
	@ConditionalOnWebApplication
	protected static class RequestMappingHandlerAdapterConfig implements InitializingBean {

		@Autowired(required = false)
		private SerializationViewMappings serializationViewMappings;

		@Bean
		@ConditionalOnBean(SerializationViewMappings.class)
		public RequestJsonViewResponseBodyAdvice requestJsonViewResponseBodyAdvice() {
			return new RequestJsonViewResponseBodyAdvice(serializationViewMappings);
		}

		@Autowired
		private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

		@Override public void afterPropertiesSet() throws Exception {
			if (serializationViewMappings != null) {
				List<ResponseBodyAdvice<?>> requestJsonViewResponseBodyAdvices = new ArrayList<>(1);
				requestJsonViewResponseBodyAdvices.add(requestJsonViewResponseBodyAdvice());
				requestMappingHandlerAdapter.setResponseBodyAdvice(requestJsonViewResponseBodyAdvices);
			}
		}
	}

	@Configuration
	@ConditionalOnWebApplication
	protected static class WebMvcRegistrationsConfig extends WebMvcRegistrationsAdapter {
		@Autowired
		private RepositoryResourceMappings repositoryResourceMappings;

		@Autowired(required = false)
		private ProxyPathMapper proxyPathMapper;

		@Override public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
			return new VersionRepositoryRestRequestMappingHandlerMapping(repositoryResourceMappings, proxyPathMapper);
		}
	}
}
