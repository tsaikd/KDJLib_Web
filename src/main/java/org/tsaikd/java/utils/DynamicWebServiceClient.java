package org.tsaikd.java.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.engine.DefaultObjectSupplier;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties.ProxyProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DynamicWebServiceClient implements InvocationHandler {

	static Log log = LogFactory.getLog(DynamicWebServiceClient.class);

	public static int DefaultRetryMax = 1;
	public static long DefaultRetryWaitMs = 0;
	public static long DefaultReceiveTimeoutMs = 600000;

	private static SOAPFactory soapFactory = OMAbstractFactory.getSOAP12Factory();

	private HashMap<String, String> mapMethodAction = new HashMap<>();
	private HashMap<String, String[]> mapMethodArgName = new HashMap<>();
	private int retryMax = DefaultRetryMax;
	private long retryWaitMs = DefaultRetryWaitMs;
	private long receiveTimeoutMs = DefaultReceiveTimeoutMs;

	private Document wsdlDoc;
	private RPCServiceClient wsClient;
	private Object reflectProxy;
	private String targetNamespace;

	static {
		XPathUtils.getDocumentBuilderFactory().setNamespaceAware(true);
	}

	public <T> T create(Class<T> serviceClass, String wsdlUrl, boolean noProxy) {
		if (reflectProxy == null) {
			try {
				log.debug("Create WebService Client for " + wsdlUrl);
				HttpGet method = new HttpGet(wsdlUrl);
				HttpClient httpClient = new ConfigHttpClient(noProxy);
				HttpResponse httpRes = httpClient.execute(method);
				if (httpRes.getStatusLine().getStatusCode() >= 400) {
					throw new RuntimeException(httpRes.getStatusLine().getReasonPhrase());
				}
				String httpData = EntityUtils.toString(httpRes.getEntity(), "UTF-8");
				wsdlDoc = XPathUtils.parseDocumentr(new ByteArrayInputStream(httpData.getBytes("UTF-8")));

				Node node = XPathUtils.selectSingleNode(wsdlDoc, "//*[name()='soap:address'][@location]/@location");
				if (node == null) {
					node = XPathUtils.selectSingleNode(wsdlDoc, "//*[name()='wsdlsoap:address'][@location]/@location");
				}
				if (node == null) {
					throw new ParserConfigurationException("cannot find EPR in wsdl " + wsdlUrl);
				}
				EndpointReference epr = new EndpointReference(node.getNodeValue());

				node = XPathUtils.selectSingleNode(wsdlDoc, "/*[local-name()='definitions'][@targetNamespace]/@targetNamespace");
				targetNamespace = node.getNodeValue();

				wsClient = new RPCServiceClient();
				wsClient.setTargetEPR(epr);
				if (!noProxy) {
					HttpHost httpProxy = ConfigHttpClient.getDefaultProxy();
					if (httpProxy != null) {
						ProxyProperties proxyProperty = new ProxyProperties();
						proxyProperty.setProxyName(httpProxy.getHostName());
						proxyProperty.setProxyPort(httpProxy.getPort());
						wsClient.getOptions().setProperty(HTTPConstants.PROXY, proxyProperty);
					}
				}
				wsClient.getOptions().setTimeOutInMilliSeconds(receiveTimeoutMs);

				// .NET Development Server not support this feature
				wsClient.getOptions().setProperty(HTTPConstants.CHUNKED, false);

				reflectProxy = Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class[] {serviceClass}, this);
			} catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException e) {
				throw new RuntimeException(e);
			}
		}
		return serviceClass.cast(reflectProxy);
	}

	public <T> T create(Class<T> serviceClass, String wsdlUrl) {
		return create(serviceClass, wsdlUrl, false);
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

	private String typeHandler(Class<?> type, Object value) {
		String typeName = type.getName();
		if (type.isPrimitive()) {
			if (typeName.equals("int")) {
				return typeHandler(Integer.class, value);
			}
		} else if (typeName.equals("java.util.Date")) {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
			return fmt.format(value);
		}
		Object castValue = type.cast(value);
		if (castValue == null) {
			return null;
		} else {
			return castValue.toString();
		}
	}

	private void addOMChild(OMElement wrapper, String name, Class<?> type, Object value) {
		if (type.isArray()) {
			Class<?> componentType = type.getComponentType();
			for (Object obj : (Object[]) value) {
				addOMChild(wrapper, name, componentType, obj);
			}
		} else {
			String text = typeHandler(type, value);
			OMElement child = soapFactory.createOMElement(name, targetNamespace, "");
			child.setText(text);
			wrapper.addChild(child);
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

		synchronized (mapMethodAction) {
			if (!mapMethodAction.containsKey(methodName)) {
				String selector = String.format("//*[local-name()='operation'][@name='%1$s']/*[local-name()='operation'][@soapAction]/@soapAction", methodName);
				Node node = XPathUtils.selectSingleNode(wsdlDoc, selector);
				mapMethodAction.put(methodName, node.getNodeValue());
			}
		}

		synchronized (mapMethodArgName) {
			if (!mapMethodArgName.containsKey(methodName)) {
				ArrayList<String> argName = new ArrayList<>();
				String selector = String.format("//*[local-name()='schema']/*[local-name()='element'][@name='%1$s']/*[local-name()='complexType']/*[local-name()='sequence']/*[local-name()='element'][@name]/@name", methodName);
				NodeList nodeList = XPathUtils.selectNodeList(wsdlDoc, selector);
				for (int i=0 ; i<nodeList.getLength() ; i++) {
					Node node = nodeList.item(i);
					argName.add(node.getNodeValue());
				}
				mapMethodArgName.put(methodName, argName.toArray(new String[]{}));
			}
		}
		String[] argName = mapMethodArgName.get(methodName);
		OMElement wrapper = soapFactory.createOMElement(methodName, targetNamespace, "");
		Class<?>[] types = method.getParameterTypes();
		for (int i=0 ; i<types.length ; i++) {
			addOMChild(wrapper, argName[i], types[i], args[i]);
		}

		while (retryNow < retryMax) {
			try {
				OMElement wsRes;
				synchronized (wsClient) {
					wsClient.getOptions().setAction(mapMethodAction.get(methodName));
					wsRes = wsClient.sendReceive(wrapper);
				}
				ret = BeanUtil.deserialize(wsRes, new Class[] {retType}, new DefaultObjectSupplier());
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
