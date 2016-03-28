package cn.bestwu.framework.rest.support;

import org.springframework.web.multipart.MultipartFile;

/**
 * MultipartFile 持有对象，解决有MultipartFile参数时，请求必须是multipart/form-data type
 *
 * @author Peter Wu
 */
public class MultipartFileHolder {
	private MultipartFile file;

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}
}
