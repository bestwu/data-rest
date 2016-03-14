package cn.bestwu.framework.rest.support;

import cn.bestwu.framework.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class MailClient {

	private final Logger logger = LoggerFactory.getLogger(MailClient.class);

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

	public void send(String subject, String content, String... mailTo) throws UnsupportedEncodingException, MessagingException {
		send(subject, content, null, mailTo);
	}

	public void send(String subject, String content, Map<String, String> inlineFiles, String... mailTo) throws MessagingException, UnsupportedEncodingException {
		if (logger.isDebugEnabled())
			logger.debug("正在给{}发送邮件", StringUtil.valueOf(mailTo));
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
			if (logger.isWarnEnabled())
				logger.warn("邮件发送失败：{}", e.getMessage());
		}
		if (logger.isDebugEnabled())
			logger.debug("邮件发送完成");
	}

}
