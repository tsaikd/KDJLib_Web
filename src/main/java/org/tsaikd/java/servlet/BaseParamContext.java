package org.tsaikd.java.servlet;

import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tsaikd.java.mongodb.JSONLongIdCallback;

import com.mongodb.util.JSON;

public class BaseParamContext {

	static Log log = LogFactory.getLog(BaseParamContext.class);

	protected HttpServletRequest req;

	protected HttpServletResponse res;

	public BaseParamContext(HttpServletRequest req, HttpServletResponse res) {
		this.req = req;
		this.res = res;
	}

	public static LinkedList<String> getParams(HttpServletRequest req, String name) {
		LinkedList<String> list = new LinkedList<>();
		String[] values = req.getParameterValues(name);

		if (values == null && !name.endsWith("[]")) {
			values = req.getParameterValues(name + "[]");
		}
		if (values == null) {
			return list;
		}
		for (String value : values) {
			list.add(value);
		}
		return list;
	}

	public LinkedList<String> getParams(String name) {
		return getParams(req, name);
	}

	public static String getParam(HttpServletRequest req, String name, String defValue) {
		LinkedList<String> res = getParams(req, name);
		return res.isEmpty() ? defValue : res.getLast();
	}

	public String getParam(String name, String defValue) {
		return getParam(req, name, defValue);
	}

	public String getParam(String name) {
		String value = getParam(name, null);
		if (value == null) {
			throw new IllegalArgumentException("field " + name + " is a must parameter");
		}
		return value;
	}

	public static LinkedList<Integer> getParamInts(HttpServletRequest req, String name) {
		LinkedList<Integer> res = new LinkedList<>();
		LinkedList<String> values = getParams(req, name);
		for (String value : values) {
			for (String val : value.split(",")) {
				res.add(Integer.parseInt(val));
			}
		}
		return res;
	}

	public LinkedList<Integer> getParamInts(String name) {
		return getParamInts(req, name);
	}

	public static Integer getParamInt(HttpServletRequest req, String name, Integer defValue) {
		LinkedList<Integer> res = getParamInts(req, name);
		return res.isEmpty() ? defValue : res.getLast();
	}

	public Integer getParamInt(String name, Integer defValue) {
		return getParamInt(req, name, defValue);
	}

	public Integer getParamInt(String name) {
		Integer value = getParamInt(name, null);
		if (value == null) {
			throw new IllegalArgumentException("field " + name + " is a must parameter");
		}
		return value;
	}

	public static LinkedList<Long> getParamLongs(HttpServletRequest req, String name) {
		LinkedList<Long> res = new LinkedList<>();
		LinkedList<String> values = getParams(req, name);
		for (String value : values) {
			for (String val : value.split(",")) {
				res.add(Long.parseLong(val));
			}
		}
		return res;
	}

	public LinkedList<Long> getParamLongs(String name) {
		return getParamLongs(req, name);
	}

	public static Long getParamLong(HttpServletRequest req, String name, Long defValue) {
		LinkedList<Long> res = getParamLongs(req, name);
		return res.isEmpty() ? defValue : res.getLast();
	}

	public Long getParamLong(String name, Long defValue) {
		return getParamLong(req, name, defValue);
	}

	public Long getParamLong(String name) {
		Long value = getParamLong(name, null);
		if (value == null) {
			throw new IllegalArgumentException("field " + name + " is a must parameter");
		}
		return value;
	}

	public static Boolean getParamBoolean(HttpServletRequest req, String name, Boolean defValue) {
		String value = getParam(req, name, null);
		if (value == null) {
			return defValue;
		}
		if (value.equalsIgnoreCase("true")) {
			return true;
		}
		if (value.equalsIgnoreCase("false")) {
			return false;
		}
		if (value.equalsIgnoreCase("yes")) {
			return true;
		}
		if (value.equalsIgnoreCase("no")) {
			return false;
		}
		if (value.equalsIgnoreCase("on")) {
			return true;
		}
		if (value.equalsIgnoreCase("off")) {
			return false;
		}
		if (value.equalsIgnoreCase("1")) {
			return true;
		}
		if (value.equalsIgnoreCase("0")) {
			return false;
		}
		return defValue;
	}

	public Boolean getParamBoolean(String name, Boolean defValue) {
		return getParamBoolean(req, name, defValue);
	}

	public Boolean getParamBoolean(String name) {
		Boolean value = getParamBoolean(name, null);
		if (value == null) {
			throw new IllegalArgumentException("field " + name + " is a must parameter");
		}
		return value;
	}

	public static LinkedList<Object> getParamJsons(HttpServletRequest req, String name) {
		LinkedList<Object> res = new LinkedList<>();
		LinkedList<String> values = getParams(req, name);
		for (String value : values) {
			res.add(JSON.parse(value, new JSONLongIdCallback()));
		}
		return res;
	}

	public LinkedList<Object> getParamJsons(String name) {
		return getParamJsons(req, name);
	}

	public static Object getParamJson(HttpServletRequest req, String name, Object defValue) {
		LinkedList<Object> res = getParamJsons(req, name);
		return res.isEmpty() ? defValue : res.getLast();
	}

	public Object getParamJson(String name, Object defValue) {
		return getParamJson(req, name, defValue);
	}

	public Object getParamJson(String name) {
		Object value = getParamJson(name, null);
		if (value == null) {
			throw new IllegalArgumentException("field " + name + " is a must parameter");
		}
		return value;
	}

}
