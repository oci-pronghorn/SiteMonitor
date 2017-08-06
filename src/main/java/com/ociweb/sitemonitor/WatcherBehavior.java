package com.ociweb.sitemonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.gl.api.GreenRuntime;
import com.ociweb.gl.api.HTTPResponseListener;
import com.ociweb.gl.api.HTTPResponseReader;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.pronghorn.util.Appendables;

public class WatcherBehavior implements TimeListener, HTTPResponseListener, StartupListener {  //TODO: transducer listeners should have transducer first..

	private final Logger logger = LoggerFactory.getLogger(WatcherBehavior.class);
	
	private final GreenCommandChannel cmd;
	private final String host;
	private final String path;
	private final String topic;
	private final int port;
	
	public WatcherBehavior(GreenRuntime runtime, String topic, String host, String path, int port) {
		this.cmd = runtime.newCommandChannel(NET_REQUESTER | DYNAMIC_MESSAGING);
		this.topic = topic;
		this.host = host;
		this.path = path;
		this.port = port;
	}


	@Override
	public void startup() {
		//immediate on startup check.
		if (!cmd.httpGet(host, port, path)) { //TODO: clean up method signatures, clean up client useage??
			logger.warn("skipped polling this cycle, system is overloaded");
		}
	}

	
	@Override
	public void timeEvent(long time, int iteration) {
		
	//	Appendables.appendValue(Appendables.appendEpochTime(System.out, time).append(" iteration "),iteration).append("\n");
				 
		if (!cmd.httpGet(host, port, path)) { //TODO: clean up method signatures, clean up client useage??
			logger.warn("skipped polling this cycle, system is overloaded");
		}
		
	}

	//TODO: use direct method dispatch for the routes so we can avoid the ugly nested if statements?
	
	@Override
	public boolean responseHTTP(HTTPResponseReader reader) {
		
		if (reader.isBeginningOfResponse()) { //TODO: add is beginning of response??
			
//			logger.info("got status of {} connection {} pos {} dataSize {}",
//					    reader.statusCode(), 
//					    reader.connectionId(), reader.absolutePosition(), reader.available());
	
			if (200 != reader.statusCode() ) {			
				return cmd.publishTopic(topic,
						                w->{w.writeShort(reader.statusCode());});			
			}
		} //else if (reader.isEndOfResponse()) {
		//	logger.info("got end of response status {} connection {} dataSize {}", reader.statusCode(), reader.connectionId(), reader.available());
		//	cmd.httpClose(host, port);
		//} 
		
//		else {
//			
//			new Exception("why get middle").printStackTrace();
//			
//			logger.info("got middle data status {} connection {} dataSize {}", reader.statusCode(), reader.connectionId(), reader.available());
//		}
		return true;
	}



}
