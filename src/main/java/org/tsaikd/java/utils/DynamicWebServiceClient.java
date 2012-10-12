package org.tsaikd.java.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.sun.org.apache.xpath.internal.XPathAPI;

public class DynamicWebServiceClient implements InvocationHandler {

	static Log log = LogFactory.getLog(DynamicWebServiceClient.class);

	public static int DefaultRetryMax = 1;
	public static long DefaultRetryWaitMs = 0;
	public static long DefaultReceiveTimeoutMs = 600000;

	private static DefaultHttpClient httpClient = new DefaultHttpClient();
	private static DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

	private HashMap<String, String> mapMethodAction = new HashMap<>();
	private int retryMax = DefaultRetryMax;
	private long retryWaitMs = DefaultRetryWaitMs;
	private long receiveTimeoutMs = DefaultReceiveTimeoutMs;

	private Document wsdlDoc;
	private RPCServiceClient wsClient;
	private Object reflectProxy;
	private String targetNamespace;

	static {
		docFactory.setNamespaceAware(true);
	}

	public <T> T create(Class<T> serviceClass, String wsdlUrl) {
		if (reflectProxy == null) {
			try {
				log.debug("Create WebService Client for " + wsdlUrl);
				HttpGet method = new HttpGet(wsdlUrl);
				HttpResponse httpRes = httpClient.execute(method);
				if (httpRes.getStatusLine().getStatusCode() >= 400) {
					throw new RuntimeException(httpRes.getStatusLine().getReasonPhrase());
				}
				String httpData = EntityUtils.toString(httpRes.getEntity(), "UTF-8");
				wsdlDoc = docFactory.newDocumentBuilder().parse(new ByteArrayInputStream(httpData.getBytes("UTF-8")));

				Node node = XPathAPI.selectSingleNode(wsdlDoc, "//*[name()=\"soap:address\"]/@location");
				EndpointReference epr = new EndpointReference(node.getNodeValue());

				node = XPathAPI.selectSingleNode(wsdlDoc, "/*[name()=\"wsdl:definitions\"]/@targetNamespace");
				targetNamespace = node.getNodeValue();

				wsClient = new RPCServiceClient();
				wsClient.setTargetEPR(epr);
				wsClient.getOptions().setTimeOutInMilliSeconds(receiveTimeoutMs);

				reflectProxy = Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class[] {serviceClass}, this);
			} catch (IOException | SAXException | ParserConfigurationException | TransformerException e) {
				throw new RuntimeException(e);
			}
		}
		return serviceClass.cast(reflectProxy);
	}

	public <T> T get(Class<T> serviceClass) {
		return serviceClass.cast(reflectProxy);
	}

	public void setRetry(int retryMax, long retryWaitMs) {
		this.retryMax = retryMax;
		this.retryWaitMs = retryWaitMs;
	}

	public void setTimeout(long receiveTimeoutMs) {
		this.receiveTimeoutMs = receiveTimeoutMs;
		if (wsClient != null) {
			wsClient.getOptions().setTimeOutInMilliSeconds(receiveTimeoutMs);
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (null == args) {
			args = new Object[0];
		}
		Class<?> retType = method.getReturnType();
		String methodName = method.getName();
		Object[] ret = null;
		Exception eRet = null;
		int retryNow = 0;

		if (mapMethodAction.containsKey(methodName)) {
			wsClient.getOptions().setAction(mapMethodAction.get(methodName));
		} else {
			String selector = String.format("//*[name()=\"wsdl:operation\"][@name=\"%1$s\"]/*[name()=\"soap:operation\"]/@soapAction", methodName);
			Node node = XPathAPI.selectSingleNode(wsdlDoc, selector);
			mapMethodAction.put(methodName, node.getNodeValue());
			wsClient.getOptions().setAction(node.getNodeValue());
		}

		QName opName = new QName(targetNamespace, methodName);

		while (retryNow < retryMax) {
			try {
				ret = wsClient.invokeBlocking(opName, args, new Class[] {retType});
				eRet = null;
				break;
			} catch (Exception e) {
				retryNow++;
				eRet = e;
				if (retryWaitMs > 0) {
					Thread.sleep(retryWaitMs);
				}
			}
		}

		if (eRet != null) {
			throw eRet;
		}

		if (ret == null) {
			return ret;
		}

//		if (retType.isArray()) {
//			try {
//				Object newret = ret[0];
//				Method retMethod = newret.getClass().getMethod("getString");
//				newret = retMethod.invoke(newret);
//				newret = ((ArrayList<?>) newret).toArray(new String[0]);
//				return newret;
//			} catch(Exception e) {
//				e.printStackTrace();
//			}
//		}

		return ret[0];
	}

}
