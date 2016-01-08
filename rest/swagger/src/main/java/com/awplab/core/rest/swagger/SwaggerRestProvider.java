package com.awplab.core.rest.swagger;

import com.awplab.core.rest.service.AbstractRestProvider;
import com.awplab.core.rest.service.RestService;
import io.swagger.annotations.*;
import io.swagger.models.*;
import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.In;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.Parameter;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.framework.FrameworkUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by andyphillips404 on 1/7/16.
 */
@Component(name = SwaggerRestProvider.CONFIG_FACTORY_NAME)
@Provides
public class SwaggerRestProvider extends AbstractSwaggerRestProvider {

    public final static String CONFIG_FACTORY_NAME = "com.awplab.core.rest.swagger";

    public final static String PROPERTY_ALIAS = "com.awplab.core.rest.swagger.alias";

    public final static String PROPERTY_HOST = "com.awplab.core.rest.swagger.host";
    public final static String PROPERTY_BASE_PATH = "com.awplab.core.rest.swagger.basePath";
    public final static String PROPERTY_SCHEMES = "com.awplab.core.rest.swagger.schemes";

    /*
    public final static String PROPERTY_INFO_DESCRIPTION = "com.awplab.core.rest.swagger.info.description";
    public final static String PROPERTY_INFO_VERSION = "com.awplab.core.rest.swagger.info.version";
    public final static String PROPERTY_INFO_TITLE = "com.awplab.core.rest.swagger.info.title";
    public final static String PROPERTY_INFO_TERMS_OF_SERVICE = "com.awplab.core.rest.swagger.info.termsOfService";

    public final static String PROPERTY_INFO_CONTACT_NAME = "com.awplab.core.rest.swagger.info.contact.name";
    public final static String PROPERTY_INFO_CONTACT_URL = "com.awplab.core.rest.swagger.info.contact.url";
    public final static String PROPERTY_INFO_CONTACT_EMAIL = "com.awplab.core.rest.swagger.info.contact.email";

    public final static String PROPERTY_INFO_LICENSE_NAME = "com.awplab.core.rest.swagger.info.license.name";
    public final static String PROPERTY_INFO_LICENSE_URL = "com.awplab.core.rest.swagger.info.license.url";
    */

    @Property(name = PROPERTY_ALIAS, mandatory = true)
    @Override
    protected void setAlias(String alias) {
        super.setAlias(alias);
    }

    @Property(name = PROPERTY_HOST, mandatory = true)
    private String host;
    @Property(name = PROPERTY_BASE_PATH, mandatory = true)
    private String basePath;
    @Property(name = PROPERTY_SCHEMES, mandatory = true)
    private String[] schemes;

    /*
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
    */

    @Override
    public void processSwagger(Swagger swagger) {
        swagger.setHost(host);
        swagger.setBasePath(basePath);

        if (schemes != null && schemes.length > 0) {
            List<Scheme> newSchemes = new ArrayList<>();
            for (String scheme : schemes) {
                newSchemes.add(Scheme.forValue(scheme));
            }
            swagger.setSchemes(newSchemes);
        }

    }



}
