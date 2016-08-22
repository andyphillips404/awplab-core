package com.awplab.core.rest.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.base.ProviderBase;

/**
 * Created by andyphillips404 on 12/20/15.
 */
public interface JacksonJaxrsService {
    Class<ProviderBase> getProviderClass() throws ClassNotFoundException;
    Class<ObjectMapper> getMapperClass() throws ClassNotFoundException;
}
