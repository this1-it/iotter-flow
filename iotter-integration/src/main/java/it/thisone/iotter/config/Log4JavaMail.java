package it.thisone.iotter.config;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.slf4j.Logger;

/**

 * <code>
 * Log4JavaMail log4JavaMail = new Log4JavaMail(logger, "utf-8");<br/>
 * Session session = Session.getInstance(props, null); // 获得邮件会话对象<br/>
 * session.setDebugOut(log4JavaMail); <br/>
 * session.setDebug(true);<br/>
 * </code><br/>
 * 
 *
 */
public class Log4JavaMail extends PrintStream {
	private static byte[] CRLF = new byte[] { 13, 10 };
	private ByteArrayOutputStream bos = new ByteArrayOutputStream();
	private Logger logger;
	private String charset;
	private boolean hitData;

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public Log4JavaMail(Logger log) {
		super(System.out);
		this.logger = log;
	}

	public Log4JavaMail(Logger log, String charset) {
		super(System.out);
		this.logger = log;
		this.charset = charset;
	}

	@Override
	public void write(byte buf[], int off, int len) {
		bos.write(buf, off, len);
		if (len > 1) {
			if (buf[off + len - 2] == CRLF[0]
					&& buf[off + len - 1] == CRLF[1]) {
				flush();
			}
		}
	}

	@Override
	public void flush() {
		if (charset == null) {
			charset = "utf-8";
		}
		try {
			String msg = new String(bos.toByteArray(), charset);
			if (msg.endsWith("\r\n")) {
				msg = msg.substring(0, msg.length() - 2);
			}

			if (msg.equals("DATA")) {
				hitData = true;
			} else if (msg.equals(".\r\n")) {
				hitData = false;
			}

			if (hitData) {
				logger.trace(msg);
			} else {
				logger.debug(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		bos.reset();
	}

	@Override
	public void println(String x) {
		logger.debug(x);
	}
}