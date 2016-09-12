package cn.bestwu.framework.rest.filter;

import cn.bestwu.framework.util.ResourceUtil;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 清理 ThreadLocal 防止线程重用时数据出错
 *
 * @author Peter Wu
 */
public class ThreadLocalCleanFilter extends OncePerRequestFilter implements Ordered {

	private int order = Ordered.HIGHEST_PRECEDENCE + 1;

	@Override
	public int getOrder() {
		return this.order;
	}

	/**
	 * Set the order for this filter.
	 *
	 * @param order the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	protected void doFilterInternal(final HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		filterChain.doFilter(request, response);
		ResourceUtil.API_SIGNATURE.remove();
		ResourceUtil.REQUEST_VERSION.remove();
		ResourceUtil.REQUEST_METHOD.remove();
	}

}

