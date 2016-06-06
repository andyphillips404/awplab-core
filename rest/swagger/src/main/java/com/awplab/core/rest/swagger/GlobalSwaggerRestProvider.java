package com.awplab.core.rest.swagger;

import com.awplab.core.rest.service.RestManager;
import com.awplab.core.rest.service.RestService;
import org.apache.felix.ipojo.annotations.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by andyphillips404 on 1/11/16.
 */
@Component(publicFactory = false, immediate = true, managedservice = GlobalSwaggerRestProvider.CONFIG_MANAGED_SERVICE)
@Provides
@Instantiate
public class GlobalSwaggerRestProvider implements RestService {

    public static final String CONFIG_MANAGED_SERVICE = "com.awplab.core.rest.swagger.global";

    public static final String PROPERTY_ENABLED = "com.awplab.core.rest.swagger.global.enabled";

    public static final String PROPERTY_CORS = "com.awplab.core.rest.swagger.global.cors";

    public static final String PROPERTY_SKIP_ALIASES = "com.awplab.core.rest.swagger.global.skipAliases";

    public static final String PROPERTY_ONLY_INCLUDE_ALIASES = "com.awplab.core.rest.swagger.global.onlyIncludeAliases";

    @ServiceController(specification = RestService.class, value = true)
    private boolean restServiceController;


    @Property(name = PROPERTY_ENABLED)
    private void setEnabled(boolean enabled) {
        restServiceController = enabled;
    }

    @Property(name = PROPERTY_SKIP_ALIASES)
    private String[] skipAliases;

    @Property(name = PROPERTY_ONLY_INCLUDE_ALIASES)
    private String[] onlyIncludeAliases;

    private boolean enableCors = true;

    @Property(name = PROPERTY_CORS)
    private void setEnableCors(boolean coors) {
        enableCors = coors;
        RestManager.getProvider().reloadAliases();
    }


    @Override
    public String getAlias() {
        return RestManager.GLOBAL_ALIAS;
    }

    @Override
    public Set<Class<?>> getClasses(String alias) {
        if (skipAliases != null && skipAliases.length > 0 && Arrays.asList(skipAliases).contains(alias)) return Collections.emptySet();

        if (onlyIncludeAliases != null && onlyIncludeAliases.length > 0 && !Arrays.asList(onlyIncludeAliases).contains(alias)) return Collections.emptySet();

        HashSet<Class<?>> classes = new HashSet<>();
        classes.add(SwaggerRestProvider.class);
        if (enableCors) classes.add(CorsResponseFilter.class);
        return classes;
    }


}
