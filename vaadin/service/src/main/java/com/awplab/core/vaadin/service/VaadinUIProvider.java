package com.awplab.core.vaadin.service;

import com.vaadin.server.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by andyphillips404 on 8/11/16.
 */
public abstract class VaadinUIProvider extends UIProvider {

    private static Set<VaadinSession> vaadinSessions = Collections.synchronizedSet(new HashSet<>());

    public void sessionDestroy(SessionDestroyEvent sessionDestroyEvent) {
        vaadinSessions.remove(sessionDestroyEvent.getSession());
    }

    public void sessionInit(SessionInitEvent sessionInitEvent) throws ServiceException {
        vaadinSessions.add(sessionInitEvent.getSession());
    }

    public void shutdown() {
        for (VaadinSession vaadinSession : vaadinSessions) {
            vaadinSession.close();
        }

    }




}
