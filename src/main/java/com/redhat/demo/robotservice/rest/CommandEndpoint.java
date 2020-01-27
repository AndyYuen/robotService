package com.redhat.demo.robotservice.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.stereotype.Component;

import com.redhat.demo.robotservice.Util;
import com.redhat.demo.robotservice.model.Command;


@Component
@Path("/")
public class CommandEndpoint {

	static String SEPARATOR = "####################################################################################";

	static Map<String, String> robotNames = new HashMap<String, String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put("LEADER", 		"192.168.99.104");
			put("FOLLOWER", 	"192.168.99.105");
			//put("DM_ROBOT", 	"192.168.99.103");
			put("DM_ROBOT", 	"192.168.0.11");
			put("DANCE_ROBOT", 	"192.168.0.10");
			put("OPENSHIFT", 	"192.168.0.12");
			put("BUILDAH", 		"192.168.0.13");
			put("PODMAN", 		"192.168.0.11");
			put("CRI-O", 		"192.168.0.10");
		}
	};


    
    @POST
    @Path("/command")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendCommand( Command cmd) throws IOException, JSONException, AuthenticationException {
    	System.out.println(SEPARATOR);
    	System.out.println("Robot name: " + cmd.getRobotName() + " Command: " + cmd.getCmdString());
    	if (!robotNames.containsKey(cmd.getRobotName())) {
    		System.out.println("Unknown robot: " + cmd.getRobotName());
    		return Response.status(404).build();
    	}
    	sendToRobot(cmd.getRobotName(), cmd.getCmdString(), robotNames.get(cmd.getRobotName()));
    	return Response.status(201).build();
    }
    
	public static void sendToRobot(String robot, String cmd, String ip) throws IOException, AuthenticationException, JSONException {
		System.out.println("Robot command: " + String.format("{ \"cmd\" : \"%s;\" }", cmd));
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost postRequest = new HttpPost(String.format("http://%s/rpc/Robot.Cmd", ip));
		StringEntity params =new StringEntity(String.format("{ \"cmd\" : \"%s;\" }", cmd));
		postRequest.addHeader("accept", "application/json");
		postRequest.addHeader("content-type", "application/json");
		postRequest.setEntity(params);
/*	    UsernamePasswordCredentials creds
	      = new UsernamePasswordCredentials("user", "password");
	    postRequest.addHeader(new BasicScheme().authenticate(creds, postRequest, null));*/

		HttpResponse response = httpClient.execute(postRequest);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
			   + response.getStatusLine().getStatusCode());
		}
	}
}
