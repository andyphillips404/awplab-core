package com.awplab.core.vaadin.service.provider;

import com.awplab.core.vaadin.service.BasicAuthRequired;
import org.osgi.service.http.HttpContext;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;

/**
 * Created by andyphillips404 on 8/11/16.
 */
public class BasicAuthHttpContext implements HttpContext {

    private HttpContext defaultContext;

    private BasicAuthRequired authRequired;

    public BasicAuthHttpContext(HttpContext defaultContext, BasicAuthRequired authRequired) {
        this.defaultContext = defaultContext;
        this.authRequired = authRequired;
    }

    @Override
    public boolean handleSecurity(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {


        boolean isSecure = httpServletRequest.isSecure();
        if (httpServletRequest.getHeader("X-Forwarded-Proto") != null &&
                (httpServletRequest.getHeader("X-Forwarded-Proto").contains("https") || httpServletRequest.getHeader("X-Forwarded-Proto").contains("HTTPS"))) {
            isSecure = true;
        }

        if (authRequired.requireSecure() && !isSecure) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Secure connection required!");
            return false;
        }

        if (httpServletRequest.getHeader("Authorization") == null) {
            httpServletResponse.addHeader("WWW-Authenticate", HttpServletRequest.BASIC_AUTH + " realm=\"" + authRequired.httpRealm() + "\"");
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }


        if (authenticated(httpServletRequest)) {
            return true;
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

    }

    private boolean authenticated(HttpServletRequest request) {
        request.setAttribute(AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);


        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.length() > 0) {

            // Get the authType (Basic, Digest) and authInfo (user/password)
            // from
            // the header
            authHeader = authHeader.trim();
            int blank = authHeader.indexOf(' ');
            if (blank > 0) {
                String authType = authHeader.substring(0, blank);
                String authInfo = authHeader.substring(blank).trim();

                if (authType.equalsIgnoreCase("Basic")) {
                    final String userNamePassword = new String(Base64.getDecoder().decode(authInfo));
                    final String userNamePasswordRegex = "^(.*?)(\\:)(.*?)$";

                    if (userNamePassword.matches(userNamePasswordRegex)) {
                        String userName = userNamePassword.replaceAll(userNamePasswordRegex, "$1");
                        String password = userNamePassword.replaceAll(userNamePasswordRegex, "$3");
                        try {
                            Subject subject = new Subject();

                            final LoginContext loginContext = new LoginContext(authRequired.karafRealm(), subject, callbacks -> {
                                for (Callback callback : callbacks) {
                                    if (callback instanceof NameCallback) {
                                        ((NameCallback) callback).setName(userName);
                                    }
                                    if (callback instanceof PasswordCallback) {
                                        ((PasswordCallback) callback).setPassword(password.toCharArray());
                                    }
                                }
                            });
                            loginContext.login();

                            request.setAttribute(BasicAuthRequired.SUBJECT_REQUEST_ATTRIBUTE, subject);
                            return authRequired.allowSubject(subject);


                        } catch (Exception ignored) {
                            //System.out.print(ignored);
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public URL getResource(String s) {
        return defaultContext.getResource(s);
    }

    @Override
    public String getMimeType(String s) {
        return defaultContext.getMimeType(s);
    }
}
