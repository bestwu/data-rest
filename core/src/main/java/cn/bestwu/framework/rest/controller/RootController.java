
package cn.bestwu.framework.rest.controller;

import cn.bestwu.api.sign.ApiSign;
import cn.bestwu.framework.rest.exception.ResourceNotFoundException;
import cn.bestwu.lang.util.CaptchaUtil;
import cn.bestwu.lang.util.Sha1DigestUtil;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 根路径
 *
 * @author Peter Wu
 */
@RestController
@ConditionalOnWebApplication
@Slf4j
public class RootController extends BaseController {

	/**
	 * 拼音
	 *
	 * @param word word
	 * @return word对应简拼
	 */
	@ApiSign
	@RequestMapping(value = "/utils/pinyin", method = RequestMethod.GET)
	public Object pinyin(String[] word) {
		Assert.notEmpty(word, getText("param.notnull", "word"));

		Map<String, String> map = new HashMap<>();
		for (String w : word) {
			try {
				map.put(w, PinyinHelper.getShortPinyin(w));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return ok(map);
	}

	@Value("${logging.path:}")
	private String logging_path;

	/**
	 * 查看日志
	 *
	 * @param index 页码
	 * @return 日志
	 * @throws IOException      IOException
	 * @throws ServletException ServletException
	 */
	@RequestMapping(value = "/logs/{index}", method = RequestMethod.GET, produces = { "text/html", "text/plain" })
	public Object showlog(@PathVariable int index) throws IOException, ServletException {

		if (!StringUtils.hasText(logging_path)) {
			logging_path = getRealPath("/WEB-INF/logs");
		}
		String logFilePath;

		if (index <= 0)
			logFilePath = logging_path + "/log";
		else if (index > 1000)
			logFilePath = logging_path + "/log.1000";
		else
			logFilePath = logging_path + "/log." + index;

		File logFile = new File(logFilePath);
		if (logFile.exists()) {
			String log = StreamUtils.copyToString(new FileInputStream(logFile), Charset.forName("UTF-8"));
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setETag("\"".concat(Sha1DigestUtil.shaHex(log)).concat("\""));
			return ResponseEntity.ok().headers(cacheControl(httpHeaders)).body(log);
		} else {
			throw new ResourceNotFoundException(getText("log.notFound"));
		}
	}

	/**
	 * 验证码httpSession参数名
	 */
	public static final String CAPTCHA = "CAPTCHA";

	/**
	 * 验证码
	 *
	 * @param httpSession httpSession
	 * @param response    response
	 * @throws IOException IOException
	 */
	@RequestMapping(value = "/utils/captcha", method = RequestMethod.GET)
	public void captcha(HttpSession httpSession, HttpServletResponse response) throws IOException {
		response.setContentType("image/jpeg");
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);

		String captcha = CaptchaUtil.generateCaptcha(4);
		httpSession.setAttribute(CAPTCHA, captcha);

		ServletOutputStream outputStream = response.getOutputStream();
		CaptchaUtil.generateImage(100, 40, outputStream, captcha);
		outputStream.flush();
		outputStream.close();
	}
}
