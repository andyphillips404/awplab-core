package com.awplab.core.rest.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import java.net.URL;
import java.util.*;

/**
 * Created by andyphillips404 on 12/19/15.
 */
public interface JacksonManagerService {

    void registerJacksonJaxrsProvider(JacksonJsonProvider)


    void registerModulesWithObjectMapper(ObjectMapper objectMapper);

    Set<JacksonModulesService> getModulesProviders();

    Set<JacksonJaxrsService> getJaxrsProviders();

    void updateChange();



}
