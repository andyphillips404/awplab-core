package com.awplab.core.vaadin.service.provider;

import com.awplab.core.vaadin.service.BasicAuthRequired;
import com.awplab.core.vaadin.service.VaadinManager;
import com.awplab.core.vaadin.service.VaadinProvider;
import com.vaadin.server.VaadinServlet;
import org.apache.felix.ipojo.annotations.*;
import org.ops4j.pax.web.service.WebContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.util.*;

/**
 * Created by andyphillips404 on 8/11/16.
 */
@Component(publicFactory = false, immediate = true)
@Instantiate
@Provides(specifications = {VaadinManager.class})
public class VaadinManagerProvider implements VaadinManager {

    Set<BaseVaadinServlet> servlets = Collections.synchronizedSet(new HashSet<>());

    @Requires
    WebContainer webContainer;

    private VaadinServlet bootstrapVaadinServlet;

    @Validate
    private void start() {
        bootstrapVaadinServlet = new VaadinServlet();
        try {
            webContainer.registerServlet(bootstrapVaadinServlet, new String[]{"/VAADIN/*"}, null, null);
        }
        catch (ServletException ex) {
            logger.error("Exception registering boostrapVaadinServlet!", ex);
        }
        logger.info("Exception registering boostrapVaadinServlet!");
    }

    @Invalidate
    private void stop() {
        if (bootstrapVaadinServlet != null) webContainer.unregisterServlet(bootstrapVaadinServlet);
        new HashSet<BaseVaadinServlet>(servlets).forEach(baseVaadinServlet -> {
            unregisterProvider(baseVaadinServlet.getVaadinProvider());
        });

    }

    private Logger logger = LoggerFactory.getLogger(VaadinManagerProvider.class);

    @Bind(optional = true, aggregate = true)
    public void bindProvider(VaadinProvider vaadinProvider) {
        try {
            registerProvider(vaadinProvider);
        }
        catch (Exception ex) {
            logger.error("Exception registering vaadin provider!", ex);
        }
    }

    @Unbind(optional = true, aggregate = true)
    public void unbindProvider(VaadinProvider vaadinProvider) {

        try {
            unregisterProvider(vaadinProvider);
        }
        catch (Exception ex) {
            logger.error("Exception registering vaadin provider!", ex);
        }
    }

    @Override
    public Set<VaadinProvider> getProviders() {
        HashSet<VaadinProvider> providers = new HashSet<>();
        servlets.forEach(baseVaadinServlet -> {providers.add(baseVaadinServlet.getVaadinProvider());});
        return Collections.unmodifiableSet(providers);
    }

    @Override
    public Set<VaadinServlet> getServlets() {
        return Collections.unmodifiableSet(servlets);
    }


    private Optional<BaseVaadinServlet> findServlet(VaadinProvider vaadinProvider) {
        for (BaseVaadinServlet baseVaadinServlet : servlets) {
            if (baseVaadinServlet.getVaadinProvider().equals(vaadinProvider)) {
                return Optional.of(baseVaadinServlet);
            }
        }

        return Optional.empty();
    }

    @Override
    public void registerProvider(VaadinProvider vaadinProvider) throws ServletException {
        if (vaadinProvider.getPath() == null || !vaadinProvider.getPath().startsWith("/") || vaadinProvider.getPath().endsWith("/")) {
            throw new RuntimeException("Invalid path, path must not be null, start with a / and end with no /");
        }

        if (findServlet(vaadinProvider).isPresent()) {
            throw new RuntimeException("Vaadin provider is already registered.  Aborting!");
        }

        BaseVaadinServlet baseVaadinServlet = new BaseVaadinServlet(vaadinProvider);
        Dictionary<String, Object> initParams = new Hashtable<String, Object>();
        initParams.put("productionMode", vaadinProvider.productionMode());
        vaadinProvider.heartbeatInterval().ifPresent(integer -> {initParams.put("heartbeatInterval", integer); });
        vaadinProvider.closeIdleSessions().ifPresent(close -> {initParams.put("closeIdleSessions", close); });
        vaadinProvider.pushMode().ifPresent(pushMode -> {initParams.put("pushmode", pushMode.toString().toLowerCase());});
        webContainer.registerServlet(baseVaadinServlet, new String[] { vaadinProvider.getPath() + "/*"}, initParams, 0, true, (baseVaadinServlet instanceof BasicAuthRequired ? new BasicAuthHttpContext(webContainer.getDefaultSharedHttpContext(), (BasicAuthRequired)baseVaadinServlet) : webContainer.getDefaultSharedHttpContext()));
        servlets.add(baseVaadinServlet);
    }

    @Override
    public void unregisterProvider(VaadinProvider vaadinProvider) {
        BaseVaadinServlet vaadinServlet = findServlet(vaadinProvider).orElseThrow(() -> new RuntimeException("Vaadin provider is not registered.  Aborting!"));
        vaadinServlet.getUiProvider().shutdown();
        webContainer.unregisterServlet(vaadinServlet);
        servlets.remove(vaadinServlet);
    }

}
