package com.redhat.demo.robotservice.rest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
			put("LEADER", 		"192.168.1.126");
			put("FOLLOWER", 	"192.168.99.105");
			put("DM_ROBOT", 	"192.168.99.103");
			//put("DM_ROBOT", 	"192.168.2.112");
			put("DANCE_ROBOT", 	"192.168.99.106");
			put("OPENSHIFT", 	"192.168.99.104");
			put("BUILDAH", 		"192.168.99.105");
			put("PODMAN", 		"192.168.99.103");
			put("CRI-O", 		"192.168.99.106");
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
		HttpGet getRequest = new HttpGet(URLEncoder.encode(String.format("http://%s/exec?param=%s", ip, cmd), 
				StandardCharsets.UTF_8.toString()));
		getRequest.addHeader("accept", "application/json");


		HttpResponse response = httpClient.execute(getRequest);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
			   + response.getStatusLine().getStatusCode());
		}
		else {
			String content = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
			httpClient.getConnectionManager().shutdown();

			System.out.println("***********");
			System.out.println(content);
		}
	}
}
