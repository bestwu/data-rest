package cn.bestwu.framework.rest.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * 支持PUT上传
 *
 * @author Peter Wu
 */
@Slf4j
public class StandardServletMultipartResolver implements MultipartResolver {

	private boolean resolveLazily = false;

	public void setResolveLazily(boolean resolveLazily) {
		this.resolveLazily = resolveLazily;
	}

	@Override
	public boolean isMultipart(HttpServletRequest request) {
		// Same check as in Commons FileUpload...
		String method = request.getMethod();
		if (RequestMethod.POST.name().equals(method) || RequestMethod.PUT.name().equals(method)) {//支持PUT方法
			String contentType = request.getContentType();
			return (contentType != null && contentType.toLowerCase().startsWith("multipart/"));
		}
		return false;
	}

	@Override
	public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
		return new StandardMultipartHttpServletRequest(request, this.resolveLazily);
	}

	@Override
	public void cleanupMultipart(MultipartHttpServletRequest request) {
		// To be on the safe side: explicitly delete the parts,
		// but only actual file parts (for Resin compatibility)
		try {
			request.getParts().stream().filter(part -> request.getFile(part.getName()) != null).forEach(part -> {
				try {
					part.delete();
				} catch (Exception ex) {
					if (log.isWarnEnabled()) {
						log.warn("Failed to perform cleanup of multipart items", ex);
					}
				}
			});
		} catch (Exception ex) {
			if (log.isWarnEnabled()) {
				log.warn("Failed to perform cleanup of multipart items", ex);
			}
		}
	}

}
