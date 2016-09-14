package com.awplab.core.admin;

import com.awplab.core.admin.provider.AdminUI;

import javax.security.auth.Subject;

/**
 * Created by andyphillips404 on 8/31/16.
 */
public interface AdminProvider {

    AdminViewProvider createViewProvider(AdminUI adminUI);


}
