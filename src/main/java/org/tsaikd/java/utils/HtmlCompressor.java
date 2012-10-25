package org.tsaikd.java.utils;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.googlecode.htmlcompressor.compressor.ClosureJavaScriptCompressor;
import com.googlecode.htmlcompressor.compressor.YuiCssCompressor;

public class HtmlCompressor {

	private static Logger log = Logger.getLogger(HtmlCompressor.class);
	private static ClosureJavaScriptCompressor jsCompressor = new ClosureJavaScriptCompressor();
	private static YuiCssCompressor cssCompressor = new YuiCssCompressor();

	public enum ConfType {
		Javascript,
		CSS,
		CombineText,
	}

	public static void compress(String dstFile, List<String> srcFiles, ConfType type) throws Exception {
		FileWriter fw = new FileWriter(dstFile);
		StringBuilder sb = new StringBuilder();

		for (String jsFile : srcFiles) {
			File file = new File(jsFile);
			if (!file.isFile()) {
				log.warn("Skip not found file: " + jsFile);
				continue;
			}
			log.debug("Loading " + jsFile + " ...");
			sb.append(FileUtils.readFileToString(file, "UTF-8").replaceAll("/\\*!", "/*"));
		}

		if (sb.length() > 0) {
			String comp = null;
			switch (type) {
			case CSS:
				log.debug("Compressing CSS ...");
				comp = cssCompressor.compress(sb.toString());
				break;
			case Javascript:
				log.debug("Compressing Javascript ...");
				comp = jsCompressor.compress(sb.toString());
				break;
			default:
				log.debug("Combine plain text ...");
				comp = sb.toString();
				break;
			}
			if (comp != null) {
				fw.write(comp);
			}
			fw.close();
			log.debug("Compressed to " + dstFile);
		}
	}

	public static void compressFromXml(String xmlConfig) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new File(xmlConfig));
		doc.getDocumentElement().normalize();
		NodeList nodeList = doc.getElementsByTagName("compress");

		for (int i=0 ; i<nodeList.getLength() ; i++) {
			Node node = nodeList.item(i);
			if (node.getNodeName() == "compress") {
				ConfCompress conf = new ConfCompress();
				parseXmlCompress(conf, node);
				if (conf.isValid()) {
					compress(conf.outputFile, conf.inputFiles, conf.type);
				}
			}
		}
	}

	private static class ConfCompress {
		public ConfType type = ConfType.Javascript;
		public String outputFile = null;
		public List<String> inputFiles = new ArrayList<String>();
		public boolean isValid() {
			if (outputFile == null)
				return false;
			if (inputFiles.size() < 1)
				return false;
			return true;
		}
	}

	private static void parseXmlCompress(ConfCompress conf, Node node) throws Exception {
		NodeList nodeList = node.getChildNodes();
		for (int i=0 ; i<nodeList.getLength() ; i++) {
			Node child = nodeList.item(i);
			if (child.getNodeName() == "type") {
				parseXmlType(conf, child);
			} else if (child.getNodeName() == "outputFile") {
				parseXmlOutputFile(conf, child);
			} else if (child.getNodeName() == "inputList") {
				parseXmlInputList(conf, child);
			}
		}
	}

	private static void parseXmlType(ConfCompress conf, Node node) throws Exception {
		NodeList nodeList = node.getChildNodes();
		for (int i=0 ; i<nodeList.getLength() ; i++) {
			Node child = nodeList.item(i);
			String value = getNodeValue(child);
			if (value != null) {
				if (value.equalsIgnoreCase("css")) {
					conf.type = ConfType.CSS;
				}
			}
		}
	}

	private static void parseXmlOutputFile(ConfCompress conf, Node node) throws Exception {
		NodeList nodeList = node.getChildNodes();
		for (int i=0 ; i<nodeList.getLength() ; i++) {
			Node child = nodeList.item(i);
			String value = getNodeValue(child);
			if (value != null) {
				conf.outputFile = value;
			}
		}
	}

	private static void parseXmlInputList(ConfCompress conf, Node node) throws Exception {
		NodeList nodeList = node.getChildNodes();
		for (int i=0 ; i<nodeList.getLength() ; i++) {
			Node child = nodeList.item(i);
			if (child.getNodeName() == "inputFile") {
				parseXmlInputFile(conf, child);
			}
		}
	}

	private static void parseXmlInputFile(ConfCompress conf, Node node) throws Exception {
		NodeList nodeList = node.getChildNodes();
		for (int i=0 ; i<nodeList.getLength() ; i++) {
			Node child = nodeList.item(i);
			String value = getNodeValue(child);
			if (value != null) {
				conf.inputFiles.add(value);
			}
		}
	}

	private static String getNodeValue(Node node) throws Exception {
		String value = node.getNodeValue();
		if (value != null) {
			value = value.trim();
			if (value.isEmpty()) {
				value = null;
			}
		}
		return value;
	}

}
