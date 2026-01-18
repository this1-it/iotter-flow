package it.thisone.iotter.i18n;

import com.vaadin.flow.i18n.I18NProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Vaadin Flow I18NProvider implementation that bridges Flow's getTranslation() API
 * with the existing Spring MessageSource infrastructure.
 *
 * Supports 5 locales: English (default), Italian, German, Spanish, French.
 * Missing translations return "!{key}" format and fall back to English via MessageSourceWithFallback.
 */
@Component
public class FlowI18NProvider implements I18NProvider {

    private static final Logger logger = LoggerFactory.getLogger(FlowI18NProvider.class);

    private static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(
        Locale.ENGLISH,
        Locale.ITALIAN,
        Locale.GERMAN,
        new Locale("es"),
        Locale.FRENCH
    );

    private final MessageSource messageSource;

    @Autowired
    public FlowI18NProvider(MessageSource messageSource) {
        this.messageSource = messageSource;
        logger.info("FlowI18NProvider initialized with {} supported locales: {}",
            SUPPORTED_LOCALES.size(),
            SUPPORTED_LOCALES);
    }

    @Override
    public List<Locale> getProvidedLocales() {
        return SUPPORTED_LOCALES;
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        if (key == null) {
            logger.warn("Translation requested with null key");
            return "!null";
        }

        try {
            String translation = messageSource.getMessage(key, params, null, locale);
            if (translation == null) {
                logger.warn("Translation key '{}' not found for locale '{}'", key, locale);
                return "!" + key;
            }
            return translation;
        } catch (Exception e) {
            logger.warn("Error retrieving translation for key '{}' and locale '{}': {}",
                key, locale, e.getMessage());
            return "!" + key;
        }
    }
}
