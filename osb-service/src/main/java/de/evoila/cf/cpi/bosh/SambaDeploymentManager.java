package de.evoila.cf.cpi.bosh;

import de.evoila.cf.broker.bean.BoshProperties;
import de.evoila.cf.broker.model.ServiceInstance;
import de.evoila.cf.broker.model.catalog.plan.Plan;
import de.evoila.cf.broker.model.credential.UsernamePasswordCredential;
import de.evoila.cf.cpi.CredentialConstants;
import de.evoila.cf.cpi.bosh.deployment.DeploymentManager;
import de.evoila.cf.cpi.bosh.deployment.manifest.Manifest;
import de.evoila.cf.security.credentials.CredentialStore;
import de.evoila.cf.security.utils.RandomString;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jannik Heyl.
 */
public class SambaDeploymentManager extends DeploymentManager {

    private CredentialStore credentialStore;

    public SambaDeploymentManager(BoshProperties properties, Environment environment, CredentialStore credentialStore) {
       super (properties, environment);
       this.credentialStore = credentialStore;
    }

    @Override
    protected void replaceParameters(ServiceInstance serviceInstance, Manifest manifest, Plan plan,
                                     Map<String, Object> customParameters, boolean isU) {
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

        UsernamePasswordCredential usernamePasswordCredential = credentialStore.createUser(serviceInstance, CredentialConstants.ROOT_CREDENTIALS);
        String usergroup = new RandomString(15).nextString();

        rest.put("user", usernamePasswordCredential.getUsername());
        rest.put("password", usernamePasswordCredential.getPassword());
        smb.put("usergroup", usergroup);

        serviceInstance.setUsergroup(usergroup);

        this.updateInstanceGroupConfiguration(manifest, plan);
    }
}
