package com.awplab.core.vaadin.service;

import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;

import java.util.Collection;

/**
 * Created by andyphillips404 on 8/11/16.
 */
public abstract class VaadinUIProvider extends UIProvider {



    public void sessionDestroy(SessionDestroyEvent sessionDestroyEvent) {

    }

    public void sessionInit(SessionInitEvent sessionInitEvent) throws ServiceException {

    }

    public void shutdown() {

    }




}
