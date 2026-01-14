package it.thisone.iotter.ui.validators;

import com.vaadin.flow.data.binder.Validator.RegexpValidator;

/*
 * https://vaadin.com/security/2021-03-11
 * 
 * https://owasp.org/www-community/attacks/Regular_expression_Denial_of_Service_-_ReDoS
 * 
 * https://github.com/vaadin/framework/commit/754ca011d79af3dd4fa9046ff0ade5367f5e6246
 * 
 * a@a.m5qRt8zLxQG4mMeu9yKZm5qRt8zLxQG4mMeu9yKZm5qRt8zLxQG4mMeu9yKZ&
 * 
 */

@SuppressWarnings("serial")
public class AntiReDoSEmailValidator extends RegexpValidator {
	private static final String PATTERN = "^" + "([a-zA-Z0-9_\\.\\-+])+" // local
            + "@" + "[a-zA-Z0-9-.]+" // domain
            + "\\." + "[a-zA-Z0-9-]{2,}" // tld
            + "$";
	
    /**
     * Creates a validator for checking that a string is a syntactically valid
     * e-mail address.
     *
     * @param errorMessage
     *            the message to display in case the value does not validate.
     */
    public AntiReDoSEmailValidator(String errorMessage) {
        super(errorMessage,PATTERN);
    }
}
