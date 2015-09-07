package com.adskom.utilities.httpreqtool.helpers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class helper for HTTP request
 * 
 * @author irnawan Eka
 *
 */
public class HttpRequestHelper {

	private static final Logger log = LoggerFactory.getLogger(HttpRequestHelper.class);

	private Map<String, String> headerMap;
	private Map<String, String> paramMap;
	private String methodType;
	private String urlAdress;
	private String content;

	public static enum HTTP_HEADER_TYPE {
		XML, JSON, NONE
	}

	public static enum HTTP_METHOD_TYPE {
		GET, POST
	}

	public HttpRequestHelper() {
	}

	/**
	 * 
	 * @param paramMap
	 *            parameters key and value pairs
	 * @param urlAdress
	 *            URL target
	 */
	public HttpRequestHelper(String methodType, Map<String, String> paramMap, String urlAdress) {
		super();
		this.methodType = methodType;
		this.paramMap = paramMap;
		this.urlAdress = urlAdress;
	}

	/**
	 * @param headerMap
	 *            Http header name value pairs
	 * @param paramMap
	 *            parameters key and value pairs
	 * @param urlAdress
	 *            URL target
	 */
	public HttpRequestHelper(String methodType, Map<String, String> headerMap, Map<String, String> paramMap,
			String urlAdress) {
		super();
		this.methodType = methodType;
		this.headerMap = headerMap;
		this.paramMap = paramMap;
		this.urlAdress = urlAdress;
	}

	/**
	 * set HTTP Method type
	 * 
	 * @param methodType
	 *            GET or POST
	 */
	public void setMethodType(String methodType) {
		this.methodType = methodType;
	}

	/**
	 * set paramMap for header base on key and value pair
	 * 
	 * @param paramMap
	 */
	public void setHeaderMap(Map<String, String> headerMap) {
		this.headerMap = headerMap;
	}

	/**
	 * set paramMap for parameter base on key and value pair
	 * 
	 * @param paramMap
	 */
	public void setFieldMap(Map<String, String> paramMap) {
		this.paramMap = paramMap;
	}

	/**
	 * set paramMap for parameter base on key and value pair
	 * 
	 * @param paramMap
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * set URL target
	 * 
	 * @param urlAdress
	 */
	public void setUrlAdress(String urlAdress) {
		this.urlAdress = urlAdress;
	}

	/**
	 * Send Request
	 * 
	 * 
	 * @return respond from url target
	 * @throws Exception
	 */
	
	public String sendRequest(String headerType) throws Exception {
		String responseMsg = null;
		DefaultHttpClient httpClient = new DefaultHttpClient();

		HttpRequest request = null;

		if (HTTP_METHOD_TYPE.POST.toString().equalsIgnoreCase(methodType)) {
			request = new HttpPost(urlAdress);
			log.debug("Http Method type: POST");

			List<NameValuePair> params = constructPostData();
			if (params != null && params.size() > 0) {
				log.debug("Sending parameters: ");
				((HttpPost) request).setEntity(new UrlEncodedFormEntity(params));
			}

			if (content != null) {
				log.debug("content: "+content);
				StringEntity se = new StringEntity(content);
				((HttpPost) request).setEntity(se);
			}

		} else {
			String urlParameters = constructGetData();
			if (urlParameters != null)
				urlAdress = urlAdress.concat(urlParameters);
			request = new HttpGet(urlAdress);
			log.debug("Http Method type: GET");
		}

		log.debug("Send request to URL:" + urlAdress);

		if ((headerType != null) && (headerType.equals(HTTP_HEADER_TYPE.NONE.toString()) == false)) {
			if (headerType.equalsIgnoreCase(HTTP_HEADER_TYPE.XML.toString()))
				((HttpMessage) request).addHeader("accept", "application/xml");
			else if (headerType.equalsIgnoreCase(HTTP_HEADER_TYPE.JSON.toString()))
				((HttpMessage) request).addHeader("accept", "application/json");
		}

		buildHeader((HttpMessage) request);

		HttpResponse response = httpClient.execute((HttpUriRequest) request);

		if (response.getStatusLine().getStatusCode() != 200) {

			throw new Exception("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());

		} else {
			log.info("Success: HTTP 200.");
		}

		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

		String output;
		log.debug("Output from Server .... \n");
		StringBuffer sb = new StringBuffer();
		while ((output = br.readLine()) != null) {
			sb.append(output);
		}

		responseMsg = sb.toString();
		log.debug(sb.toString());
		httpClient.getConnectionManager().shutdown();

		return responseMsg;
	}

	private void buildHeader(HttpMessage request) {
		if (headerMap != null && headerMap.size() > 0) {
			Set<String> keyList = headerMap.keySet();
			int i = 1;
			for (String key : keyList) {
				request.addHeader(key, headerMap.get(key));
				log.debug("header " + i + ": [key: " + key + ", value: " + headerMap.get(key));
				i++;
			}
		}
	}

	/**
	 * Build URL POST parameters
	 * 
	 * @return URL parameters
	 * @throws UnsupportedEncodingException
	 */
	private List<NameValuePair> constructPostData() throws UnsupportedEncodingException {

		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();

		if ((paramMap != null) && (paramMap.size() > 0)) {

			Set<String> keyList = paramMap.keySet();
			int i = 1;
			for (String key : keyList) {
				urlParameters.add(new BasicNameValuePair(key, paramMap.get(key)));
				log.debug("parameter " + i + ": [key: " + key + ", value: " + paramMap.get(key));
				i++;
			}
		}
		return urlParameters;

	}

	/**
	 * Build URL GET parameters
	 * 
	 * @return URL parameters
	 * @throws UnsupportedEncodingException
	 */
	private String constructGetData() {

		String urlParams = null;
		if ((paramMap != null) && (paramMap.size() > 0)) {

			Set<String> keyList = paramMap.keySet();

			StringBuffer urlParamsBuffer = new StringBuffer("?");
			int keyListSize = keyList.size();
			int i = 0;
			for (String key : keyList) {
				urlParamsBuffer.append(key + "=" + paramMap.get(key));
				if (i + 1 < keyListSize)
					urlParamsBuffer.append("&");

				log.debug("parameter " + (i + 1) + ": [key: " + key + ", value: " + paramMap.get(key));
				i++;
			}

			urlParams = urlParamsBuffer.toString();
			urlParamsBuffer = null; // release memory
		}
		return urlParams;

	}


}
