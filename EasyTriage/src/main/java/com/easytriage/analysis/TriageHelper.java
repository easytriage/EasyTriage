package com.easytriage.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import ch.ethz.ssh2.Connection;

import org.apache.log4j.Logger;

import com.easytriage.result.SSHHelper;


public class TriageHelper {
	private static final Logger logger = Logger.getLogger(TriageHelper.class);
	
	private static String WINDOW = "windows";
	private static String LINUX = "linux";
	private static String MAC = "mac";
	
	public static Map<String, Map<String, String>> resultMap = new HashMap<String, Map<String, String>>();
	
	public static void setIndexRootPath(String dir) {
		if (StringUtils.isBlank(dir)) {
			System.out.println("ERROR: the root dir is blank");
			return;
		}
		CreateIndexAndSearch.init(dir);
	}

	
	public static boolean createIndex() {
		return CreateIndexAndSearch.createIndex();
	}
	
	public static Map<String, String> searchResult(String query) {
		if (StringUtils.isBlank(query)) {
			System.out.println("ERROR: the query statement is blank");
			return null;
		}
		return CreateIndexAndSearch.searchResult(query);
	}
	
	public static boolean getLogsRecursivelyFromTestRunID(String id, String dir) {
		if (StringUtils.isBlank(id) || StringUtils.isBlank(dir)) {
			System.out.println("ERROR: the id or dir is blank");
			return false;
		}
		logger.info("Test run id: " + id);
		setIndexRootPath(dir);
		char[] num = id.toCharArray();
		String path = "";
		for (char c : num) {
			path = path + c + "/";
		}
		String url = "https://cat.eng.vmware.com/PA/results/esx/" + path;
		boolean r = GetLogs.getRecursivelyFromLink(url, dir);
		if (r != true) {
			url = "https://cat-wdc-services.eng.vmware.com/WDC/results/esx/" + path;
			r = GetLogs.getRecursivelyFromLink(url, dir);
		}
		if (r != true) {
			logger.error("Fail to find log file with the url given");
		}
		return r;
	}
	
	public static String searchFilter(String text) {
		text = text.replaceAll("[^a-zA-Z ]", " ");
		text = text.replaceAll("VAR", " ");
        return text.trim();
	}
	
	public static Map<String, String> queryWithErrorLine(String errorLine) {
		String[] content = errorLine.split("\n");
		
		for (int i=0; i<content.length; i++) {
			if (content[i].contains("::Inline")) {
				content[i] = "";
			}
		}
		List<String> wordList = getKeyWords("src/triageConfig/FailureWordList");
		List<String> skips = getKeyWords("src/triageConfig/IgnoreFailures");
		String failureWord = null;
		String query = null;
		for(String s : content) {
			if (isSkip(skips, s))
				continue;
			failureWord = MatchWord(wordList, s);
			if (failureWord != null) {
				String tmp[] = s.split(failureWord);
				query = failureWord + " AND" + tmp[1];
				query = searchFilter(query);
				break;
			}
		}
		if (query == null)
			return null;
		Map<String, String> ret = searchResult(query);
		if (!resultMap.containsKey(query)) {
			resultMap.put(query, ret);
		}
		else {
			Map<String, String> temp = resultMap.get(query);
			for (Entry<String, String> entry : ret.entrySet()) {
				if(temp.containsKey(entry.getKey()))
					continue;
				temp.put(entry.getKey(), entry.getValue());
			}
			resultMap.put(query, temp);
		}
		return ret;
	}
	
	public static Map<String, String> getTriageArgs() {
		List<String> args = getKeyWords("src/triageConfig/TriageArgs");
		Map<String, String> ret = new LinkedHashMap<String, String>();
		for (String t : args) {
			ret.put(t.split(" ")[0].trim(), t.split(" ")[1].trim());
		}
		return ret;
	}
	
	
	public static List<String> getKeyWords(String dir) {
		File file = new File(dir);
		List<String> list = new ArrayList<String>();
		String word = null;
		try {
			InputStream input = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(input);
			BufferedReader buffer = new BufferedReader(reader);
			
			while(true) {
				word = buffer.readLine();
				if (word == null)
					break;
				list.add(word);
			}
			buffer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return list;
	}
	
	public static String MatchWord(List<String> list, String line) {
		if (list == null || list.isEmpty()) {
			System.out.println("ERROR: The list is empty");
			return null;
		}
		for (String w : list) {
			if (line.contains(w)) {
				return w;
			}
		}
		return null;
	}
	
	private static boolean isSkip(List<String> list, String line) {
		if (list == null || list.isEmpty()) {
			System.out.println("ERROR: The list is empty");
			return false;
		}
		for (String s : list) {
			if (line.contains(s)) {
				return true;
			}
		}
		return false;
	}
	
	public static void search() {
		Map<String, String> result = searchResult("Test AND Failed");
		logger.info("Total: " + result.size() + " failed");
		if (result.size() == 0)
			return;
		for (Map.Entry<String, String> entry : result.entrySet()) {
			logger.info(entry.getKey().split("_TDS.")[1]);
		}
		while (!result.isEmpty()) {
			Map<String, String> sameFailure = getTheSameFailure(result);
			if (sameFailure == null)
				continue;
			Map<String, String> report = new HashMap<String, String>();
			for (Map.Entry<String, String> entry : sameFailure.entrySet()) {
				if(result.containsKey(entry.getKey())) {
					result.remove(entry.getKey());
					report.put(entry.getKey(),entry.getValue());
				}
			}
			logger.info("Failed by the similiar error : ");
			for (Map.Entry<String, String> entry : report.entrySet()) {
				logger.info("Case log path: " + entry.getKey().split("_TDS.")[1]);
				//logger.info(entry.getValue());
				//System.out.println(entry.getValue());
			}
		}
	}
	
	public static Map<String, String> getTheSameFailure(Map<String, String> result) {
		Map<String, String> sameFailure = null;
		for (Map.Entry<String, String> entry : result.entrySet()) {
			sameFailure = queryWithErrorLine(entry.getValue());
			if (sameFailure != null)
				break;
		}
		return sameFailure;
	}
	
	public static String getOsInfo() {
		Properties prop = System.getProperties();
		String os = prop.getProperty("os.name");
		System.out.println(os);
		if (os.toLowerCase().startsWith("win")) {
			return WINDOW;
		} else if (os.toLowerCase().startsWith("mac")) {
			return MAC;
		} else if (os.toLowerCase().startsWith("linux")) {
			return LINUX; 
		} else {
			System.out.println("Unknow OS");
			return null;
		}
	}
	
	public static List<String> getRunId(Connection con, String path) throws Exception {
		if (con == null || path == null)
			return null;
		List<String> list = new LinkedList<String>();
		String command = "grep testrun_id " + path;
		Map<String, String> re = SSHHelper.getRemoteSSHCmdOutput(con, command);
		for (Map.Entry<String, String> e : re.entrySet()) {
			
			String[] tmp = e.getValue().split(",");
			for(int i = 0; i < tmp.length; i++) {
				String t = tmp[i].replaceAll(",|\"", "");
				if (t.split(": ").length == 2) {
					String id = t.split(": ")[1];
					System.out.println(id);
					list.add(id);
				}
			}
		}
		return list;
	}
	
	public static void fillInLink(Connection con, String id, String url, String path) throws Exception {
		if (con == null || id == null || url == null || path == null)
			return;
		String command = "sed -i '/\"testrun_id\": \"" +  id + "\"," + "/a \\        \"autoTriage\": " + "\"" + url + "\",' " + path;
		SSHHelper.getRemoteSSHCmdOutput(con, command);
	}

}
