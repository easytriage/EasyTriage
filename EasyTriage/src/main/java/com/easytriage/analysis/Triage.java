package com.easytriage.analysis;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ch.ethz.ssh2.Connection;

import com.easytriage.result.LogHandler;
import com.easytriage.result.SSHHelper;

/*
 * The triage operation starts from this class
 * */
public class Triage {
	private static final Logger logger = Logger.getLogger(Triage.class);

	public static void doTriage() throws Exception {
		// Map<String, String> triageArgs = TriageHelper.getTriageArgs();
		// List<String> triageArgs =
		// TriageHelper.getKeyWords("src/triageConfig/TriageArgs");
		// if (triageArgs == null || triageArgs.isEmpty()) {
		// logger.error("The args of triage is empty");
		// return;
		// }
		// for (Map.Entry<String, String> entry : triageArgs.entrySet()) {
		// doTriage(entry.getKey(),entry.getValue());
		// //copyToFile(entry.getKey());
		// }

		// Login the target mc which has the json result
		Connection con = SSHHelper.getSSHConnection("10.115.173.209", "root",
				"ca$hc0w");
		// The location of triage result
		String url = "http://pa-dbc1130.eng.vmware.com/bpei/triage/";
		// The location of json result
		String path = "/tmp/scratch_results_from_cat/results/1909/super-main/4565084/result.json";
		List<String> triageArgs = TriageHelper.getRunId(con, path);
		/*
		 * Do triage for every testrunId, put the result(log4j result) to dbc,
		 * then clear the log for next run
		 */
		for (String id : triageArgs) {
			File rootDir = new File(System.getProperty("user.home"),
					"autoTriage/" + id);
			boolean r = doTriage(id, rootDir.getAbsolutePath());
			File logFile = new File(System.getProperty("user.home"),
					"autoTriage/" + "result/" + id + ".log");
			url = "http://pa-dbc1130.eng.vmware.com/bpei/triage/";
			url = url + id + ".log";
			logger.info("**********************************************End**********************************************");
			TriageHelper.fillInLink(con, id, url, path);
			LogHandler.copyToFile(id, logFile);
			SSHHelper.scpLocalFileToRemote(con, logFile.getAbsolutePath(),
					"/dbc/pa-dbc1130/bpei/triage");
			LogHandler.clearLogFile();
		}

		/*
		 * Get the summary report from the query result
		 */
		for (Entry<String, Map<String, String>> entry : TriageHelper.resultMap
				.entrySet()) {
			System.out.println("Failed by the similiar error: "
					+ entry.getKey());
			for (Entry<String, String> e : entry.getValue().entrySet()) {
				System.out.println(e.getKey().split("_TDS.")[1]);
			}
			System.out.println("");
		}
	}

	/*
	 * create index and search with testrunId and log path(local)
	 * */
	public static boolean doTriage(String id, String path) {
		logger.info("**********************************************Begin**********************************************");
		boolean r = TriageHelper.getLogsRecursivelyFromTestRunID(id, path);
		if (r == false)
			return r;
		r = TriageHelper.createIndex();
		if (r == false)
			return r;
		TriageHelper.search();
		return r;
	}

	public static void main(String args[]) {
		try {
			doTriage();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
