package com.awplab.core.rest.swagger;

import com.awplab.core.rest.service.AbstractRestProvider;
import com.awplab.core.rest.service.RestManager;
import io.swagger.annotations.ApiOperation;
import io.swagger.jaxrs.Reader;
import io.swagger.models.Swagger;
import io.swagger.util.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.HashSet;

/**
 * Created by andyphillips404 on 1/7/16.
 */
@Path("/")
public class BaseSwaggerRestProvider extends AbstractRestProvider {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());


    @Context
    private Application application;

    protected void processSwagger(Swagger swagger) {

    }
    protected HashSet<Class<?>> getSwaggerClasses() {
        HashSet<Class<?>> classes = new HashSet<>();

        RestManager restManager = RestManager.getProvider();
        if (restManager == null) {
            logger.error("Unable to get RestManager provider");
            return null;
        }

        for (Class<?> clazz : application.getClasses()) {
            classes.add(clazz);
        }

        for (Object singleton : application.getSingletons()) {
            classes.add(singleton.getClass());
        }

        return classes;
    }

    protected Swagger getSwaggerWithClasses() {

        try {
            Reader reader = new Reader(null);
            Swagger swagger = reader.read(getSwaggerClasses());

            processSwagger(swagger);

            return swagger;
        }
        catch (Exception ex) {
            logger.error("Uncaught exception attempting to generate Swagger", ex);
            return null;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/swagger.json")
    @ApiOperation(value = "The swagger definition in JSON", hidden = true)
    public Response getListingJson() {
        return Response.ok().entity(getSwaggerWithClasses()).build();
    }

    @GET
    @Produces("application/yaml")
    @Path("/swagger.yaml")
    @ApiOperation(value = "The swagger definition in YAML", hidden = true)
    public Response getListingYaml(
            @Context Application app,
            //@Context ServletConfig sc,
            @Context HttpHeaders headers,
            @Context UriInfo uriInfo) {
        try {
            String yaml = Yaml.mapper().writeValueAsString(getSwaggerWithClasses());
            String[] parts = yaml.split("\n");
            StringBuilder b = new StringBuilder();
            for (String part : parts) {
                int pos = part.indexOf("!<");
                int endPos = part.indexOf(">");
                b.append(part);
                b.append("\n");
            }
            return Response.ok().entity(b.toString()).type("application/yaml").build();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return Response.status(404).build();
    }


}
