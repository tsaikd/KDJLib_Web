package org.tsaikd.java.filter.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.CRC32;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EtagHttpResponseWrapper extends HttpServletResponseWrapper {

	static Log log = LogFactory.getLog(EtagHttpResponseWrapper.class);

	protected ByteArrayOutputStream baos;
	protected ByteServletOutputStream bsos;
	protected PrintWriter printWriter;

	public EtagHttpResponseWrapper(HttpServletResponse response, ByteArrayOutputStream baos) {
		super(response);
		this.baos = baos;
		bsos = new ByteServletOutputStream(baos);
	}

	@Override
	public ServletOutputStream getOutputStream() {
		return bsos;
	}

	@Override
	public PrintWriter getWriter() {
		if (printWriter == null) {
			printWriter = new PrintWriter(bsos);
		}
		return printWriter;
	}

	@Override
	public void flushBuffer() throws IOException {
		bsos.flush();
		if (printWriter != null) {
			printWriter.flush();
		}
	}

	public String getEtag() {
		byte[] data = baos.toByteArray();
		CRC32 crc32 = new CRC32();
		crc32.update(data);
		return String.valueOf(crc32.getValue());
	}

}
