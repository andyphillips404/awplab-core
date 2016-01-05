package com.awplab.core.ipojo.manager;

import com.awplab.core.ipojo.IPojoManagerService;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.HandlerFactory;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.architecture.InstanceDescription;

import java.util.Arrays;

/**
 * Created by andyphillips404 on 12/21/15.
 */
@Component(publicFactory = false, immediate = true)
@Provides
@Instantiate
public class IPojoManagerProvider implements IPojoManagerService {

    @Requires(optional = true)
    private Architecture[] architectures;

    @Requires(optional = true)
    private HandlerFactory[] handlerFactories;

    @Requires(optional = true)
    private Factory[] factories;

    @Override
    public Architecture[] getArchitectures() {
        return architectures;
    }

    @Override
    public HandlerFactory[] getHandlerFactories() {
        return handlerFactories;
    }

    @Override
    public Factory[] getFactories() {
        return factories;
    }

    @Override
    public InstanceDescription getInstanceDescription(Object instance) {
        for (Architecture architecture : architectures) {
            if (architecture.getInstanceDescription().getState() == ComponentInstance.VALID) {
                if (architecture.getInstanceDescription().getInstance() instanceof InstanceManager) {
                    InstanceManager instanceManager = (InstanceManager) architecture.getInstanceDescription().getInstance();
                    if (Arrays.asList(instanceManager.getPojoObjects()).contains(instance)) {
                        return architecture.getInstanceDescription();
                    }
                }
            }
        }

        return null;
    }
}
