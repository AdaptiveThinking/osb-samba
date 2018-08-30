package de.evoila.cf.cpi.bosh;

import de.evoila.cf.broker.bean.BoshProperties;
import de.evoila.cf.broker.model.DashboardClient;
import de.evoila.cf.broker.model.Plan;
import de.evoila.cf.broker.model.ServiceInstance;
import de.evoila.cf.broker.repository.PlatformRepository;
import de.evoila.cf.broker.service.CatalogService;
import de.evoila.cf.broker.service.availability.ServicePortAvailabilityVerifier;
import de.evoila.cf.cpi.bosh.deployment.DeploymentManager;
import io.bosh.client.deployments.Deployment;
import io.bosh.client.errands.ErrandSummary;
import io.bosh.client.vms.Vm;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SmbBoshPlatformService extends BoshPlatformService {

    private static final int defaultPort = 5000;

    public SmbBoshPlatformService(PlatformRepository repository, CatalogService catalogService,
                                  ServicePortAvailabilityVerifier availabilityVerifier,
                                  BoshProperties boshProperties, Optional<DashboardClient> dashboardClient,
                                  DeploymentManager deploymentManager,
                                  Environment environment) {

        super(repository, catalogService, availabilityVerifier,
                boshProperties, dashboardClient, new SambaDeploymentManager(boshProperties, environment));
    }

    public void runCreateErrands(ServiceInstance instance, Plan plan, Deployment deployment, Observable<List<ErrandSummary>> errands) {}

    protected void runUpdateErrands(ServiceInstance instance, Plan plan, Deployment deployment, Observable<List<ErrandSummary>> errands) {}

    protected void runDeleteErrands(ServiceInstance instance, Deployment deployment, Observable<List<ErrandSummary>> errands) {}

    @Override
    protected void updateHosts(ServiceInstance serviceInstance, Plan plan, Deployment deployment) {
        List<Vm> vms = super.getVms(serviceInstance);
        if(serviceInstance.getHosts() == null)
            serviceInstance.setHosts(new ArrayList<>());
        serviceInstance.getHosts().clear();

        vms.forEach(vm -> serviceInstance.getHosts().add(super.toServerAddress(vm, defaultPort)));
    }

    @Override
    public void postDeleteInstance(ServiceInstance serviceInstance) {}

}