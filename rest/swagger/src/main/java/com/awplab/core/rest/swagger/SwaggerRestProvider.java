package com.awplab.core.rest.swagger;

import com.awplab.core.rest.service.RestManager;
import io.swagger.models.*;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;

import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by andyphillips404 on 1/7/16.
 */
@Component(name = SwaggerRestProvider.CONFIG_FACTORY_NAME)
@Provides
@Path("/")
public class SwaggerRestProvider extends BaseSwaggerRestProvider {

    public final static String CONFIG_FACTORY_NAME = "com.awplab.core.rest.swagger";

    public final static String PROPERTY_ALIAS = "com.awplab.core.rest.swagger.alias";

    public static final String PROPERTY_CORS = "com.awplab.core.rest.swagger.cors";

    public final static String PROPERTY_HOST = "com.awplab.core.rest.swagger.host";
    public final static String PROPERTY_BASE_PATH = "com.awplab.core.rest.swagger.basePath";
    public final static String PROPERTY_SCHEMES = "com.awplab.core.rest.swagger.schemes";

    public final static String PROPERTY_INFO_DESCRIPTION = "com.awplab.core.rest.swagger.info.description";
    public final static String PROPERTY_INFO_VERSION = "com.awplab.core.rest.swagger.info.version";
    public final static String PROPERTY_INFO_TITLE = "com.awplab.core.rest.swagger.info.title";
    public final static String PROPERTY_INFO_TERMS_OF_SERVICE = "com.awplab.core.rest.swagger.info.termsOfService";

    public final static String PROPERTY_INFO_CONTACT_NAME = "com.awplab.core.rest.swagger.info.contact.name";
    public final static String PROPERTY_INFO_CONTACT_URL = "com.awplab.core.rest.swagger.info.contact.url";
    public final static String PROPERTY_INFO_CONTACT_EMAIL = "com.awplab.core.rest.swagger.info.contact.email";

    public final static String PROPERTY_INFO_LICENSE_NAME = "com.awplab.core.rest.swagger.info.license.name";
    public final static String PROPERTY_INFO_LICENSE_URL = "com.awplab.core.rest.swagger.info.license.url";


    @Property(name = PROPERTY_ALIAS)
    @Override
    protected void setAlias(String alias) {
        super.setAlias(alias);
    }

    @Property(name = PROPERTY_HOST)
    private String host;
    @Property(name = PROPERTY_BASE_PATH)
    private String basePath;
    @Property(name = PROPERTY_SCHEMES)
    private String[] schemes;

    @Property(name = PROPERTY_INFO_DESCRIPTION)
    private String infoDescription;
    @Property(name = PROPERTY_INFO_VERSION)
    private String infoVersion;
    @Property(name = PROPERTY_INFO_TITLE)
    private String infoTitle;
    @Property(name = PROPERTY_INFO_TERMS_OF_SERVICE)
    private String infoTermsOfService;

    @Property(name = PROPERTY_INFO_CONTACT_NAME)
    private String infoContactName;
    @Property(name = PROPERTY_INFO_CONTACT_URL)
    private String infoContactUrl;
    @Property(name = PROPERTY_INFO_CONTACT_EMAIL)
    private String infoContactEmail;

    @Property(name = PROPERTY_INFO_LICENSE_NAME)
    private String infoLicenseName;
    @Property(name = PROPERTY_INFO_LICENSE_URL)
    private String infoLicenseUrl;


    private boolean enableCors = true;

    @Property(name = PROPERTY_CORS)
    private void setEnableCors(boolean coors) {
        enableCors = false;
        RestManager.getProvider().reloadAliases();
    }

    @Override
    public void processSwagger(Swagger swagger) {
        if (host != null) swagger.setHost(host);
        if (basePath != null) swagger.setBasePath(basePath);

        if (schemes != null && schemes.length > 0) {
            List<Scheme> newSchemes = new ArrayList<>();
            for (String scheme : schemes) {
                newSchemes.add(Scheme.forValue(scheme));
            }
            swagger.setSchemes(newSchemes);
        }


        if (swagger.getInfo() == null) swagger.setInfo(new Info());
        if (infoDescription != null) swagger.getInfo().setDescription(infoDescription);
        if (infoVersion != null) swagger.getInfo().setVersion(infoVersion);
        if (infoTitle != null) swagger.getInfo().setTitle(infoTitle);
        if (infoTermsOfService != null) swagger.getInfo().setTermsOfService(infoTermsOfService);
        if (swagger.getInfo().getContact() == null) swagger.getInfo().setContact(new Contact());
        if (infoContactName != null) swagger.getInfo().getContact().setName(infoContactName);
        if (infoContactUrl != null) swagger.getInfo().getContact().setUrl(infoContactUrl);
        if (infoContactEmail != null) swagger.getInfo().getContact().setEmail(infoContactEmail);
        if (swagger.getInfo().getLicense() == null) swagger.getInfo().setLicense(new License());
        if (infoLicenseName != null) swagger.getInfo().getLicense().setName(infoLicenseName);
        if (infoLicenseUrl != null) swagger.getInfo().getLicense().setUrl(infoLicenseUrl);


    }

    @Override
    public Set<Class<?>> getClasses(String alias) {
        HashSet<Class<?>> classes = new HashSet<>(super.getClasses(alias));
        if (enableCors) classes.add(CorsResponseFilter.class);
        return classes;
    }
}
