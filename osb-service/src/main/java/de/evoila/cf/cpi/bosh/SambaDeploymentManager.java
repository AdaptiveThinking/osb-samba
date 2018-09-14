package de.evoila.cf.cpi.bosh;

import de.evoila.cf.broker.bean.BoshProperties;
import de.evoila.cf.broker.model.Plan;
import de.evoila.cf.broker.model.ServiceInstance;
import de.evoila.cf.broker.util.RandomString;
import de.evoila.cf.cpi.bosh.deployment.DeploymentManager;
import de.evoila.cf.cpi.bosh.deployment.manifest.Manifest;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jannikheyl on 19.01.18.
 */
public class SambaDeploymentManager extends DeploymentManager {

    private RandomString randomStringUser = new RandomString(10);
    private RandomString randomStringPassword = new RandomString(15);
    private RandomString randomStringUsergroup = new RandomString(15);

    public SambaDeploymentManager(BoshProperties properties, Environment environment) {
       super (properties, environment);
    }

    @Override
    protected void replaceParameters(ServiceInstance serviceInstance, Manifest manifest, Plan plan, Map<String, Object> customParameters) {
        HashMap<String, Object> properties = new HashMap<>();
        properties.putAll(plan.getMetadata().getCustomParameters());

        if (customParameters != null && !customParameters.isEmpty())
            properties.putAll(customParameters);

        HashMap<String, Object> manifestProperties = (HashMap<String, Object>) manifest
                .getInstanceGroups()
                .stream()
                .filter(i -> {
                    if (i.getName().equals("samba"))
                        return true;
                    return false;
                }).findFirst().get().getProperties();

        HashMap<String, Object> smb = (HashMap<String, Object>) manifestProperties.get("smb");
        HashMap<String, Object> rest = (HashMap<String, Object>) manifestProperties.get("rest");

        String username = randomStringUser.nextString();
        String password = randomStringPassword.nextString();
        String usergroup = randomStringUsergroup.nextString();

        rest.put("user", username);
        rest.put("password", password);
        smb.put("usergroup", usergroup);

        serviceInstance.setUsername(username);
        serviceInstance.setPassword(password);
        serviceInstance.setUsergroup(usergroup);

        this.updateInstanceGroupConfiguration(manifest, plan);
    }
}
