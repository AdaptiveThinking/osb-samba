/**
 * 
 */
package de.evoila.cf.broker.service.custom;

import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

import de.evoila.cf.broker.model.*;
import com.jcraft.jsch.JSchException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.client.RestTemplate;
import rx.Observable;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;
import de.evoila.cf.broker.bean.BoshProperties;
import de.evoila.cf.cpi.bosh.connection.BoshConnection;
import io.bosh.client.deployments.SSHConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.evoila.cf.broker.exception.ServiceBrokerException;
import de.evoila.cf.broker.service.impl.BindingServiceImpl;

/**
 * @author Johannes Hiemer.
 *
 */
@Service
public class ExampleBindingService extends BindingServiceImpl {

	private Logger log = LoggerFactory.getLogger(getClass());

	private SecureRandom random = new SecureRandom();
	private BoshConnection connection;
	private final int VMINDEX = 0;

	@Autowired
	BoshProperties boshProperties;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.evoila.cf.broker.service.impl.BindingServiceImpl#createCredentials(
	 * java.lang.String, de.evoila.cf.broker.model.ServiceInstance,
	 * de.evoila.cf.broker.model.ServerAddress)
	 */
	@Override
	protected Map<String, Object> createCredentials(String bindingId, ServiceInstance serviceInstance,
			ServerAddress host, Plan plan, ServiceInstanceBindingRequest request) throws ServiceBrokerException {

		SecureRandom random = new SecureRandom();
		Map<String, Object> credentials = new HashMap<String, Object>();
		credentials.put("password", new BigInteger(130, random).toString(32));
		String username = bindingId.replaceAll("[\\s\\-()]", "");

		//creating HTTP Request
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Map<String, Object>> postRequest = new HttpEntity<Map<String, Object>>(credentials, getHttpHeaders(serviceInstance));
		ResponseEntity<Map>  response = restTemplate.exchange("http://"+host.getIp() + ":5000/user/" + username, HttpMethod.POST, postRequest, Map.class);
		credentials = (Map<String, Object>) response.getBody();
		if(credentials.containsKey("failure")){
			log.error("creating credentials failed");
			throw new ServiceBrokerException("creating credentials failed");
		}
		log.info("credentials created successfully");

		return credentials;
	}

	@Override
	protected void deleteBinding(String bindingId, ServiceInstance serviceInstance) throws ServiceBrokerException {
		Map<String, Object> status = new HashMap<String, Object>();

		//creating HTTP Request
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Map<String, Object>> request = new HttpEntity<Map<String, Object>>(getHttpHeaders(serviceInstance));
		ResponseEntity<Map>  response = restTemplate.exchange("http://"+serviceInstance.getHosts().get(0).getIp() + ":5000/user/" +serviceInstance, HttpMethod.POST, request, Map.class);
		status = (Map<String, Object>) response.getBody();
		if(status.containsKey("failure")){
			log.error("creating credentials failed");
			throw new ServiceBrokerException("creating credentials failed");
		}
		log.info("credentials deleted successfully");

	}

	@Override
	protected List<VolumeMounts> createMountPoint(String bindingId, ServiceInstance serviceInstance, ServerAddress host, Plan plan, ServiceInstanceBindingRequest request, Map<String, Object> credentials) throws ServiceBrokerException {
		VolumeMounts volumeMounts = new VolumeMounts();
		if (serviceInstance.getHosts().get(0) == null)
			return null;

		//Set Mountpoint on container default is /mnt/
		if (request != null && request.getParameters() != null && request.getParameters().containsKey("container_dir")) {
			volumeMounts.setContainer_dir(request.getParameters().get("container_dir"));
		} else {
			volumeMounts.setContainer_dir("/mnt/");
		}

		volumeMounts.setDriver("smbdriver");
		volumeMounts.setMode(VolumeMode.rw);
		volumeMounts.setDevice_type(DeviceType.shared);

		//configuring Mount config Object
		MountConfig mountConfig = new MountConfig("0777", "0777");
		mountConfig.setUsername((String) credentials.get("username"));
		mountConfig.setPassword((String) credentials.get("password"));
		mountConfig.setSource("//" + serviceInstance.getHosts().get(0).getIp() + "/volume");

		Device device = new Device("volume", mountConfig);

		volumeMounts.setDevice(device);

		return Arrays.asList(volumeMounts);

	}


	@Override
	public ServiceInstanceBinding getServiceInstanceBinding(String id) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.evoila.cf.broker.service.impl.BindingServiceImpl#bindRoute(de.evoila.
	 * cf.broker.model.ServiceInstance, java.lang.String)
	 */
	@Override
	protected RouteBinding bindRoute(ServiceInstance serviceInstance, String route) {
		throw new UnsupportedOperationException();
	}
	
    public String nextSessionId() {
        return new BigInteger(130, random).toString(32);
    }

    private void ssh(ServiceInstance serviceInstance, ArrayList<String> commands){

		try {
			connection = new BoshConnection(boshProperties.getUsername(),
					boshProperties.getPassword(),
					boshProperties.getHost(), boshProperties.getAuthentication()).authenticate();
			Observable<Session> oSession = connection.connection().vms().ssh(new SSHConfig("smb" + serviceInstance, "serviceBroker", null, "samba", VMINDEX), "get_credentials.sh");
			Session session = oSession.toBlocking().first();
			Channel chanel = session.openChannel("shell");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(chanel.getOutputStream()));
			for(String command : commands){
				writer.write(command);
			}
			writer.close();
		} catch (JSchException e) {
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	private HttpHeaders getHttpHeaders(ServiceInstance serviceInstance){
		String apiCreds = serviceInstance.getUsername() + ":" + serviceInstance.getPassword();
		apiCreds = new String(Base64.encode(apiCreds.getBytes()));
		HttpHeaders header = new HttpHeaders();
		header.add("Authorization", "Basic "+apiCreds);
		return  header;

	}

}
