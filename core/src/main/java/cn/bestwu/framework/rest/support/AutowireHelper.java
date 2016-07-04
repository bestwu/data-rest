package cn.bestwu.framework.rest.support;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * bean 导入工具
 *
 * @author Peter Wu
 */
@Configuration
public class AutowireHelper implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	/**
	 * @param clazz clazz
	 * @param <T>   clazz 对应的类型
	 * @return clazz 对应的Bean
	 */
	public static <T> T getBean(Class<T> clazz) {
		return applicationContext.getBean(clazz);
	}

	/**
	 * @return 如果当前线程是HttpServletRequest请求返回对应 request,否则返回:null
	 */
	public static HttpServletRequest getRequest() {
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (requestAttributes == null) {
			return null;
		}
		return requestAttributes.getRequest();
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) {
		AutowireHelper.applicationContext = applicationContext;
	}

}