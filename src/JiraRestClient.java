
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
 
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.Header;
import java.util.logging.*;

/*************************************************************
 * 
 * @author David Wu
 * Date:28 Aug 2016
 * This is a test class access jira server via jira rest api 
 */
public class JiraRestClient
{
	/***************************
	 * Note:
	 * on JIRA server make sure the 'Allow Remote API Calls' is turned ON under Administration > General Configuration.
	 * on jira server: make sure give this test issue anonymous access for all operation permission via permission scheme
	 */
	private static final Logger LOGGER = Logger.getLogger(JiraRestClient.class.getName() );
	static final String JIRA_BASE_URL = "http://rhe-jira-test01.test.lan:8280/jira/rest/api/2/issue/";
	static final String JIRA_AUTH_URL = "http://rhe-jira-test01.test.lan:8280/jira/rest/auth/1/session";
	static final String JSON_LOGIN_DATA = "{\"username\":\"userNmae\",\"password\":\"passWord\"}";
	
	public static void main(String[] args) {
		 
		try {
			
			
			String issueId = "ISBCS-240/";
			String transUrl = JIRA_BASE_URL + issueId + "transitions?";
			
			// create client
			HttpClient httpClient = HttpClientBuilder.create().build();
            
			// get transitions option for this issue 
			showResult(getValidTransitions(transUrl, httpClient));
			
			// login jira and get session details
			showResult(logInJira(JIRA_AUTH_URL, httpClient));
			
			// Execute change transition request via session id/value (add to header) 
			showResult(changeTransitions(issueId,JIRA_BASE_URL + issueId,httpClient));
 
		} 
		catch (ClientProtocolException e) {
			e.printStackTrace();
		} 
		 catch (IOException e) {
				e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static HttpResponse getValidTransitions(String url, HttpClient client) 
			throws ClientProtocolException, IOException
	{
		  // create a getRequest
		  HttpGet getRequest = new HttpGet(url);
		  getRequest.setHeader("Content-type", "application/json");
		  LOGGER.info("send getValidTransitions request =" + getRequest.toString());
		  return client.execute(getRequest);
	}
	
	public static HttpResponse logInJira(String url, HttpClient client) 
			throws ClientProtocolException, IOException
	{
		
			// Create new getRequest
			HttpPost authPostrequest = new HttpPost(url);
			StringEntity authParam = new StringEntity(JSON_LOGIN_DATA);
			authPostrequest.setEntity(authParam);
			authPostrequest.setHeader("Content-type", "application/json");
			LOGGER.info("send logInJira request = " + authPostrequest.toString());
			return(client.execute(authPostrequest));		
	}
	
	public static HttpResponse changeTransitions(String issueId, String url, HttpClient client) 
			throws ClientProtocolException, IOException
	{		
			// should extract from response of logInJira request, for quick test - hard code-in here 
		    String jsonTransId = "{\"id\":\"141\"}";
			// Create new getRequest
			HttpPost postRequest = new HttpPost(url);
			StringEntity param = new StringEntity(jsonTransId);
			postRequest.setEntity(param);
			postRequest.setHeader("Content-type", "application/json");	
			
			// ****  change transition may require Cookie-based Authentication ***	
			// more details see www
			// https://developer.atlassian.com/jiradev/jira-apis/jira-rest-apis/jira-rest-api-tutorials/jira-rest-api-example-cookie-based-authentication
			// this pat currently not working - response return 405 error - method not allowed for the request resource 
			LOGGER.info("send changeTransitions request = " + postRequest.toString());
			return(client.execute(postRequest));
	}
	
	public static void showResult(HttpResponse response) throws IOException{		
			// Check for HTTP response code: 200 = success
		    if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
		    }			
			 
		    //get all headers
		    /*
		    Header[] headers = response.getAllHeaders();
		    for (Header header : headers) {
		    	System.out.println("Key : " + header.getName()
		    	      + " ,Value : " + header.getValue());
		    }
		   */
		    
			// Get-Capture Complete application/xml body response
		    BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output;
			System.out.println("============Output:============");
			 
			// Simply iterate through XML response and show on console.
			while ((output = br.readLine()) != null) {
							//System.out.println(output);
				LOGGER.info(output);
			}
	}
  
}
