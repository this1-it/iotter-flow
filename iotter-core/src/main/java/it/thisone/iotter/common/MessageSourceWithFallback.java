package it.thisone.iotter.common;

import java.text.MessageFormat;
import java.util.Locale;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Feature #286 Gestione lingua: se non è presenta la lingua comunicata dal browser fare fallback sull'inglese
 *
 */
public class MessageSourceWithFallback extends ReloadableResourceBundleMessageSource {

	final static private Locale defaultLocale = Locale.ENGLISH;

	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {
		MessageFormat result = super.resolveCode(code, locale);
		if ((result == null || result.toPattern().isEmpty()) && !locale.equals(defaultLocale)) {
			return super.resolveCode(code, defaultLocale);
		}
		return result;
	}

	@Override
	protected String resolveCodeWithoutArguments(String code, Locale locale) {
		String result = super.resolveCodeWithoutArguments(code, locale);
		if ( (result == null || result.isEmpty()) && !locale.equals(defaultLocale)) {
			return super.resolveCodeWithoutArguments(code, defaultLocale);
		}
		return result;
	}
}