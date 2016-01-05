package com.awplab.core.rest.service.security;

import javax.annotation.Priority;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.util.Base64;

/**
 * Created by andyphillips404 on 3/26/15.
 */
@Priority(Priorities.AUTHENTICATION) // authorization filter - should go after any authentication filters
public class BasicAuthKarafSecurityRequestFilter implements ContainerRequestFilter {

    private static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String AUTHENTICATION_SCHEME_BASIC = "Basic";

    private final boolean requireSecure;
    private final String karafRealm;
    private final String[] limitToRoles;
    private final String[] limitToGroups;
    private final String httpRealm;

    public BasicAuthKarafSecurityRequestFilter(String[] limitToGroups, String[] limitToRoles, String karafRealm, boolean requireSecure, String httpRealm) {
        this.limitToGroups = limitToGroups;
        this.limitToRoles = limitToRoles;
        this.karafRealm = karafRealm;
        this.requireSecure = requireSecure;
        this.httpRealm = httpRealm;
    }


    private boolean checkSecurityContext(SecurityContext context) {
        if (context instanceof KarafSecurityContext) {

            if (limitToRoles != null && limitToRoles.length > 0) {
                boolean found = false;
                for (String role : limitToRoles) {
                    if (context.isUserInRole(role)) {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
            if (limitToGroups != null && limitToGroups.length > 0) {
                boolean found = false;
                for (String group : limitToGroups) {
                    if (((KarafSecurityContext) context).isUserInGroup(group)) {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }

            return true;
        }

        return false;
    }

    private void abortRequestContext(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response
                .status(Response.Status.UNAUTHORIZED)
                .entity("User is not Authorized!")
                .header(HEADER_WWW_AUTHENTICATE, AUTHENTICATION_SCHEME_BASIC + " realm=\"" + this.httpRealm + "\"" )
                .build());
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        boolean isSecure = requestContext.getSecurityContext().isSecure();
        if (requestContext.getHeaders().containsKey("X-Forwarded-Proto") &&
                (requestContext.getHeaders().get("X-Forwarded-Proto").contains("https") || requestContext.getHeaders().get("X-Forwarded-Proto").contains("HTTPS"))) {
            isSecure = true;
        }
        if (requireSecure && !isSecure) {
            requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Secure connection is required!")
                    .build());

            return;
        }

        if (karafRealm != null) {

            if (requestContext.getSecurityContext() != null &&
                    requestContext.getSecurityContext() instanceof KarafSecurityContext &&
                    checkSecurityContext(requestContext.getSecurityContext()))
                return;

            String authHeader = requestContext.getHeaderString(HEADER_AUTHORIZATION);
            if (authHeader != null && authHeader.length() > 0) {

                // Get the authType (Basic, Digest) and authInfo (user/password)
                // from
                // the header
                authHeader = authHeader.trim();
                int blank = authHeader.indexOf(' ');
                if (blank > 0) {
                    String authType = authHeader.substring(0, blank);
                    String authInfo = authHeader.substring(blank).trim();

                    // Check whether authorization type matches
                    if (authType.equalsIgnoreCase(AUTHENTICATION_SCHEME_BASIC)) {

                        final String userNamePassword = new String(Base64.getDecoder().decode(authInfo));
                        final String userNamePasswordRegex = "^(.*?)(\\:)(.*?)$";

                        if (userNamePassword.matches(userNamePasswordRegex)) {


                            try {
                                Subject subject = new Subject();

                                final LoginContext loginContext = new LoginContext(karafRealm, subject, new CallbackHandler() {
                                    @Override
                                    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                                        for (Callback callback : callbacks) {
                                            if (callback instanceof NameCallback) {
                                                ((NameCallback) callback).setName(userNamePassword.replaceAll(userNamePasswordRegex, "$1"));
                                            }
                                            if (callback instanceof PasswordCallback) {
                                                ((PasswordCallback) callback).setPassword(userNamePassword.replaceAll(userNamePasswordRegex, "$3").toCharArray());
                                            }
                                        }
                                    }
                                });
                                loginContext.login();
                                requestContext.setSecurityContext(new KarafSecurityContext(loginContext.getSubject(), requestContext.getSecurityContext().isSecure(), SecurityContext.BASIC_AUTH));

                                if (checkSecurityContext(requestContext.getSecurityContext())) return;

                                //return;
                            } catch (Exception ignored) {
                                //System.out.print(ignored);
                            }
                        }
                    }
                }
            }
        }
        abortRequestContext(requestContext);
    }
}