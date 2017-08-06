package com.ociweb.sitemonitor;

import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.gl.api.GreenRuntime;
import com.ociweb.gl.api.HTTPFieldReader;
import com.ociweb.gl.api.HTTPRequestReader;
import com.ociweb.gl.api.PubSubListener;
import com.ociweb.gl.api.RestListener;
import com.ociweb.pronghorn.network.config.HTTPContentTypeDefaults;
import com.ociweb.pronghorn.pipe.BlobReader;
import com.ociweb.pronghorn.util.Appendables;

public class StatusBehavior implements RestListener, PubSubListener {

	private final GreenCommandChannel cmd;

	private short status = 200;
	
	private final int statusId;
	private final int resetId;
		
	public StatusBehavior(GreenRuntime runtime, int statusId, int resetId) {
		this.cmd = runtime.newCommandChannel(NET_RESPONDER); //TODO: we see 2 sets of constants shoud fine 1
		this.statusId = statusId;
		this.resetId = resetId;
	}

	@Override
	public boolean restRequest(HTTPRequestReader request) {
		
		if (request.getRouteId() == statusId) {
			
			//TODO: signature still not clear what first arg is.
			//TODO: context is bad here should assume no close..
			return cmd.publishHTTPResponse(request, 200, 
					HTTPFieldReader.END_OF_RESPONSE | HTTPFieldReader.CLOSE_CONNECTION, //TODO: too complext
					HTTPContentTypeDefaults.TXT,  //TODO: this is also ugly.
					w->{Appendables.appendValue(w,"Error status code: ",status);} );  
			
		} else if (request.getRouteId() == resetId) {			
			status = 200;
			return cmd.publishHTTPResponse(request, 200);
		}
		return true;
	}

	@Override
	public boolean message(CharSequence topic, BlobReader payload) {
		
		status = payload.readShort();
		
		return true;
	}

}
