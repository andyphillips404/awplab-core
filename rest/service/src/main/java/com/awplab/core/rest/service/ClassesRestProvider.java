package com.awplab.core.rest.service;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by andyphillips404 on 12/19/15.
 */
@Component(name = ClassesRestProvider.CONFIG_FACTORY_NAME, immediate = true)
@Provides(specifications = RestService.class)
public class ClassesRestProvider extends AbstractRestProvider {
    public final static String CONFIG_FACTORY_NAME = "com.awplab.core.rest.classes";

    public final static String PROPERTY_ALIAS = "com.awplab.core.rest.classes.alias";
    public final static String PROPERTY_NAMES = "com.awplab.core.rest.classes.names";

    @Requires
    RestManagerService restManagerService;

    @Override
    RestManagerService getRestManagerService() {
        return restManagerService;
    }

    @Property(name = PROPERTY_ALIAS)
    @Override
    protected void setAlias(String alias) {
        super.setAlias(alias);
    }

    private Set<Class<?>> classes;

    @Property(name = PROPERTY_NAMES)
    private void setClasses(String[] classes) {
        Logger logger = LoggerFactory.getLogger(ClassesRestProvider.class);


        HashSet<Class<?>> newClasses = new HashSet<>();
        for (String className : classes) {
            try {
                newClasses.add(Class.forName(className));
            }
            catch (Exception ex) {
                logger.error("Exception creating class named: " + className, ex);
            }
        }
        this.classes = newClasses;

        restManagerService.reloadAlias(getAlias());
    }






}
