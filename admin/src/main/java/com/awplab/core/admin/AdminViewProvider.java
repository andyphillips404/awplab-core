package com.awplab.core.admin;

import com.awplab.core.admin.provider.AdminUI;
import com.awplab.core.vaadin.service.VaadinProvider;
import com.vaadin.navigator.View;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

import javax.security.auth.Subject;
import java.util.Optional;

/**
 * Created by andyphillips404 on 8/10/16.
 */
public interface AdminViewProvider {

    AdminView createView(Subject subject);

}
