package yeh.poketype;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public class AjaxRequest {
	private String mRequestType;
	private String mUrl;
	private List<NameValuePair> mParams = new ArrayList<NameValuePair>();

	public AjaxRequest(String requestType, String url) {
		setRequestType(requestType);
		setUrl(url);
	}
	
	public void setRequestType(String requestType) {
		requestType = requestType.toUpperCase(Locale.getDefault());
		if (!requestType.equals("GET") && !requestType.equals("POST")) {
			requestType = "GET";
		}
		mRequestType = requestType;
	}

	public String getRequestType() {
		return mRequestType;
	}

	public void setUrl(String url) {
		mUrl = url;
	}

	public String getUrl() {
		return mUrl;
	}

	public String send() throws Exception {
		String output = "";
		try {
			String urlWithParams = mUrl;
			String parameters = getQuery(mParams);

			// If the request type is a GET with parameters, modify the URL
			if (!parameters.equals("") && mRequestType.equals("GET")) {
				urlWithParams += "?" + parameters;
			}

			// Setup the HttpUrlConnection; open it and set timeouts
			URL newUrl = new URL(urlWithParams);
			HttpURLConnection con = (HttpURLConnection) newUrl.openConnection();
			con.setReadTimeout(10000);
			con.setConnectTimeout(15000);
			if (mRequestType.equals("POST")) {
				con.setRequestMethod("POST");
			}

			// Get the status of the connection and proceed accordingly
			int statusCode = con.getResponseCode();
			if (statusCode != HttpURLConnection.HTTP_OK) {
				Log.d("JY", "ERROR: HttpURLConnection(" + mUrl
				        + ") failed with status code " + statusCode);
				throw new RuntimeException();
			} else {
				if (mRequestType.equals("POST")) {
					// Send the POST parameters
					OutputStream out = con.getOutputStream();
					out.write(parameters.getBytes());
					out.flush();
					con.connect();
				}
				// Read the response into the output variable
				output = readStream(con.getInputStream());
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return output;
	}

	// Returns the index of a parameter if found, else -1 otherwise.
	private int findParam(String key) {
		for (int i = 0; i < mParams.size(); i++) {
			if (mParams.get(i).getName().equals(key)) {
				return i;
			}
		}
		return -1;
	}

	// Add a parameter. If the parameter (same key) already existed, replace it.
	public void addParam(String key, String value) {
		int index = findParam(key);
		if (index == -1) {
			mParams.add(new BasicNameValuePair(key, value));
		} else {
			mParams.set(index, new BasicNameValuePair(key, value));
		}
	}

	// Delete a parameter with the given key.
	public void deleteParam(String key) {
		int index = findParam(key);
		if (index != -1) {
			mParams.remove(index);
		}
	}

	// Delete all the parameters.
	public void deleteAllParams() {
		mParams.clear();
	}

	// Convert the InputStream from reading the request output into a String
	private String readStream(InputStream in) {
		Scanner scanner = new Scanner(in);
		String result = scanner.useDelimiter("\\A").next();
		scanner.close();
		return result;
	}
	
	// Build the URL query parameters into a string
	private String getQuery(List<NameValuePair> params)
	        throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (NameValuePair pair : params) {
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}

		return result.toString();
	}
}