/**
 * 
 */
package de.evoila.cf.broker.service.sample.raw;

import de.evoila.cf.cpi.existing.CustomExistingServiceConnection;

/**
 * @author Johannes Hiemer
 * This class contains a specific implementation for the access to the 
 * underlying service. For a database for example it contains the access
 * to the connection, the update and create commands etc.
 * 
 * This class should not have any dependencies to Spring or other large
 * Frameworks but instead work with the Drivers directly against the native
 * API.
 */
public class ExampleBackendRawService implements CustomExistingServiceConnection {
	
	private boolean initialized;
	
	public void createConnection() {
		this.initialized = true;
	}
	
	public boolean isConnected() {
		return this.initialized;
	}

	public void bind() {
		// We bind 
	}

}