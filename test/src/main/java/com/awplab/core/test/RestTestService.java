package com.awplab.core.test;

import com.awplab.core.rest.service.RestService;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by andyphillips404 on 12/19/15.
 */
@Provides
@Component(publicFactory = false, immediate = true)
@Instantiate
@Path("/")
public class RestTestService implements RestService {
    @Path("hello")
    @GET
    public String hello() {
        return "HELLO WORLD";
    }

    @Override
    public String getAlias() {
        return "/test1";
    }
}
