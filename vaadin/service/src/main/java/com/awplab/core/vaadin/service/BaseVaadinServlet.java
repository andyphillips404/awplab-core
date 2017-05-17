package com.awplab.core.vaadin.service;

import com.awplab.core.vaadin.service.VaadinProvider;
import com.awplab.core.vaadin.service.VaadinUIProvider;
import com.vaadin.server.*;

import javax.servlet.ServletException;

/**
 * Created by andyphillips404 on 8/11/16.
 */
public class BaseVaadinServlet extends VaadinServlet implements SessionInitListener, SessionDestroyListener {

    private final VaadinProvider vaadinProvider;

    private final VaadinUIProvider uiProvider;

    public BaseVaadinServlet(VaadinProvider vaadinProvider) {
        this.vaadinProvider = vaadinProvider;
        this.uiProvider = vaadinProvider.createUIProvider();
    }


    public VaadinProvider getVaadinProvider() {
        return vaadinProvider;
    }

    public VaadinUIProvider getUiProvider() {
        return uiProvider;
    }

    @Override
    public void sessionDestroy(SessionDestroyEvent sessionDestroyEvent) {
        uiProvider.sessionDestroy(sessionDestroyEvent);
    }

    @Override
    public void sessionInit(SessionInitEvent sessionInitEvent) throws ServiceException {
        sessionInitEvent.getSession().addUIProvider(uiProvider);
        uiProvider.sessionInit(sessionInitEvent);
    }

    @Override
    protected void servletInitialized() throws ServletException {
        super.servletInitialized();

        getService().addSessionInitListener(this);
        getService().addSessionDestroyListener(this);



    }


}
