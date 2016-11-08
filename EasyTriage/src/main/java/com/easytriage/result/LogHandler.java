package com.easytriage.result;

import java.util.List;
import java.io.File;  
import java.io.FileInputStream;  
import java.io.FileNotFoundException;  
import java.io.FileOutputStream;  
import java.io.FileWriter;
import java.io.IOException;  
import java.io.InputStream;  
import java.io.OutputStream;  

import org.apache.log4j.Logger;

import com.easytriage.analysis.TriageHelper;

/*
 * Class contains methods for generating triage result from log
 * */
public class LogHandler {
	private static final Logger logger = Logger.getLogger(LogHandler.class);
	
	public static String getLogFile() {
		List<String> args = TriageHelper.getKeyWords("src/main/resources/log4j.properties");
		String ret = null;
		for (String item : args) {
			if (item.contains("log4j.appender.A1.file")){
				ret = item.split("=")[1];
				break;
			}
		}
		return ret;
	}
	
	public static void copyToFile(String id, File dstFile) {
		String filePath = getLogFile();
		if (filePath == null) {
			System.out.println("The log file path is null");
			return;
		}
		File srcFile = new File(filePath);
		copyFile(srcFile, dstFile, true);
	}
	
	  
	    public static boolean copyFile(File srcFile, File destFile,  
	            boolean overlay) {  	  
	        // 判断源文件是否存在  
	        if (!srcFile.exists()) {  
	            System.out.println("源文件不存在！");  
	            return false;  
	        } else if (!srcFile.isFile()) {  
	        	System.out.println("复制文件失败，源文件不是一个文件！");  
	            return false;  
	        }  
	  
	        if (destFile.exists()) {  
	            // 如果目标文件存在并允许覆盖  
	            if (overlay) {  
	                // 删除已经存在的目标文件，无论目标文件是目录还是单个文件  
	            	destFile.delete();  
	            }  
	        } else {  
	            // 如果目标文件所在目录不存在，则创建目录  
	            if (!destFile.getParentFile().exists()) {  
	                // 目标文件所在目录不存在  
	                if (!destFile.getParentFile().mkdirs()) {  
	                    // 复制文件失败：创建目标文件所在目录失败  
	                    return false;  
	                }  
	            }  
	        }  
	  
	        // 复制文件  
	        int byteread = 0; // 读取的字节数  
	        InputStream in = null;  
	        OutputStream out = null;  
	  
	        try {  
	            in = new FileInputStream(srcFile);  
	            out = new FileOutputStream(destFile);  
	            byte[] buffer = new byte[1024];  
	  
	            while ((byteread = in.read(buffer)) != -1) {  
	                out.write(buffer, 0, byteread);  
	            }  
	            return true;  
	        } catch (FileNotFoundException e) {  
	            return false;  
	        } catch (IOException e) {  
	            return false;  
	        } finally {  
	            try {  
	                if (out != null)  
	                    out.close();  
	                if (in != null)  
	                    in.close();  
	            } catch (IOException e) {  
	                e.printStackTrace();  
	            }  
	        }  
	    }  
	    
	    public static void clearLogFile() {
	    	String log = getLogFile();
	    	if (log == null) {
	    		System.out.println("The log file path is null");
	    		return;
	    	}
	    	
	    	File logFile = new File(log);
	    	FileWriter fw;
			try {
				fw = new FileWriter(logFile);
		    	fw.write("");
		    	fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    public static void main(String args[]) {
	    	//copyToFile("37260222");
	    }
}
