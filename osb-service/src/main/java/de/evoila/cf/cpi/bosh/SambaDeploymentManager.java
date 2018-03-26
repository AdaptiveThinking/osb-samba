package de.evoila.cf.cpi.bosh;

import de.evoila.cf.broker.bean.BoshProperties;
import de.evoila.cf.broker.model.Plan;
import de.evoila.cf.broker.model.ServiceInstance;
import de.evoila.cf.broker.util.RandomString;
import de.evoila.cf.cpi.bosh.deployment.DeploymentManager;
import de.evoila.cf.cpi.bosh.deployment.manifest.Manifest;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jannikheyl on 19.01.18.
 */
@Service
public class SambaDeploymentManager extends DeploymentManager {

    private RandomString randomStringUser = new RandomString(10);
    private RandomString randomStringPassword = new RandomString(15);
    private RandomString randomStringUsergroup = new RandomString(15);

    public SambaDeploymentManager(BoshProperties properties) {
       super (properties);
    }

    @Override
    protected void replaceParameters(ServiceInstance serviceInstance, Manifest manifest, Plan plan, Map<String, String> customParameters) {
        HashMap<String, Object> properties = new HashMap<>();
        properties.putAll(plan.getMetadata().getCustomParameters());
        properties.putAll(customParameters);

        SecureRandom random = new SecureRandom();
        HashMap<String, Object> manProperties = new HashMap<String, Object >();

        HashMap<String, Object> rest = new HashMap<String, Object>();
        HashMap<String, Object> smb = new HashMap<String, Object>();

        String username = randomStringUser.nextString();
        String password = randomStringPassword.nextString();
        String usergroup = randomStringUsergroup.nextString();

        rest.put("user", username);
        rest.put("password", password);
        smb.put("usergroup", usergroup);

        manProperties.put("rest", rest);
        manProperties.put("smb", smb);

        serviceInstance.setUsername(username);
        serviceInstance.setPassword(password);
        serviceInstance.setUsergroup(usergroup);

        this.updateInstanceGroupConfiguration(manifest, plan);
    }
}
