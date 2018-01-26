package de.evoila.cf.cpi.bosh;

import de.evoila.cf.broker.bean.BoshProperties;
import de.evoila.cf.broker.model.DashboardClient;
import de.evoila.cf.broker.model.Plan;
import de.evoila.cf.broker.model.ServerAddress;
import de.evoila.cf.broker.model.ServiceInstance;
import de.evoila.cf.broker.repository.PlatformRepository;
import de.evoila.cf.broker.service.CatalogService;
import de.evoila.cf.broker.service.availability.ServicePortAvailabilityVerifier;
import de.evoila.cf.cpi.bosh.*;
import de.evoila.cf.cpi.bosh.deployment.DeploymentManager;
import io.bosh.client.deployments.Deployment;
import io.bosh.client.vms.Vm;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SmbBoshPlatformService extends BoshPlatformService {
    public SmbBoshPlatformService(PlatformRepository repository,
                                  CatalogService catalogService,
                                  ServicePortAvailabilityVerifier availabilityVerifier,
                                  BoshProperties boshProperties,
                                  Optional<DashboardClient> dashboardClient,
                                  DeploymentManager deploymentManager) {
        super(repository,
                catalogService,
                availabilityVerifier,
                boshProperties,
                dashboardClient,
                deploymentManager);
    }


    @Override
    protected void updateHosts(ServiceInstance instance, Plan plan, Deployment deployment){
        int port = 445;
        List<Vm> vms= super.getVms(instance);
        if(instance.getHosts() == null)
            instance.setHosts(new ArrayList<>());
        instance.getHosts().clear();
        vms.forEach(vm -> instance.getHosts().add( new ServerAddress(String.format("%s-%d", vm.getJobName(), vm.getIndex()), vm.getIps().get(0), port)));
    }



}