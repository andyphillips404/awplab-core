package com.awplab.core.rest.service.manager;

import com.awplab.core.rest.service.RestApplication;
import com.awplab.core.rest.service.RestManager;
import com.awplab.core.rest.service.RestService;
import com.awplab.core.rest.service.SimpleRestProvider;
import com.awplab.core.rest.service.events.RestEventData;
import com.awplab.core.rest.service.events.RestEventTopics;
import org.apache.felix.ipojo.annotations.*;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by andyphillips404 on 2/22/15.
 */
@Component(immediate = true, publicFactory=false, managedservice = RestManagerProvider.CONFIG_MANAGED_SERVICE_NAME)
@Instantiate
@Provides(specifications = RestManager.class)
public class RestManagerProvider implements RestManager, BundleListener {

    private Set<RestService> restProviders = ConcurrentHashMap.newKeySet();

    private Map<String, ServletContainer> servletContainers = new ConcurrentHashMap<>();

    private Map<String, RestApplication> applications = new ConcurrentHashMap<>();

    @ServiceProperty(name = PROPERTY_HOLD_OFF_TIME_SECONDS, value = "10")
    private int holdOffTimeSeconds;

    @Requires
    HttpService httpService;

    @Context
    private BundleContext bundleContext;

    private Logger logger = LoggerFactory.getLogger(RestManagerProvider.class);

    @Validate
    private void start() {
        bundleContext.addBundleListener(this);
        logger.info("Rest Manager Started");

        RestEventTopics.postEvent(RestEventTopics.MANAGER_STARTED);
    }

    @Invalidate
    public void stop() {
        bundleContext.removeBundleListener(this);

        for (String alias : servletContainers.keySet()) {
            httpService.unregister(alias);
        }

        restProviders.clear();
        servletContainers.clear();
        applications.clear();

        logger.info("Rest Manager Shut Down");

        RestEventTopics.postEvent(RestEventTopics.MANAGER_STOPPED);

    }


    @Override
    public void bundleChanged(BundleEvent bundleEvent) {
        if (bundleEvent.getType() == BundleEvent.STOPPED) {
            for (RestService restProvider : restProviders) {
                if (FrameworkUtil.getBundle(restProvider.getClass()).equals(bundleEvent.getBundle())) {
                    unregisterProvider(restProvider);
                }

                // TODO:  Check for registered classes and singletons?
            }
        }
    }

    private Set<String> dirtyAliases = Collections.synchronizedSet(new HashSet<>());

    private Timer dirtyTimer = null;
    private final Object dirtyTimerLock = new Object();

    private class DirtyAliasesTask extends TimerTask {
        @Override
        public void run() {
            synchronized (dirtyTimerLock) {



                for (String alias : dirtyAliases) {
                    logger.info("Creating / Updating service containers and servlets for alias " + alias);
                    try {

                        String eventTopic = RestEventTopics.ALIAS_STARTED;

                        ServletContainer container = servletContainers.get(alias);
                        if (container != null) {
                            httpService.unregister(alias);
                            servletContainers.remove(alias);
                            applications.remove(alias);
                            eventTopic = RestEventTopics.ALIAS_RESTARTED;

                        }

                        Set<RestService> providers = collectRestProviders(alias);
                        RestApplication restApplication = null;
                        if (providers.size() > 0) {
                            providers.addAll(collectRestProviders(RestManager.GLOBAL_ALIAS));
                            restApplication = new RestApplication(alias, providers);
                            applications.put(alias, restApplication);
                            container = new ServletContainer( ResourceConfig.forApplication(restApplication));
                            //container = new ServletContainer( restApplication);

                            servletContainers.put(alias, container);

                            httpService.registerServlet(alias, container, null, null);

                        }
                        else {
                            eventTopic = RestEventTopics.ALIAS_STOPPED;
                        }

                        RestEventTopics.postEvent(alias, eventTopic, (restApplication != null ? Collections.singletonMap(RestEventData.APPLICATION, restApplication) : Collections.emptyMap()));

                    } catch (Exception ex) {
                        logger.error("Exception attempting to reload rest services for alias " + alias, ex);
                    }


                }

                dirtyAliases.clear();

            }
        }
    }

