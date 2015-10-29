package net.qing.sms.simulator;


import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.type.TypeReference;
import eet.evar.tool.logger.Logger;
import eet.evar.tool.logger.LoggerFactory;

import org.apache.commons.beanutils.BeanUtils;

/**
 * 类说明：json帮助类
 */
public class JacksonUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(JacksonUtils.class);
	
	private static String[] formats = new String[]{"yyyyMMddHHmmss", "yyyyMMdd"};
	private static ObjectMapper  mapper = new ObjectMapper();
	static {		
		// 忽略在JSON字符串中存在但Java对象实际没有的属性
		 mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
	}
	
	/**
	 * 将Json格式的字符串转换成指定的对象返回 。
	 * @param jsonString Json格式的字符串 。如：{'guId':'1', 'name' : '张三'}
	 * @param beanClass 需要转换的对象的Class对象。
	 * @return  转换后的对象。
	 */
	public static Object json2Object(String jsonString, Class beanClass) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setDateFormat(new SimpleDateFormat(formats[0]));
		try {
			return mapper.readValue(jsonString, beanClass);
		} catch (Exception e) {
			logger.error("解析json：" + jsonString + ", To " + beanClass.getName() + " 出错！", e);
			return null;
		}
	}
	
	/**
	 * 将Json格式的字符串转换成指定对象组成的List返回。
	 * @param jsonString Json格式的字符串 。如：[{'guId':'1', 'name' : '张三'}, {'guId':'2', 'name' : '李四'}]
	 * @param beanClass 需要转换的对象的Class对象。
	 * @return List 指定对象组成的List。
	 */
	public static List json2List(String jsonString, Class beanClass) {
		mapper.setDateFormat(new SimpleDateFormat(formats[0]));
		List result = new ArrayList();
		try {
			List<LinkedHashMap<String, Object>> list = mapper.readValue(jsonString, new TypeReference<List>() {});
			for (int i = 0; i < list.size(); i++) {
				LinkedHashMap<String, Object> map = list.get(i);
				Object obj = beanClass.newInstance();
				BeanUtils.populate(obj, map);
				result.add(obj);
	        }
			return result;
		} catch (Exception e) {
			logger.error("解析json：" + jsonString + ", To list<" + beanClass.getName() + "> 出错！", e);
			return new ArrayList();
		}
	}
	
	 /**
     * 将一个实体类对象转换成Json数据格式
     * @param bean 需要转换的实体类对象
     * @return 转换后的Json格式字符串
     */
    public static String beanToJson(Object bean) {
    	if(bean == null) {
    		return nullToJson();
    	}
        StringBuilder json = new StringBuilder();
        json.append("{");
        PropertyDescriptor[] props = null;
        try {
            props = Introspector.getBeanInfo(bean.getClass(), Object.class)
                    .getPropertyDescriptors();
        } catch (IntrospectionException e) {
        }
        if (props != null) {
            for (int i = 0; i < props.length; i++) {
                try {
                    String name = objectToJson(props[i].getName());
                    String value = objectToJson(props[i].getReadMethod()
                            .invoke(bean));
                    json.append(name);
                    json.append(":");
                    json.append(value);
                    json.append(",");
                } catch (Exception e) {
                }
            }
            json.setCharAt(json.length() - 1, '}');
        } else {
            json.append("}");
        }
        return json.toString();
    }
    
	/**
     * 将一个实体类对象转换成Json数据格式
     * @param bean 需要转换的实体类对象
     * @return 转换后的Json格式字符串
     */
    public static String beanToJson2(Object bean) {
        StringBuilder json = new StringBuilder();
        PropertyDescriptor[] props = null;
        try {
            props = Introspector.getBeanInfo(bean.getClass(), Object.class)
                    .getPropertyDescriptors();
            if (props != null) {
                for (int i = 0; i < props.length; i++) {
                    try {
                        String name = objectToJson(props[i].getName());
                        if("\"attributes\"".equals(name)){
                        	continue;
                        }
                        String value = objectToJson(props[i].getReadMethod()
                                .invoke(bean));
                        json.append(name);
                        json.append(":");
                        json.append(value);
                        json.append(",");
                    } catch (Exception e) {
                    }
                }
                json.setCharAt(json.length() - 1, ' ');
            } 
        } catch (IntrospectionException e) {
        	throw new RuntimeException("对象转json异常");
        } catch (Exception e){
        	throw new RuntimeException("对象转json异常");
        }
        return json.toString();
    }
    
    /**
     * 将一个List对象转换成Json数据格式返回
     * @param list 需要进行转换的List对象
     * @return 转换后的Json数据格式字符串
     */
    public static String listToJson(List<?> list) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        if (list != null && list.size() > 0) {
            for (Object obj : list) {
                json.append(objectToJson(obj));
                json.append(",");
            }
            json.setCharAt(json.length() - 1, ']');
        } else {
            json.append("]");
        }
        return json.toString();
    }

    /**
     * 将一个List对象转换成Json数据格式返回
     * @param list List<Object[]>
     * @param keyNames String[] json数据值所对应的键名数组，键名数组要和Object[]中的值一一对应好
     * @return String json格式的字符串 如：
     * [{"areaCode":"0573","dailyRoundCount":6},{"areaCode":"0572","dailyRoundCount":2}]
     */
    public static String listToJson(List<Object[]> list, String[] keyNames) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        if (list != null && list.size() > 0) {
            for (Object[] ary : list) {
            	json.append("{");
            	if(ary != null && ary.length > 0) {
            		for(int i=0; i<ary.length; i++) {
            			json.append("\"" + keyNames[i] + "\"");
            			json.append(":");
                		json.append(objectToJson(ary[i]));
                		json.append(",");
            		}
            		json.setCharAt(json.length() - 1, '}');
            		json.append(",");
            	} else {
            		json.append("}");
            	}
            } //结束 for (Object[] ary : list) {
            json.setCharAt(json.length() - 1, ']');
        } else {
            json.append("]");
        }
        return json.toString();
    }
    
    /**
     * 将一个Set对象转换成Json数据格式返回
     * @param set 需要进行转换的Set对象
     * @return 转换后的Json数据格式字符串
     */
    public static String setToJson(Set<?> set) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        if (set != null && set.size() > 0) {
            for (Object obj : set) {
                json.append(objectToJson(obj));
                json.append(",");
            }
            json.setCharAt(json.length() - 1, ']');
        } else {
            json.append("]");
        }
        return json.toString();
    }
    
    /**
     * 将一个对象数组转换成Json数据格式返回
     * @param array 需要进行转换的数组对象
     * @return 转换后的Json数据格式字符串
     */
    public static String arrayToJson(Object[] array) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        if (array != null && array.length > 0) {
            for (Object obj : array) {
                json.append(objectToJson(obj));
                json.append(",");
            }
            json.setCharAt(json.length() - 1, ']');
        } else {
            json.append("]");
        }
        return json.toString();
    }

    /**
     * 将一个Map对象转换成Json数据格式返回
     * @param map 需要进行转换的Map对象
     * @return 转换后的Json数据格式字符串
     */
    public static String mapToJson(Map<?, ?> map) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        if (map != null && map.size() > 0) {
            for (Object key : map.keySet()) {
                json.append(objectToJson(key));
                json.append(":");
                json.append(objectToJson(map.get(key)));
                json.append(",");
            }
            json.setCharAt(json.length() - 1, '}');
        } else {
            json.append("}");
        }
        return json.toString();
    }

    /**
     * 将一个Map对象转换成Json数据格式返回
     * @param map 需要进行转换的Map对象
     * @param keyName json数据值所对应的键名，map的键作为json数据的值
     * @param valueName json数据值所对应的键名，map的值作为json数据的值
     * @return String json格式的字符串 如：
     * [
     *  {"operator":"3","segment":["137","138"]},
     *  {"operator":"2","segment":["180","189"]},
     *  {"operator":"1","segment":["130","131"]}
     * ]
     */
    public static String mapToJson(Map<?, ?> map, String keyName, String valueName) {
    	 StringBuilder json = new StringBuilder();
    	 json.append("[");
         if (map != null && map.size() > 0) {
             for (Object key : map.keySet()) {
            	 json.append("{");
            	 json.append("\"" + keyName + "\"");
                 json.append(":");
                 json.append(objectToJson(key));
                 json.append(",");
                 json.append("\"" + valueName + "\"");
                 json.append(":");
                 json.append(objectToJson(map.get(key)));
                 json.append("}");
                 json.append(",");
             }
             json.setCharAt(json.length() - 1, ']');
         } else {
             json.append("]");
         }
         return json.toString();
    }
    
    /**
     * 将Number类型值转换成字符串
     * @param number
     * @return String
     */
    private static String numberToJson(Number number) {
        return number.toString();
    }
    
    /**
     * 将Boolean类型值转换成字符串
     * @param bool
     * @return String
     */
    private static String booleanToJson(Boolean bool) {
        return bool.toString();
    }

    private static String nullToJson() {
        return "";
    }

    /**
     * 将String类型的值转换成Json形式的数据
     * @param s String
     * @return String Json形式的数据
     */
    public static String stringToJson(String s) {
        if (s == null) {
            return nullToJson();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
            case '"':
                sb.append("\\\"");
                break;
            case '\\':
                sb.append("\\\\");
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\r':
                sb.append("\\r");
                break;
            case '\t':
                sb.append("\\t");
                break;
            default:
                if (ch >= '\u0000' && ch <= '\u001F') {
                    String ss = Integer.toHexString(ch);
                    sb.append("\\u");
                    for (int k = 0; k < 4 - ss.length(); k++) {
                        sb.append('0');
                    }
                    sb.append(ss.toUpperCase());
                } else {
                    sb.append(ch);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 将简单对象转换成json格式
     * @param key String 键的名字
     * @param value Object 值
     * @return json格式字符串。如：{"requestId" : "12345678"}
     */
    public static String simpleObjToJson(String key, Object value) {
    	String keyValue = stringToJson(key);
    	String jsonValue = objectToJson(value);
    	return "{\"" + keyValue + "\" : " + jsonValue + "}";
    }
    
    /**
     * 将java中的一些对象类型转换成Json格式需要的数据
     * @param obj Object
     * @return String Json格式需要的数据
     */
	public static String objectToJson(Object obj) {
        StringWriter writer = null;
		JsonGenerator gen = null;
		try {
			writer = new StringWriter();
			gen = new JsonFactory().createJsonGenerator(writer);
			mapper.writeValue(gen, obj);
			String json = writer.toString();
			return json;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (gen != null) {
					gen.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }

	public static String[] jsonToArray(String jsonString) 
	throws JsonParseException, JsonMappingException, IOException {
		mapper.setDateFormat(new SimpleDateFormat(formats[0]));
		return mapper.readValue(jsonString, String[].class);
	}
	
	public static void main(String[] args) {
		String jsonString = "{\"11\": \"22\", \"xx\": \"2222\"}";
		try {
			System.out.println(jsonToMap(jsonString));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	public static Map<Object, Object> jsonToMap(String json) {
		mapper.setDateFormat(new SimpleDateFormat(formats[0]));
		try {
			return mapper.readValue(json, Map.class);
		} catch (Exception e) {
			logger.error("解析json错误", e);
			return new HashMap<Object, Object>();
		}
	}
}
