package cn.bestwu.framework.event;

import cn.bestwu.framework.event.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 事件注解处理执行类
 *
 * @author Peter Wu
 */
public class AnnotatedEventHandlerInvoker implements ApplicationListener<RepositoryEvent>, BeanPostProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(AnnotatedEventHandlerInvoker.class);

	/**
	 * 事件处理方法丢失参数错误信息
	 */
	private static final String PARAMETER_MISSING = "Invalid event handler method %s! At least a single argument is required to determine the domain type for which you are interested in events.";

	/**
	 * 持有事件处理方法
	 */
	private final MultiValueMap<Class<?>, EventHandlerMethod> handlerMethods = new LinkedMultiValueMap<>();

	/**
	 * 事件触发
	 *
	 * @param event 事件
	 */
	@Override
	public void onApplicationEvent(RepositoryEvent event) {
		Class<?> domainType = event.getDomainType();
		if (!handlerMethods.containsKey(domainType)) {
			return;
		}

		Object src = event.getSource();
		List<Object> parameters = new ArrayList<>();
		parameters.add(src);
		if (event instanceof LinkedEvent) {
			parameters.add(((LinkedEvent) event).getLinked());
		}
		Object[] args = parameters.toArray();

		Class<? extends RepositoryEvent> eventType = event.getClass();

		for (EventHandlerMethod handlerMethod : handlerMethods.get(domainType)) {

			if (!handlerMethod.eventType.equals(eventType)) {
				continue;
			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("Invoking {} handler for {}.", event.getClass().getSimpleName(), event.getSource());
			}

			ReflectionUtils.invokeMethod(handlerMethod.method, handlerMethod.handler, args);
		}
	}

	/**
	 * bean 初始化前 skip
	 *
	 * @param bean     bean
	 * @param beanName bean name
	 * @return bean
	 * @throws BeansException BeansException
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
	 * bean 初始化后
	 * 如果是{@code RestEventHandler}注解的bean 将其处理事件注解注解的方法加入{@code handlerMethods}持有事件处理方法
	 *
	 * @param bean     bean
	 * @param beanName bean name
	 * @return bean
	 * @throws BeansException BeansException
	 */
	@Override
	public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {

		Class<?> beanType = ClassUtils.getUserClass(bean);
		RestEventHandler typeAnno = AnnotationUtils.findAnnotation(beanType, RestEventHandler.class);

		if (typeAnno == null) {
			return bean;
		}
		ReflectionUtils.doWithMethods(beanType, method -> {

			inspect(bean, method, HandleBeforeCreate.class, BeforeCreateEvent.class);
			inspect(bean, method, HandleAfterCreate.class, AfterCreateEvent.class);
			inspect(bean, method, HandleBeforeSave.class, BeforeSaveEvent.class);
			inspect(bean, method, HandleBeforeShow.class, BeforeShowEvent.class);
			inspect(bean, method, HandleAfterSave.class, AfterSaveEvent.class);
			inspect(bean, method, HandleBeforeDelete.class, BeforeDeleteEvent.class);
			inspect(bean, method, HandleAfterDelete.class, AfterDeleteEvent.class);
			inspect(bean, method, HandleItemResource.class, ItemResourceEvent.class);
			inspect(bean, method, HandleDefaultSort.class, DefaultSortEvent.class);
			inspect(bean, method, HandleDefaultPredicate.class, DefaultPredicateEvent.class);
			inspect(bean, method, HandleQueryBuilder.class, QueryBuilderEvent.class);
		}, USER_METHODS);

		return bean;
	}

	/**
	 * 方法检查
	 *
	 * @param handler        RestEventHandler 注解的bean
	 * @param method         方法
	 * @param annotationType 注解
	 * @param eventType      事件类型
	 * @param <T>            <T>
	 */
	private <T extends Annotation> void inspect(Object handler, Method method, Class<T> annotationType, Class<? extends RepositoryEvent> eventType) {

		T annotation = AnnotationUtils.findAnnotation(method, annotationType);

		if (annotation == null) {
			return;
		}

		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length == 0) {
			throw new IllegalStateException(String.format(PARAMETER_MISSING, method));
		}
		Class<?> domainType;
		if (annotationType.equals(HandleDefaultSort.class)) {
			domainType = ((HandleDefaultSort) annotation).value();
		} else if (annotationType.equals(HandleDefaultPredicate.class)) {
			domainType = ((HandleDefaultPredicate) annotation).value();
		} else if (annotationType.equals(HandleQueryBuilder.class)) {
			domainType = ((HandleQueryBuilder) annotation).value();
		} else {
			domainType = parameterTypes[0];
		}

		EventHandlerMethod handlerMethod = new EventHandlerMethod(eventType, handler, method);
		handlerMethods.add(domainType, handlerMethod);
	}

	/**
	 * 事件 Handler方法
	 */
	static class EventHandlerMethod {

		final Class<? extends RepositoryEvent> eventType;
		final Method method;
		final Object handler;

		private EventHandlerMethod(Class<? extends RepositoryEvent> eventType, Object handler, Method method) {

			this.eventType = eventType;
			this.method = method;
			this.handler = handler;

			ReflectionUtils.makeAccessible(this.method);
		}

		@Override
		public String toString() {
			return String.format("EventHandlerMethod{ eventType=%s, method=%s, handler=%s }", eventType, method, handler);
		}
	}

	/**
	 * 用户定义的方法，排除反射代理生成的方法
	 */
	public static final ReflectionUtils.MethodFilter USER_METHODS = method -> !method.isSynthetic() && //
			!method.isBridge() && //
			!ReflectionUtils.isObjectMethod(method) && //
			!ClassUtils.isCglibProxyClass(method.getDeclaringClass()) && //
			!ReflectionUtils.isCglibRenamedMethod(method);
}
