package com.easytriage.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.HttpsURLConnection; 

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


public class GetLogs {
	private static final Logger logger = Logger.getLogger(GetLogs.class);
	private static StringBuilder result = new StringBuilder();
	
	public static String sendGetAndSave(String url, String param, String filePath) {
		if (StringUtils.isBlank(url)) {
			System.out.println("ERROR: the url of sendGet is blank");
			return null;
		}
		
		BufferedReader in = null;
		String urlNameString;
		try {
			if (StringUtils.isNotBlank(param)) {
				urlNameString = url + "?" + param;
			} else {
				urlNameString = url;
			}

			URL realUrl = new URL(urlNameString);
			HttpsURLConnection connection = (HttpsURLConnection)realUrl.openConnection();
			SSLSocketFactory s = HttpsUtil.getHttpsSocket();
			HttpsURLConnection.setDefaultSSLSocketFactory(s);
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.connect();

			in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			int cnt = 0;
			while ((line = in.readLine()) != null) {
				result.append(line);
				result.append("\n");
				if (cnt > 200) {
					save2Files(result.toString(), filePath);
					result.delete(0, result.length());
					cnt = 0;
				}
			}
			save2Files(result.toString(), filePath);
			result.delete(0, result.length());
			cnt = 0;
		} catch (Exception e) {
			//System.out.println("Send get request error:" + e);
			//e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result.toString();
	}
	
	public static String sendGet(String url, String param) {
		if (StringUtils.isBlank(url)) {
			System.out.println("ERROR: the url of sendGet is blank");
			return null;
		}
		StringBuilder result = new StringBuilder();
		BufferedReader in = null;
		String urlNameString;
		try {
			if (StringUtils.isNotBlank(param)) {
				urlNameString = url + "?" + param;
			} else {
				urlNameString = url;
			}

			URL realUrl = new URL(urlNameString);
			HttpsURLConnection connection = (HttpsURLConnection)realUrl.openConnection();
			SSLSocketFactory s = HttpsUtil.getHttpsSocket();
			HttpsURLConnection.setDefaultSSLSocketFactory(s);
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.connect();

			in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result.append(line).append("\n");
			}
		} catch (Exception e) {
			//System.out.println("Send get request error:" + e);
			//e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result.toString();
	}
	


	private static boolean save2Files(String str, String path) {
		if (StringUtils.isBlank(str) || StringUtils.isBlank(path)) {
			System.out.println("ERROR: The arg of save2Files is not valid");
		}
		
		createFileRecursively(path);
		
		File log = new File(path);
		try {
			OutputStream out = new FileOutputStream(log);
			out.write(str.getBytes());
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return false;
	}

	public static void getLogsFromUrl(String url, String param, String path) {
		if (StringUtils.isBlank(url)) {
			System.out.println("ERROR: the url of sendGet is blank");
			return;
		}
		BufferedReader in = null;
		BufferedWriter bw = null;
		String urlNameString;
		try {
			if (StringUtils.isNotBlank(param)) {
				urlNameString = url + "?" + param;
			} else {
				urlNameString = url;
			}

			URL realUrl = new URL(urlNameString);
			HttpsURLConnection connection = (HttpsURLConnection)realUrl.openConnection();
			SSLSocketFactory s = HttpsUtil.getHttpsSocket();
			HttpsURLConnection.setDefaultSSLSocketFactory(s);
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.connect();

			in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			
			createFileRecursively(path);
			File log = new File(path);
			Writer w = new OutputStreamWriter(new FileOutputStream(log));
			bw = new BufferedWriter(w);
			int cnt = 0;
			while ((line = in.readLine()) != null) {
				bw.write(line);
				if (++cnt > 10) {
					bw.flush();
					cnt = 0;
				}
			}
		} catch (Exception e) {
			//System.out.println("Send get request error:" + e);
			//e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				bw.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	private static boolean createFileRecursively(String path) {
		if (path == null) {
			return false;
		}

		File file = new File(path);

		if (file.isDirectory()) {
			return file.mkdirs();
		}

		if (file.getParentFile() != null) {
			file.getParentFile().mkdirs();
		}

		try {
			return file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private static List<String> getLogList(String str) {
		if (StringUtils.isBlank(str)) {
			System.out.println("The param of getLogList is blank");
			return null;
		}
		List<String> testCases = new ArrayList<String>();
		String[] list = str.split("\n");
		for (String tmp : list) {
			if(tmp.contains("_TDS.")) {
				testCases.add(tmp.split("href=\"")[1].split("/\">")[0]);
			}
		}	
		return testCases;
	}
	
	public static boolean getRecursivelyFromLink(String url, String path) {
		String reps = sendGet(url, "");
		List<String> nameList = getLogList(reps);
		if(nameList == null)
			return false;
		String root = url.split("esx")[1].replaceAll("/", "");
		if (path.endsWith(File.separator)) {
			path = path.substring(0, path.length()-2);
		}
		String filePath = null;
		String logUrl = null;
		for (String s : nameList) {
			filePath = path + File.separator + root + File.separator + s + File.separator + "testcase.log";
			logger.info("The test case name: " + s);
			createFileRecursively(filePath);
			
			logUrl = url + s + "/testcase.log";
			//save2Files(sendGet(logUrl, ""), filePath);
			sendGetAndSave(logUrl, "", filePath);
			//getLogsFromUrl(logUrl, "", filePath);
		}
		return true;
	}

}
