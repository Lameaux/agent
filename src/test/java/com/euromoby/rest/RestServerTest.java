package com.euromoby.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.agent.Config;
import com.euromoby.service.ServiceState;

@RunWith(MockitoJUnitRunner.class)
public class RestServerTest {


	@Mock
	Config config;
	@Mock
	RestServerInitializer restServerInitializer;


	RestServer server;

	@Before
	public void init() {
		Mockito.when(config.getRestPort()).thenReturn(Integer.parseInt(Config.DEFAULT_AGENT_BASE_PORT) + RestServer.REST_PORT);
		server = new RestServer(config, restServerInitializer);
	}

	@Test
	public void shouldBeStopped() {
		assertEquals(ServiceState.STOPPED, server.getServiceState());
	}

	@Test
	public void testGetServiceName() {
		assertEquals(RestServer.SERVICE_NAME, server.getServiceName());
	}	

	@Test
	public void testStartAndStop() throws Exception {
		server.stopService();
		assertEquals(ServiceState.STOPPED, server.getServiceState());
		server.startService();
		assertEquals(ServiceState.RUNNING, server.getServiceState());
		server.startService();
		assertEquals(ServiceState.RUNNING, server.getServiceState());
		server.stopService();
		assertEquals(ServiceState.STOPPED, server.getServiceState());		
		server.stopService();
		assertEquals(ServiceState.STOPPED, server.getServiceState());		
	}
	
	
}