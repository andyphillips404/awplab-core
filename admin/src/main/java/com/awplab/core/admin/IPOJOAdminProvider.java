package com.awplab.core.admin;

import com.vaadin.server.ClientConnector;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.InstanceManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by andyphillips404 on 8/31/16.
 */
public abstract class IPOJOAdminProvider<T extends AdminViewProvider> implements AdminProvider {

    public abstract Class<T> getAdminViewProviderClass();

    public abstract void viewProviderCreated(T viewProvider);

    @Override
    public AdminViewProvider createViewProvider(AdminUI adminUI) {
        Logger logger = LoggerFactory.getLogger(IPOJOAdminProvider.class);

        try {
            BundleContext bundleContext = FrameworkUtil.getBundle(IPOJOAdminProvider.class).getBundleContext();
            ServiceReference[] refs = bundleContext.getServiceReferences(Factory.class.getName(), "(|(instance.name=" + getAdminViewProviderClass().getName() +")(service.pid=" + getAdminViewProviderClass().getName() +"))");
            if (refs != null && refs.length > 0) {
                Factory factory = (Factory) bundleContext.getService(refs[0]);
                ComponentInstance componentInstance = factory.createComponentInstance(null);
                adminUI.addDetachListener(new InstanceManagerUIDetachListener(((InstanceManager) componentInstance)));
                T viewProvider = (T) ((InstanceManager) componentInstance).getPojoObject();
                viewProviderCreated(viewProvider);
                return viewProvider;
            }
            else {
                logger.error("Unable to find ipojo factory for class: " + getAdminViewProviderClass().getName());
            }

        }
        catch (Exception ex) {
            logger.error("Exception creating instance of class: " + getAdminViewProviderClass().getName(), ex);
        }
        return null;
    }

    private class InstanceManagerUIDetachListener implements ClientConnector.DetachListener {
        private final InstanceManager instanceManager;

        public InstanceManagerUIDetachListener(InstanceManager instanceManager) {
            this.instanceManager = instanceManager;
        }

        @Override
        public void detach(ClientConnector.DetachEvent event) {
            instanceManager.dispose();
        }
    }

}
