package org.tsaikd.java.test;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tsaikd.java.utils.HtmlCompressor;

public class JsCompressor {

	static Log log = LogFactory.getLog(JsCompressor.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		log.debug("Start");
		System.out.println(new File(".").getAbsolutePath());
		HtmlCompressor.compressFromXml("test/jsCompressor.xml");
		System.out.println(new File(".").getAbsolutePath());
		log.debug("End");
	}

}
