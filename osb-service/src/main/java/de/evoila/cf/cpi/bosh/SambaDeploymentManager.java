package de.evoila.cf.cpi.bosh;

import de.evoila.cf.broker.bean.BoshProperties;
import de.evoila.cf.broker.model.Plan;
import de.evoila.cf.broker.model.ServiceInstance;
import de.evoila.cf.cpi.bosh.deployment.DeploymentManager;
import de.evoila.cf.cpi.bosh.deployment.manifest.Manifest;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

/**
 * Created by jannikheyl on 19.01.18.
 */
@Service
public class SambaDeploymentManager extends DeploymentManager{
    public static final String VOLUME_SIZE = "volumeSize";
    public static final String VM_TYPE = "vm_type";
    public static final String VOLUME_UNIT = "volumeUnit";

    public SambaDeploymentManager(BoshProperties properties) {
       super (properties);
    }

    @Override
    protected void replaceParameters(ServiceInstance instance, Manifest manifest, Plan plan, Map<String, String> customParameters) {
        HashMap<String, Object> properties = new HashMap<>();
        properties.putAll(plan.getMetadata());
        properties.putAll(customParameters);

        SecureRandom random = new SecureRandom();
        HashMap<String, Object> manProperties = new HashMap<String, Object >();

        HashMap<String, Object> rest = new HashMap<String, Object>();
        HashMap<String, Object> smb = new HashMap<String, Object>();

        rest.put("user", new BigInteger(130, random).toString(32).toString());
        rest.put("password", new BigInteger(130, random).toString(32).toString());
        smb.put("usergroup", new BigInteger(130, random).toString(32).toString());

        manProperties.put("rest", rest);
        manProperties.put("smb", smb);

           //persist credentials in serviceInstant Object
        instance.setUsername((String)rest.get("user"));
        instance.setPassword((String)rest.get("password"));
        instance.setUsergroup((String) smb.get("usergroup"));

        if(plan.getVolumeSize() != null){
            manifest.getJobs().get(0).setPersistent_disk(plan.getVolumeSize(), plan.getVolumeUnit());
        }
        if(properties.containsKey(VM_TYPE)){
            manifest.getJobs().get(0).setVm_type((String) properties.get(VM_TYPE));
        }
        manifest.getJobs().get(0).setProperties(manProperties);


    }
}
