package com.adskom.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adskom.utilities.httpreqtool.helpers.HttpRequestHelper;
import com.adskom.utilities.httpreqtool.helpers.HttpRequestHelper.HTTP_HEADER_TYPE;
import com.adskom.utilities.httpreqtool.helpers.HttpRequestHelper.HTTP_METHOD_TYPE;

public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);
	private BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));

	private enum AskQuestion {
		Y, N
	};

	public static void main(String[] args) {
		Main main = new Main();
		main.showInterface();
	}

	private void buildBasicQuestion(HttpRequestHelper httpReq) throws IOException {

		System.out.println("Enter Url: ");
		String urlAdress = buff.readLine();
		httpReq.setUrlAdress(urlAdress);
		System.out.println("Setup Additional Headers (Y/N)? ");
		String askHeader = buff.readLine();

		if (AskQuestion.Y.toString().equalsIgnoreCase(askHeader)) {

			boolean intStatus = false;
			while (!intStatus) {
				System.out.println("How many headers that you want to add (1/2/3 ...)? ");

				askHeader = buff.readLine();

				if (askHeader != null && askHeader.matches("(^[0-9]$)|(^[1-9][0-9]+$)")) {
					intStatus = true;
				}
			}
			Map<String, String> httpHeaderMap = new HashMap<String, String>();
			int headerCount = Integer.parseInt(askHeader);
			for (int i = 1; i <= headerCount; i++) {
				System.out.println("Adding Header " + i + ": ");
				System.out.println("Header Name: ");
				String headerName = buff.readLine();
				System.out.println("Header Value: ");
				String headerValue = buff.readLine();
				headerValue = unescapeBackSlashed(headerValue);
				System.out.println("Use vertical tab [0x0b] in the end of value (Y/N) ? ");
				askHeader = buff.readLine();
				if (AskQuestion.Y.toString().equalsIgnoreCase(askHeader)) {
					StringBuffer sb = new StringBuffer(headerValue);
					sb.append(0x0b);
					headerValue = sb.toString();
				}

				httpHeaderMap.put(headerName, headerValue);
			}

			httpReq.setHeaderMap(httpHeaderMap);

		}
	}

	private void preExecuteMenu(HttpRequestHelper httpReq) throws IOException {
		String askHeader = null;
		while (!HTTP_HEADER_TYPE.NONE.toString().equalsIgnoreCase(askHeader)
				&& !HTTP_HEADER_TYPE.XML.toString().equalsIgnoreCase(askHeader)
				&& !HTTP_HEADER_TYPE.JSON.toString().equalsIgnoreCase(askHeader)) {
			System.out.println("Adding Default Headers (NONE/XML/JSON) before send request;");
			System.out.println("NONE => no default header ");
			System.out.println("XML => adding xml header accept : application/xml ");
			System.out.println("JSON => adding json header accept: application/json ");
			System.out.println("choose one: ");
			askHeader = buff.readLine();

		}
		try {
			httpReq.sendRequest(askHeader);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("=== HTTP Request DONE ===");
		}
	}

	private void createPostParameter(HttpRequestHelper httpReq) throws IOException {
		System.out.println("Adding POST parameters (Y/N)? ");
		String askHeader = buff.readLine();

		if (AskQuestion.Y.toString().equalsIgnoreCase(askHeader)) {
			boolean intStatus = false;
			while (!intStatus) {
				System.out.println("How many parameters that you want to add (1/2/3 ...)? ");

				askHeader = buff.readLine();

				if (askHeader != null && askHeader.matches("(^[0-9]$)|(^[1-9][0-9]+$)")) {
					intStatus = true;
				}
			}

			Map<String, String> httpParamMap = new HashMap<String, String>();
			int headerCount = Integer.parseInt(askHeader);
			for (int i = 1; i <= headerCount; i++) {
				System.out.println("Adding Parameter " + i + ": ");
				System.out.println("param Name: ");
				String headerName = buff.readLine();
				System.out.println("param Value: ");
				String headerValue = buff.readLine();

				httpParamMap.put(headerName, headerValue);
			}
			
			httpReq.setFieldMap(httpParamMap);
		}
	}
	
	private String unescapeBackSlashed(String askHeader){
		askHeader = askHeader.replace("\\f", "\f");
		askHeader = askHeader.replace("\\n", "\n");
		askHeader = askHeader.replace("\\r", "\r");
		askHeader = askHeader.replace("\\t", "\t");
		return askHeader;
	}

	public void showInterface() {
		int choose = 0;

		while (choose != 4) {
			System.out.println("ADSKOM HTTP Request Tools");
			System.out.println("Choose Menus:");
			System.out.println("1. HTTP GET Request");
			System.out.println("2. HTTP POST Request");
			System.out.println("3. HTTP POST Request with content");
			System.out.println("4. Exit");
			System.out.print("choose(1/2/3/4/5/6/7): ");
			System.out.println("");

			try {
				HttpRequestHelper httpReq = new HttpRequestHelper();

				choose = Integer.parseInt(buff.readLine());
				if ((choose < 1) || (choose > 3)) {
					continue;
				} else {
					switch (choose) {
					case 1:
						System.out.println("=== HTTP GET Request ===");
						httpReq.setMethodType(HTTP_METHOD_TYPE.GET.toString());
						buildBasicQuestion(httpReq);
						preExecuteMenu(httpReq);
						break;
					case 2:
						System.out.println("=== HTTP POST Request ===");
						httpReq.setMethodType(HTTP_METHOD_TYPE.POST.toString());
						buildBasicQuestion(httpReq);
						createPostParameter(httpReq);
						preExecuteMenu(httpReq);
						break;
					case 3:
						System.out.println("=== HTTP POST Request ===");
						httpReq.setMethodType(HTTP_METHOD_TYPE.POST.toString());
						buildBasicQuestion(httpReq);
						createPostParameter(httpReq);
						System.out.println("Content: ");
						String content = buff.readLine();
						httpReq.setContent(content);
						preExecuteMenu(httpReq);
						break;

					}
				}
			} catch (IOException e) {
				log.error("IOException: "+e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
				log.error("Exception: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
}