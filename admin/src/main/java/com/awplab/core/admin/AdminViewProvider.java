package com.awplab.core.admin;

import javax.security.auth.Subject;

/**
 * Created by andyphillips404 on 8/10/16.
 */
public interface AdminViewProvider {

    AdminView createView(Subject subject);

}
