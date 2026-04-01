package it.thisone.iotter.ui.authentication;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.security.UserDetailsAdapter;

@Component
public class GoogleOAuth2SuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    static final String OAUTH2_AUTH_SESSION_KEY = "OAUTH2_PENDING_AUTH";

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        if (authentication instanceof OAuth2AuthenticationToken token) {
            String email = token.getPrincipal().getAttribute("email");
            User user = userService.findByName(email);

            if (user != null) {
                UserDetailsAdapter details = new UserDetailsAdapter(user);
                UsernamePasswordAuthenticationToken localAuth =
                    new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
                localAuth.setDetails(user);

                HttpSession session = request.getSession(true);
                session.setAttribute(OAUTH2_AUTH_SESSION_KEY, localAuth);
                response.sendRedirect("/oauth2callback");
                return;
            }
        }
        response.sendRedirect("/login?error=oauth2_no_account");
    }
}
