/**
 * 
 */
package de.evoila.cf.broker.service.custom;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Johannes Hiemer
 */
@Service
public class SambaCustomImplementation {
	
	private SambaService sambaBackendService;
	
	public SambaService connection(List<String> hosts, int port, String database, String username,
			String password) throws Exception {
		sambaBackendService = new SambaService();
		sambaBackendService.createConnection();
		
		return sambaBackendService;
	}

	public void bindRoleToDatabaseWithPassword(SambaService connection, String database,
                                               String username, String password) throws Exception {
		sambaBackendService.bind();
	}

}