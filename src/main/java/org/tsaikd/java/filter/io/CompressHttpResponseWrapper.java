package org.tsaikd.java.filter.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.googlecode.htmlcompressor.compressor.ClosureJavaScriptCompressor;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;

public class CompressHttpResponseWrapper extends HttpServletResponseWrapper {

	static Log log = LogFactory.getLog(CompressHttpResponseWrapper.class);

	protected ByteArrayOutputStream baos;
	protected ByteServletOutputStream bsos;
	protected PrintWriter printWriter;

	public CompressHttpResponseWrapper(HttpServletResponse response, ByteArrayOutputStream baos) {
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

	public String compress() {
		HtmlCompressor htmlCompressor = new HtmlCompressor();
		htmlCompressor.setCompressCss(true);
		htmlCompressor.setCompressJavaScript(true);

		ClosureJavaScriptCompressor closureCompressor = new ClosureJavaScriptCompressor();
		closureCompressor.setCompilationLevel(CompilationLevel.SIMPLE_OPTIMIZATIONS);
		CompilerOptions compilerOptions = new CompilerOptions();
		compilerOptions.setLanguageIn(LanguageMode.ECMASCRIPT5);
		closureCompressor.setCompilerOptions(compilerOptions);
		htmlCompressor.setJavaScriptCompressor(closureCompressor);
		return htmlCompressor.compress(baos.toString());
	}

}
