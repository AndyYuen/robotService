package com.redhat.demo.robotservice;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Util {
	static final String DEFAULT_DM_URL = "http://localhost:8080/kie-server/services/rest/server/containers/instances/dataValidation";

	public static List<String> extractCause(String data) throws JSONException {
		JSONObject jsonData = new JSONObject(data);
		JSONArray array =  jsonData.getJSONObject("result").getJSONObject("execution-results").getJSONArray("results"); 
		List<String> list = new ArrayList();
		for (int i = 0; i < array.length(); i++) {  
		     String value = array.getJSONObject(i).get("value").toString();
		     if (isJSONValid(value)) {
			     //System.out.println("value=" + value);
			     JSONObject valueObj = new JSONObject(value);
			     if (valueObj.has("org.drools.core.runtime.rule.impl.FlatQueryResults")) {
				     String result = valueObj.get("org.drools.core.runtime.rule.impl.FlatQueryResults").toString();
				     //System.out.println("result=" + result);
				     JSONObject resultObj = new JSONObject(result);
				     if (resultObj.has("idResultMaps")) {
				    	 String map = resultObj.get("idResultMaps").toString();
				    	 //System.out.println("map=" + map);
				    	 JSONObject mapObj = new JSONObject(map);
					     JSONArray elements = mapObj.getJSONArray("element");
					     for (int j = 0; j < elements.length(); j++) {
					    	 JSONArray elmArray = elements.getJSONObject(j).getJSONArray("element");
					    	 for (int k = 0; k < elmArray.length(); k++) {
						    	 JSONObject valObj = elmArray.getJSONObject(k);
						    	 //System.out.println("value=" + valObj.get("value").toString());
						    	 valObj = new JSONObject(valObj.get("value").toString());
						    	 //System.out.println("class=" + valObj.get("com.myspace.datavalidation.ValidationError").toString());
						    	 valObj = new JSONObject(valObj.get("com.myspace.datavalidation.ValidationError").toString());
						    	 //System.out.println("cause=" + valObj.get("cause").toString());
						    	 list.add(valObj.get("cause").toString());
					    	 }
					     }
				     }
			     }
		     }

		}
		return list;
	}

	public static boolean isJSONValid(String test) {
	    try {
	        new JSONObject(test);
	    } catch (JSONException ex) {
	        // edited, to include @Arthur's comment
	        // e.g. in case JSONArray is valid as well...
	        try {
	            new JSONArray(test);
	        } catch (JSONException ex1) {
	            return false;
	        }
	    }
	    return true;
	}	

	public static String invokeDM(String command) throws IOException, AuthenticationException, JSONException {

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost postRequest = new HttpPost(DEFAULT_DM_URL);
		StringEntity params =new StringEntity(command);
		postRequest.addHeader("accept", "application/json");
		postRequest.addHeader("content-type", "application/json");
		postRequest.setEntity(params);
	    UsernamePasswordCredentials creds
	      = new UsernamePasswordCredentials("user", "password");
	    postRequest.addHeader(new BasicScheme().authenticate(creds, postRequest, null));

		HttpResponse response = httpClient.execute(postRequest);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
			   + response.getStatusLine().getStatusCode());
		}

		String content = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		httpClient.getConnectionManager().shutdown();

		List<String> list = extractCause(content);
		StringBuilder builder = new StringBuilder();
		builder.append("{ \"valid\" : \"" + ((list.size() > 0)? "0": "1"));
		
		if (list.size() > 0) {
			int i = 0;
			builder.append("\" , \"error\" : [");
			for (String str : list) {
				System.out.println("list element: " + str);
				if (i++ > 0) {
					builder.append(',');
				}
				builder.append("{ \"cause\" : \"" + str + "\" }");
			}
			builder.append("]}");
		}
		else {
			builder.append("\" }");
		}
		return builder.toString();
	}
}
