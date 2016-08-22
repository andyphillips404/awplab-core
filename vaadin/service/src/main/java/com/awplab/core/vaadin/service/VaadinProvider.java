package com.awplab.core.vaadin.service;


import com.vaadin.server.*;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.UI;

import javax.security.auth.Subject;
import java.util.Optional;

/**
 * Created by andyphillips404 on 8/11/16.
 */
public interface VaadinProvider  {


    String getPath();

    default boolean productionMode() {
        return false;
    }

    default Optional<Integer> heartbeatInterval() {
        return Optional.empty();
    }

    default Optional<Boolean> closeIdleSessions() { return Optional.empty(); }

    default void servletInitialized(VaadinServlet vaadinServlet) {

    }

    default Optional<PushMode> pushMode() {
        return Optional.empty();
    }

    VaadinUIProvider createUIProvider();


    static void doAccess(UI ui, Runnable runnable) {
        if (ui.isAttached()) {
            ui.access(runnable);
        } else {
            runnable.run();
        }
    }



}
