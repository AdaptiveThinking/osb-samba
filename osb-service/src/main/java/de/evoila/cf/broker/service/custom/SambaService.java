package de.evoila.cf.broker.service.custom;

public class SambaService {
	
	private boolean initialized;
	
	public void createConnection() {
		this.initialized = true;
	}
	
	public boolean isConnected() {
		return this.initialized;
	}

	public void bind() {}

}