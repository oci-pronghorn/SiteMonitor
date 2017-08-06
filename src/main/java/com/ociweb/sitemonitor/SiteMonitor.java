package com.ociweb.sitemonitor;

import com.ociweb.gl.api.Builder;
import com.ociweb.gl.api.GreenApp;
import com.ociweb.gl.api.GreenRuntime;
import com.ociweb.gl.api.TimeTrigger;

public class SiteMonitor implements GreenApp
{
	
	private final boolean isTLS = true;
	private final int bindPort = 8443;   //TODO: upon permission denied must shut down
	private final String errorTopic = "siteError";
	private int statusId;
	private int resetId;
	private String telemtryHost;
    @Override
    public void declareConfiguration(Builder c) { //TODO: update arch to not use c
	
    	c.enableServer(isTLS, bindPort);
        
    	//TODO: add option to print the right route.
    	statusId = c.registerRoute("/status");         //TODO: rename define route
    	resetId = c.registerRoute("/status?reset"); 
    	
    	c.setTimerPulseRate(20);
    	
    	c.useInsecureNetClient();//NetClient(); //TODO: not obvious this is needed, add flag is missing.
    	//Fully self contained and testing its own telemetry...
    	telemtryHost = c.enableTelemetry();
    }


    @Override
    public void declareBehavior(GreenRuntime runtime) {

    	//TODO: ask for own address...
    	String host = telemtryHost;//"192.168.0.3";//"10.10.10.134";//"twitter.com"; //"www.cnn.com"; //http:/10.10.10.134:8098/
		String path = "/graph.dot";
		int port = 8098;
		
		runtime.addTimePulseListener(new WatcherBehavior(runtime, errorTopic, host, path, port));
    	
    	runtime.addRestListener(new StatusBehavior(runtime, statusId, resetId))
    									.addSubscription(errorTopic)
    								    .includeAllRoutes(); //TODO: this must be last and should support ANY order
    	                               
    	
    	
    }
          
}
