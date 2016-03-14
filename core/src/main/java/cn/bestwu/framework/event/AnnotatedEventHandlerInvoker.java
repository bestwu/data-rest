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

public class AnnotatedEventHandlerInvoker implements ApplicationListener<RepositoryEvent>, BeanPostProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(AnnotatedEventHandlerInvoker.class);
	private static final String PARAMETER_MISSING = "Invalid event handler method %s! At least a single argument is required to determine the domain type for which you are interested in events.";

	private final MultiValueMap<Class<?>, EventHandlerMethod> handlerMethods = new LinkedMultiValueMap<>();

	@Override
	public void onApplicationEvent(RepositoryEvent event) {
		Class<?> modelType = event.getModelType();
		if (!handlerMethods.containsKey(modelType)) {
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

		for (EventHandlerMethod handlerMethod : handlerMethods.get(modelType)) {

			if (!handlerMethod.eventType.equals(eventType)) {
				continue;
			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("Invoking {} handler for {}.", event.getClass().getSimpleName(), event.getSource());
			}

			ReflectionUtils.invokeMethod(handlerMethod.method, handlerMethod.handler, args);
		}
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

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
			inspect(bean, method, HandleBeforeLinkSave.class, BeforeLinkSaveEvent.class);
			inspect(bean, method, HandleAfterLinkSave.class, AfterLinkSaveEvent.class);
			inspect(bean, method, HandleBeforeDelete.class, BeforeDeleteEvent.class);
			inspect(bean, method, HandleAfterDelete.class, AfterDeleteEvent.class);
			inspect(bean, method, HandleBeforeLinkDelete.class, BeforeLinkDeleteEvent.class);
			inspect(bean, method, HandleAfterLinkDelete.class, AfterLinkDeleteEvent.class);
			inspect(bean, method, HandleSelfRel.class, SelfRelEvent.class);
			inspect(bean, method, HandleAddLink.class, AddLinkEvent.class);
			inspect(bean, method, HandleDefaultSort.class, DefaultSortEvent.class);
			inspect(bean, method, HandleDefaultPredicate.class, DefaultPredicateEvent.class);
			inspect(bean, method, HandleAddPredicate.class, AddPredicateEvent.class);
			inspect(bean, method, HandleBeforeSearch.class, BeforeSearchEvent.class);
			inspect(bean, method, HandleSearchResult.class, SearchResultEvent.class);
		}, USER_METHODS);

		return bean;
	}

	private <T extends Annotation> void inspect(Object handler, Method method, Class<T> annotationType, Class<? extends RepositoryEvent> eventType) {

		T annotation = AnnotationUtils.findAnnotation(method, annotationType);

		if (annotation == null) {
			return;
		}

		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length == 0) {
			throw new IllegalStateException(String.format(PARAMETER_MISSING, method));
		}
		Class<?> modelType;
		if (annotationType.equals(HandleDefaultSort.class)) {
			modelType = ((HandleDefaultSort) annotation).value();
		} else if (annotationType.equals(HandleDefaultPredicate.class)) {
			modelType = ((HandleDefaultPredicate) annotation).value();
		} else if (annotationType.equals(HandleAddPredicate.class)) {
			modelType = ((HandleAddPredicate) annotation).value();
		} else if (annotationType.equals(HandleBeforeSearch.class)) {
			modelType = ((HandleBeforeSearch) annotation).value();
		} else {
			modelType = parameterTypes[0];
		}

		EventHandlerMethod handlerMethod = new EventHandlerMethod(eventType, handler, method);
		handlerMethods.add(modelType, handlerMethod);
	}

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

	public static final ReflectionUtils.MethodFilter USER_METHODS = method -> !method.isSynthetic() && //
			!method.isBridge() && //
			!ReflectionUtils.isObjectMethod(method) && //
			!ClassUtils.isCglibProxyClass(method.getDeclaringClass()) && //
			!ReflectionUtils.isCglibRenamedMethod(method);
}
