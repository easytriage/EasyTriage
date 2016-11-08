package com.easytriage.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

/*
 * Contains the methods to create index and search
 * */
public class CreateIndexAndSearch {
	private static final Logger logger = Logger.getLogger(CreateIndexAndSearch.class);
	
	private static String FILE_PATH;
	private static List<File> fileList;
	private static int HIT = 1000;

	private static StandardAnalyzer analyzer;
	private static Directory index;
	private static IndexWriterConfig config;

	private CreateIndexAndSearch() {}

	public static void init(String path) {
		analyzer = new StandardAnalyzer();
		index = new RAMDirectory();
		config = new IndexWriterConfig(analyzer);
		fileList = new ArrayList<File>();
		FILE_PATH = path;
	}

	public static boolean createIndex() {
		String content;
		InputStream in;
		try {
			getFileRecursively(new File(FILE_PATH), fileList);
			List<Doc> list = new ArrayList<Doc>();
			list.clear();
			if (fileList.isEmpty()) {
				System.out.println("No file in the dir given");
				return false;
			}

			for (File file : fileList) {
				in = new FileInputStream(file);
				//content = IOUtil.readContentToString("UTF-8", in);
				content = getErrorLine(in);
				Doc d = new Doc();
				d.setContent(content);
				d.setFilename(file.getName());
				d.setPath(file.getPath());
				list.add(d);
				content = "";
			}
			initIndexWriter(index, config, list);

		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.info("");
		return true;
	}

	public static boolean createIndex(String endWith) {
		Date begin = new Date();
		String content;
		InputStream in;
		try {
			getFileRecursively(new File(FILE_PATH), fileList);
			List<Doc> list = new ArrayList<Doc>();
			if (fileList.isEmpty()) {
				System.out.println("File list is empty");
				return false;
			}

			for (File file : fileList) {
				if (!file.getName().endsWith(endWith)) {
					continue;
				}
				in = new FileInputStream(file);
				content = getErrorLine(in);
				//System.out.println("content:" + content);
				Doc d = new Doc();
				d.setContent(content);
				d.setFilename(file.getName());
				d.setPath(file.getPath());
				list.add(d);
				content = "";
			}
			initIndexWriter(index, config, list);

		} catch (Exception e) {
			e.printStackTrace();
		}
		Date end = new Date();
		long time = begin.getTime() - end.getTime();
		System.out.println("Create index spend： " + time + "ms");
		return true;
	}
	
	private static String getErrorLine(InputStream inputStream) throws IOException {
		if (inputStream == null) {
			return null;
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder result = new StringBuilder();
		try {
			String line = null;
			while ((line = reader.readLine()) != null) {
				if(line.contains("[ERROR]") || line.contains("[FAIL]")) {
					result.append(line);
					result.append("\n");
				}
			}
			return result.toString();
		} finally {
			reader.close();
		}
	}
	
	private static void initIndexWriter(Directory index,
			IndexWriterConfig config, List<Doc> docs) throws IOException {
		IndexWriter w;
		try {
			w = new IndexWriter(index, config);
			w.deleteAll();
			for (Doc d : docs) {
				addDoc(w, d);
			}
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}


	private static void addDoc(IndexWriter w, Doc doc) throws IOException {
		Document document = new Document();
		document.add(new TextField("filename", doc.getFilename(), Store.YES));
		document.add(new TextField("content", doc.getContent(), Store.YES));
		document.add(new TextField("path", doc.getPath(), Store.YES));
		w.addDocument(document);
	}
	
	
	public static Map<String, String> searchResult(String queryText) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		result.clear();
		try {
			String q = queryText.replaceAll("AND", "");
			logger.info("");
			logger.info("Search Statement: " + q);
			Query query = new QueryParser("content", analyzer).parse(queryText);
			IndexReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopDocs docs = searcher.search(query, HIT);
			ScoreDoc[] hits = docs.scoreDocs;
			//System.out.println("There are " + hits.length + " file(s) hit.");
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = searcher.doc(hits[i].doc);
				//System.out.println("**********************************************************************************************");
				//System.out.println(hitDoc.get("path"));
				//System.out.println(hitDoc.get("content"));
				result.put(hitDoc.get("path"), hitDoc.get("content"));
				
				//System.out.println("**********************************************************************************************");
				//System.out.println("");
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
    private static void getFileRecursively(File file, List<File> fileList) throws IOException {
    	File[] filearry = file.listFiles();
    	if (filearry == null)
    		return;
    	for (File f : filearry) {
    		if(f.isDirectory()){
				getFileRecursively(f, fileList);
			} else {
				fileList.add(f);
			}
    	}
    }

}