    private Set<RestService> collectRestProviders(String alias) {
        HashSet<RestService> returnSet = new HashSet<>();
        for (RestService restProvider : restProviders) {
            if (restProvider.getAlias().equals(alias)) returnSet.add(restProvider);
        }
        return returnSet;
    }


    private void resetDirtyTimer() {
        if (dirtyTimer != null) {
            dirtyTimer.cancel();
        }
        dirtyTimer = new Timer();
        dirtyTimer.schedule(new DirtyAliasesTask(), holdOffTimeSeconds * 1000);
        logger.info("Change detected, holding off refresh for " + holdOffTimeSeconds  + " seconds for aliases: " + Arrays.toString(dirtyAliases.toArray()));
    }


    private void updateContainerServlet(String alias)  {
        synchronized (dirtyTimerLock) {
            if (alias.equals(RestManager.GLOBAL_ALIAS)) {
                for (String dirtyAlias : servletContainers.keySet()) {
                    dirtyAliases.add(dirtyAlias);
                }

            }
            else {
                dirtyAliases.add(alias);
            }
            if (dirtyAliases.size() > 0) resetDirtyTimer();
        }
    }



    @Override
    public synchronized void registerProvider(RestService restProvider) {
        String alias = restProvider.getAlias();
        if (alias.equals(RestManager.GLOBAL_ALIAS) || alias.equals("/") || alias.startsWith("/") && !alias.endsWith("/")) {
            restProviders.add(restProvider);

            updateContainerServlet(alias);

            RestEventTopics.postEvent(alias, RestEventTopics.PROVIDER_REGISTERED, Collections.singletonMap(RestEventData.PROVIDER, restProvider));
        }
        else {
            throw new IllegalArgumentException("Invalid alias.  Must start with a / and not end with a /");
        }
    }

    @Override
    public synchronized void unregisterProvider(RestService restProvider) {
        String alias = restProvider.getAlias();

        restProviders.remove(restProvider);

        updateContainerServlet(alias);

        RestEventTopics.postEvent(alias, RestEventTopics.PROVIDER_UNREGISTERED, Collections.singletonMap(RestEventData.PROVIDER, restProvider));
    }


    @Override
    public void registerClass(String alias, Class<?> clazz) {
        registerProvider(new SimpleRestProvider(alias, clazz));
    }

    @Override
    public void registerSingleton(String alias, Object singleton) {
        registerProvider(new SimpleRestProvider(alias, singleton));

    }

    @Override
    public void unregisterClass(String alias, Class<?> clazz) {
        unregisterProvider(new SimpleRestProvider(alias, clazz));

    }

    @Override
    public void unregisterSingleton(String alias, Object singleton) {
        unregisterProvider(new SimpleRestProvider(alias, singleton));
    }

    @Override
    public synchronized RestApplication getApplication(String alias) {
        return applications.get(alias);
    }

    private String logString(RestService provider) {
        return "RestService provider in alias " + provider.getAlias() + ", provider class: " + provider.getClass().getName();
    }

    @Bind(aggregate=true, optional=true)
    private void bindProvider(RestService provider) {

        try {
            registerProvider(provider);

            logger.info("Successfully added " + logString(provider));

        } catch (Exception ex) {
            logger.error("Exception attempting to add " + logString(provider), ex);
        }

    }

    @Unbind
    private void unbindProvider(RestService provider) {

        try {
            String alias = provider.getAlias();

            restProviders.remove(alias);

            updateContainerServlet(alias);

            logger.info("Removed " + logString(provider));

        } catch (Exception ex) {
            logger.error("Unhandled exception attemmpting to remove " + logString(provider), ex);
        }

    }

    @Override
    public synchronized Set<String> getAliases() {
        return servletContainers.keySet();
    }


    @Override
    public synchronized void reloadAlias(String alias) {
        updateContainerServlet(alias);

    }

    @Override
    public Set<RestService> getProviders() {
        return Collections.unmodifiableSet(restProviders);
    }
}
