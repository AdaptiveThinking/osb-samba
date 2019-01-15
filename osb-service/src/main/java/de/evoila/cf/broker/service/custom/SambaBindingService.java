/**
 *
 */
package de.evoila.cf.broker.service.custom;

import de.evoila.cf.broker.exception.ServiceBrokerException;
import de.evoila.cf.broker.model.*;
import de.evoila.cf.broker.model.catalog.ServerAddress;
import de.evoila.cf.broker.model.catalog.plan.Plan;
import de.evoila.cf.broker.model.volume.*;
import de.evoila.cf.broker.repository.*;
import de.evoila.cf.broker.service.AsyncBindingService;
import de.evoila.cf.broker.service.HAProxyService;
import de.evoila.cf.broker.service.impl.BindingServiceImpl;
import de.evoila.cf.broker.util.RandomString;
import de.evoila.cf.cpi.bosh.connection.BoshConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Johannes Hiemer.
 */
@Service
public class SambaBindingService extends BindingServiceImpl {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static String USERNAME = "username";
    private static String PASSWORD = "password";
    private static String SCHEME = "http";
    private static int PORT = 5000;

    private RandomString usernameRandomString = new RandomString(10);
    private RandomString passwordRandomString = new RandomString(15);

    private BoshConnection connection;

    private final int VMINDEX = 0;

    public SambaBindingService(BindingRepository bindingRepository, ServiceDefinitionRepository serviceDefinitionRepository,
                               ServiceInstanceRepository serviceInstanceRepository, RouteBindingRepository routeBindingRepository,
                               HAProxyService haProxyService, JobRepository jobRepository,
                               AsyncBindingService asyncBindingService, PlatformRepository platformRepository) {
        super(bindingRepository, serviceDefinitionRepository, serviceInstanceRepository, routeBindingRepository, haProxyService, jobRepository, asyncBindingService, platformRepository);
    }

    @Override
    protected RouteBinding bindRoute(ServiceInstance serviceInstance, String route) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ServiceInstanceBinding bindService(String bindingId, ServiceInstanceBindingRequest serviceInstanceBindingRequest,
                          ServiceInstance serviceInstance, Plan plan) throws ServiceBrokerException {

        Map<String, Object> credentials = this.createCredentials(bindingId, serviceInstanceBindingRequest, serviceInstance, plan, null);
        List<VolumeMount> volumeMounts = this.createMountPoint(serviceInstance, serviceInstanceBindingRequest, credentials);

        ServiceInstanceBinding serviceInstanceBinding = new ServiceInstanceBinding(bindingId, serviceInstance.getId(), credentials, null);
        serviceInstanceBinding.setVolumeMounts(volumeMounts);

        return serviceInstanceBinding;
    }

    @Override
    protected Map<String, Object> createCredentials(String bindingId, ServiceInstanceBindingRequest serviceInstanceBindingRequest,
                                                    ServiceInstance serviceInstance, Plan plan, ServerAddress host) throws ServiceBrokerException {

        String username = usernameRandomString.nextString();
        String password = passwordRandomString.nextString();

        Map<String, Object> credentials = new HashMap<>();
        credentials.put(USERNAME, username);
        credentials.put(PASSWORD, password);

        Map<String, Object> status = this.executeRequest(serviceInstance, username, credentials, HttpMethod.POST);
        if (status.containsKey("failure")) {
            log.error("creating credentials failed");
            throw new ServiceBrokerException("creating credentials failed");
        }

        log.info("credentials created successfully");

        return credentials;
    }

    @Override
    protected void unbindService(ServiceInstanceBinding binding, ServiceInstance serviceInstance, Plan plan) throws ServiceBrokerException {
        String username = binding.getCredentials().get(USERNAME).toString();

        Map<String, Object> status = this.executeRequest(serviceInstance, username, null, HttpMethod.DELETE);
        if (status.containsKey("failure")) {
            log.error("creating credentials failed");
            throw new ServiceBrokerException("creating credentials failed");
        }

        log.info("credentials deleted successfully");
    }

    protected List<VolumeMount> createMountPoint(ServiceInstance serviceInstance, ServiceInstanceBindingRequest request,
                                                 Map<String, Object> credentials) {
        VolumeMount volumeMounts = new VolumeMount();
        if (serviceInstance.getHosts().get(0) == null)
            return null;

        //Set Mountpoint on container default is /mnt/
        if (request != null && request.getParameters() != null && request.getParameters().containsKey("container_dir")) {
            volumeMounts.setContainerDir((String) request.getParameters().get("container_dir"));
        } else {
            volumeMounts.setContainerDir("/mnt/");
        }

        volumeMounts.setDriver("smbdriver");
        volumeMounts.setMode(VolumeMode.rw);
        volumeMounts.setDeviceType(DeviceType.shared);

        //configuring Mount config Object
        MountConfig mountConfig = new MountConfig("0777", "0777");
        mountConfig.setUsername((String) credentials.get(USERNAME));
        mountConfig.setPassword((String) credentials.get(PASSWORD));
        mountConfig.setSource("//" + serviceInstance.getHosts().get(0).getIp() + "/volume");

        String volumeName = new RandomString(30).nextString();
        Device device = new Device(volumeName, mountConfig);

        volumeMounts.setDevice(device);

        return Arrays.asList(volumeMounts);

    }

    private Map<String, Object> executeRequest(ServiceInstance serviceInstance, String username, Map<String, Object> content, HttpMethod method) {
        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<Map<String, Object>> request;
        if (content != null)
            request = new HttpEntity<>(content, getHttpHeaders(serviceInstance));
        else
            request = new HttpEntity<>(getHttpHeaders(serviceInstance));

        ResponseEntity<Map> response = restTemplate.exchange(SCHEME + "://" + serviceInstance.getHosts().get(0).getIp()
                        + ":" + PORT + "/user/" + username, method, request, Map.class);
        return (Map<String, Object>) response.getBody();
    }

    private HttpHeaders getHttpHeaders(ServiceInstance serviceInstance) {
        String apiCreds = serviceInstance.getUsername() + ":" + serviceInstance.getPassword();
        apiCreds = new String(Base64.encode(apiCreds.getBytes()));
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", "Basic " + apiCreds);
        return header;
    }

}
