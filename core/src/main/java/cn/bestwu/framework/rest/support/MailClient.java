package cn.bestwu.framework.rest.support;

import cn.bestwu.framework.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 邮件发送端
 *
 * @author Peter Wu
 */
@Slf4j
public class MailClient {

	private final JavaMailSenderImpl mailSender;
	private String from;
	private final String alias;

	public MailClient(JavaMailSenderImpl mailSender, MailProperties properties) {
		this.mailSender = mailSender;
		this.from = properties.getProperties().get("from");
		if (this.from == null) {
			this.from = properties.getUsername();
		}
		this.alias = properties.getProperties().get("alias");
	}

	/**
	 * 发送邮件
	 *
	 * @param subject 标题
	 * @param content 内容
	 * @param mailTo  对象
	 * @throws UnsupportedEncodingException UnsupportedEncodingException
	 * @throws MessagingException MessagingException
	 */
	public void send(String subject, String content, String... mailTo) throws UnsupportedEncodingException, MessagingException {
		send(subject, content, null, mailTo);
	}

	/**
	 * 发送邮件
	 *
	 * @param subject     标题
	 * @param content     内容
	 * @param inlineFiles 内容附件（图片等）
	 * @param mailTo      对象
	 * @throws MessagingException MessagingException
	 * @throws UnsupportedEncodingException UnsupportedEncodingException
	 */
	public void send(String subject, String content, Map<String, String> inlineFiles, String... mailTo) throws MessagingException, UnsupportedEncodingException {
		if (log.isDebugEnabled())
			log.debug("正在给" + StringUtil.valueOf(mailTo) + "发送邮件");
		MimeMessage mimeMessage = mailSender.createMimeMessage();

		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
		if (StringUtils.hasText(alias)) {
			helper.setFrom(from, alias);
		} else {
			helper.setFrom(from);
		}
		helper.setTo(mailTo);
		helper.setSubject(subject);
		helper.setText(content, true);
		if (inlineFiles != null) {
			Set<Entry<String, String>> entrySet = inlineFiles.entrySet();
			for (Entry<String, String> entry : entrySet) {
				helper.addInline(entry.getKey(), new FileSystemResource(entry.getValue()));
			}
		}
		try {
			mailSender.send(mimeMessage);
		} catch (MailSendException e) {
			if (log.isWarnEnabled())
				log.warn("邮件发送失败：" + e.getMessage());
		}
		if (log.isDebugEnabled())
			log.debug("邮件发送完成");
	}

}
