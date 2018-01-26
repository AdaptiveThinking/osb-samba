/**
 * 
 */
package de.evoila.cf.broker.service.custom;

import de.evoila.cf.broker.bean.ExistingEndpointBean;
import de.evoila.cf.broker.exception.PlatformException;
import de.evoila.cf.broker.model.Plan;
import de.evoila.cf.broker.model.ServiceInstance;
import de.evoila.cf.broker.service.sample.ExampleBackendService;
import de.evoila.cf.cpi.existing.CustomExistingService;
import de.evoila.cf.cpi.existing.CustomExistingServiceConnection;
import de.evoila.cf.cpi.existing.ExistingServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * @author Johannes Hiemer
 *
 */
@Service
@EnableConfigurationProperties
@ConditionalOnBean(ExistingEndpointBean.class)
public class ExampleExistingServiceFactory extends ExistingServiceFactory {
	
	@Autowired
	private ExampleBackendService exampleDbService;
	
	@Override
	protected void createInstance(CustomExistingServiceConnection connection, String database) throws PlatformException {
		if(connection instanceof ExampleBackendService)
			createInstance((ExampleBackendService) connection, database);
	}
		
	@Override
	protected void deleteInstance(CustomExistingServiceConnection connection, String database) throws PlatformException {
		if(connection instanceof ExampleBackendService)
			deleteInstance((ExampleBackendService) connection, database);
	}

	protected void deleteInstance(ExampleBackendService connection, String database)
			throws PlatformException {
		log.info("Deleting the Example Service...");		
	}

	@Override
	protected CustomExistingService getCustomExistingService() {
		return exampleDbService;
	}

	protected void createInstance(ExampleBackendService connection, String database)
			throws PlatformException {
		log.info("Creating the Example Service...");
	}
	
	@Override
	public ServiceInstance postProvisioning(ServiceInstance serviceInstance, Plan plan) throws PlatformException {
		log.info("Executing Post Provisioning the Example Service...");
		
		return serviceInstance;
	}

}
