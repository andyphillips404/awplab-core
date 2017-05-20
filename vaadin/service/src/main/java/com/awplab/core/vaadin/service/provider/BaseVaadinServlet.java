package com.awplab.core.vaadin.service.provider;

import com.awplab.core.vaadin.service.VaadinProvider;
import com.vaadin.server.*;

import javax.servlet.ServletException;

/**
 * Created by andyphillips404 on 5/20/17.
 */
public class BaseVaadinServlet extends VaadinServlet implements SessionInitListener, SessionDestroyListener {

    private final VaadinProvider vaadinProvider;

    private final VaadinUIProvider uiProvider;

    public BaseVaadinServlet(VaadinProvider vaadinProvider) {
        this.vaadinProvider = vaadinProvider;
        this.uiProvider = new VaadinUIProvider(vaadinProvider, this);
    }


    public VaadinProvider getVaadinProvider() {
        return vaadinProvider;
    }

    public VaadinUIProvider getUiProvider() {
        return uiProvider;
    }

    @Override
    public void sessionDestroy(SessionDestroyEvent sessionDestroyEvent) {
        //uiProvider.sessionDestroy(sessionDestroyEvent);
    }

    @Override
    public void sessionInit(SessionInitEvent sessionInitEvent) throws ServiceException {
        sessionInitEvent.getSession().addUIProvider(uiProvider);
        //uiProvider.sessionInit(sessionInitEvent);
    }

    @Override
    protected void servletInitialized() throws ServletException {
        super.servletInitialized();

        getService().addSessionInitListener(this);
        getService().addSessionDestroyListener(this);



    }
}
