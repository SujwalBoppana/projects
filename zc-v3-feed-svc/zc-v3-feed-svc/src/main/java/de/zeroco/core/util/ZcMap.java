package de.zeroco.core.util;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
 

public class ZcMap extends LinkedHashMap<String, Object> {
	private static Logger log = Logger.getLogger(ZcMap.class.toString());
	
	public static boolean IS_CLIENT_ADMIN=false;
	public static boolean IS_USER=false;
	public static boolean IS_ZEROCODE_ADMIN=false;

	private String keyDelimiter = ".";

	private static final long serialVersionUID = 1903694438088439317L;

	/* Constructors Start */
	public ZcMap() {
		super();
	}

	public ZcMap(String keyDelimiter) {
		super();
		this.keyDelimiter = keyDelimiter;
	}

	public ZcMap(Map<String, Object> x) {
		super();
		if(x!=null)	putAll(ZcUtil.jsonToZcMap(ZcUtil.toJson(x)));
	}
	
	public ZcMap(Object x) {
		super();
		if(x!=null)	putAll(ZcUtil.jsonToZcMap(ZcUtil.toJson(x)));
	}

	public void putAllCustom(Map<String, Object> x) {
		if (x == null)
			return;
		ZcMap curObj = this;
		x.forEach((k, v) -> {
			curObj.put(k, v);
		});
	}

	public ZcMap(Object[][] data) {
		this(data, false);
	}

	public ZcMap(Object[][] data, boolean custom) {
		this(data, custom, null);
	}
	@Override
	/*
	 * Overriding finalize method to check which object is garbage collected
	 */
	protected void finalize() throws Throwable {
		// will print name of object
		// System.out.println(this.getClass().getName() + " successfully garbage collected (ZcMap)");
		// System.gc();
	}
    public boolean checkDataBetween(String listKey,String start,String to) {
    	List<ZcMap> list=getZcMapList(listKey);
    	return ZcUtil.checkDataBetween(list, start, to,new Date());
    } 
	public String formatDate(String k,String f,boolean custom) {
		Date d=date(k,custom);
		if(d==null) return null;
		else return new SimpleDateFormat(f).format(d);
	}
	public String formatDate(String k,String f) {
		return formatDate(k,f,true);
	}
	public String formatDate(String k,String sf,String df) {
		return new SimpleDateFormat(df).format(ZcUtil.getDate(getS(k), sf));
	}
	public static String DATE_TIME_FORMAT="yyyy-MM-dd HH:mm:ss";
	public String formatDate(String f) {
		Date d=new Date();
		return new SimpleDateFormat(f).format(d);
	}
	public String formatTime(String k,String f,boolean custom) {
		Date d=time(k,custom);
		if(d==null) return null;
		else return new SimpleDateFormat(f).format(d);
	}
	public String formatTime(String k,String f) {
		return formatTime(k,f,true);
	}
	public String formatDateTime(String k,String f,boolean custom) {
		Date d=dateTime(k,custom);
		if(d==null) return null;
		else return new SimpleDateFormat(f).format(d);
	}
	public String formatDateTime(String k,String f) {
		return formatDateTime(k,f,true);
	}	
	public Date date(String k,boolean custom) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse(getS(k,custom));
		} catch (ParseException e) {
			return null;
		}
	}
	public Date date(String k) {
		return date(k,true);
	}
	public Date time(String k,boolean custom) {
		try {
			return new SimpleDateFormat("HH:mm:ss").parse(getS(k,custom));
		} catch (ParseException e) {
			return null;
		}
	}
	public Date time(String k) {
		return date(k,true);
	}
	public Date dateTime(String k,boolean custom) {
		try {
			return new SimpleDateFormat(DATE_TIME_FORMAT).parse(getS(k,custom));
		} catch (ParseException e) {
			return null;
		}
	}
	public Date dateTime(String k) {
		return date(k,true);
	}
	public ZcMap(Object[][] data, boolean custom, String keyDelimiter) {
		super();
		if (keyDelimiter != null)
			this.keyDelimiter = keyDelimiter;
		for (Object[] o : data) {
			if (custom) {
				put((String) o[0], o[1]);
			} else {
				put((String) o[0], o[1]);
			}
		}
	}
	/* Constructors End */

	/* Utility Methods Start */

	public ZcMap getZcMap(String k, boolean custom) {
		if (get(k, custom) == null) {
			return new ZcMap();
		} else if (get(k, custom) instanceof String) {
			if (isBlank(k, custom)) {
				return new ZcMap();
			} else {
				String value = get(k, custom).toString();
				if (value.trim().charAt(0) == '{') {
					try {
						return new ObjectMapper().reader().forType(ZcMap.class).readValue(value);
					} catch (IOException e) {
						log.info(ZcUtil.getStackTrace(e));
						return new ZcMap();
					}
				} else {
					try {
						return (new Yaml()).loadAs(value, ZcMap.class);
					}catch (Exception e) {
						log.info(getS("code"));
						log.info("Error While Converting to ZcMap");
						log.info(value);
						return new ZcMap();
					}
				}
			}
		} else if (get(k, custom) instanceof ZcMap) {
			return (ZcMap) get(k, custom);
		} else {
			// return new ObjectMapper().convertValue(get(k, custom), ZcMap.class);
			//*
			  ZcMap _vl=new ObjectMapper().convertValue(get(k,custom), ZcMap.class);
			  put(k,_vl); // ,false
			  return _vl; 
			//*/
		}
	}

	@SuppressWarnings("rawtypes")
	public Object getCustom(String k) {
		if (k == null)
			return null;
		if (!k.contains(keyDelimiter))
			return get(k, false);
		else {
			ZcMap obj = this;
			StringTokenizer keyTokens = new StringTokenizer(k, keyDelimiter);
			int i = 0;
			int tokenCount = keyTokens.countTokens();
			while (keyTokens.hasMoreTokens()) {
				String key = keyTokens.nextToken();
				if (i++ == (tokenCount - 1))
					return obj.get(key, false);
				else {
					try {
						Object _o=obj.get(key);
						if(_o instanceof List) {
							if(ZcUtil.isBlank(_o)) return null;
							else {
								List _tl=((List)_o);
								Object _le=_tl.get(_tl.size()-1);
								if(_le instanceof Map) {
									obj=new ZcMap(_le);//(ZcMap)_le
								}else {
									return null;
								}
							}
						}else
							obj = obj.getZcMap(key, false);
					} catch (Exception e) {
						return null;
					}
				}
			}
		}
		return null;
	}
	public static Object[] haveArraySyntax(String k) {
		Object[] res = new Object[] { false, k, -1 };
		if (ZcUtil.isBlank(k))
			return res;
		k = k.trim();
		if (k.endsWith("]") && k.contains("[")) {
			String partKey = k.substring(0, k.indexOf("["));
			String index = k.substring(k.indexOf("[") + 1, k.indexOf("]"));
			int intIndex = -1;
			boolean flg = true;
			if (ZcUtil.isBlank(index)) {
				intIndex = -2;
			}else if("*".equals(index.toString().trim())) {
				intIndex = -3;
			}else if("n".equals(index.toString().trim())) {
				intIndex = -4;
			} else {
				try {
					intIndex = Integer.parseInt(index);
				} catch (NumberFormatException e) {
					flg = false;
					partKey = k;
				}

			}
			res = new Object[] { flg, partKey, intIndex };
		}
		return res;
	} 
	
	public void merge(Map<String,Object> map) {
		merge(map,true, false);
	}
	public void merge(Map<String,Object> map,boolean custom) {
		merge(map,custom, false);
	}
	public void mergeLegacy(Map<String,Object> map) {
		merge(map,true, true);
	}
	public void mergeLegacy(Map<String,Object> map,boolean custom) {
		merge(map,custom, true);
	}
	@SuppressWarnings("unchecked")
	private void merge(Map<String, Object> map,boolean custom, boolean isLegacy) {
		if (map == null)
			return;
		if (isLegacy) {
			for (java.util.Map.Entry<String, Object> obj : map.entrySet()) {
				String x = obj.getKey();
				Object y = obj.getValue();
				if (y instanceof Map) {
					ZcMap z = null;
					if (get(x,custom) instanceof Map) {
						z = getZcMap(x,custom);
					} else {
						z = new ZcMap();
					}
					put(x, z,custom);
					z.merge((Map<String, Object>) y,custom, isLegacy);
				} else {
					put(x, y,custom);
				}
			}
		} else {
			map.forEach((x, y) -> {
				if (y instanceof Map) {
					ZcMap z = null;
					if (get(x,custom) instanceof Map) {
						z = getZcMap(x,custom);
					} else {
						z = new ZcMap();
					}
					put(x, z,custom);
					z.merge((Map<String, Object>) y,custom, false);
				} else {
					put(x, y,custom);
				}
			});
		}
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object getValue(String k) throws Exception {
		// System.out.println(k);
		if (k == null) {
			return super.get(k);
		} else {
			Object[] data = haveArraySyntax(k);
			// if(true) return super.get(k);
			if (!(Boolean) data[0])
				return super.get(k);
			else {
				String partKey = (String) data[1];
				Object li = get(partKey);
				if (li == null) {
					return null;
				} else if (li instanceof List) {
					List<Object> getLi = (List) li;
					int index = (int) data[2];
					if (index == -2)
						index = getLi.size() - 1;
					else if(index == -3)
						return getLi;
					if (index < getLi.size())
						return getLi.get(index);
					else
						return null;
				} else {
					return null;
				}
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object putValue(String k, Object v) throws Exception {
		if (k == null) {
			return super.put(k, v);
		} else {
			Object[] data = haveArraySyntax(k);
			if (!(Boolean) data[0])
				return super.put(k, v);
			else {
				String partKey = (String) data[1];
				Object li = get(partKey);
				if (li instanceof List || li == null) {
					List<Object> putLi = null;
					if (li == null)
						putLi = new ArrayList<>();
					else
						putLi = (List<Object>) li;
					int index = (int) data[2];
					if (index == -2) {
						index = putLi.size();
						if(index>0)index=index-1;
					}else if(index==-3) {
						for(int _lpIdx=0;_lpIdx<putLi.size();_lpIdx++) {
							Object ed=putLi.get(_lpIdx);
							if (ed instanceof Map) {
								ZcMap putVal = new ZcMap();
								putVal.putAll((Map) ed);
								putVal.putAll((Map) v);
								putLi.set(_lpIdx, putVal);
							} else {
								putLi.set(index, new ZcMap((Map) v));
							}
						}
						super.put(partKey, li);
						return li;
					}if (index == -4) {
						index = putLi.size();
						//if(index>0)index=index-1;
					}
					for (int i = putLi.size(); i <= index; i++) {
						putLi.add(null);
					}
					if (v instanceof Map) {
						Object ed = putLi.get(index);
						if (ed == null)
							putLi.set(index, new ZcMap((Map) v));
						else {
							if (ed instanceof Map) {
								ZcMap putVal = new ZcMap();
								putVal.putAll((Map) ed);
								putVal.putAll((Map) v);
								putLi.set(index, putVal);
							} else {
								putLi.set(index, new ZcMap((Map) v));
							}
						}
					} else {
						putLi.set(index, v);
					}
					li = putLi;
					super.put(partKey, li);
				} else {
					log.info("1-----------");
					log.info(partKey);
					log.info(li==null?"null":li.toString());
					log.info(this.toJson());
					log.info("-----------");
					throw new Exception("Sorry, you can't put it here");
				}
				return li;
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object putCustom(String key, Object value) throws Exception {
		if (key == null || !key.contains(keyDelimiter))
			return putValue(key, value);
		else {
			ZcMap obj = this;
			StringTokenizer keyTokens = new StringTokenizer(key, keyDelimiter);
			int i = 0;
			int tokenCount = keyTokens.countTokens();
			while (keyTokens.hasMoreTokens()) {
				String k = keyTokens.nextToken();
				if (i++ == (tokenCount - 1)) {
					if (!k.toLowerCase().endsWith("setting")) {
						//if (obj.get(k, false) instanceof Map) {// && !(value instanceof Map)) {
						//	throw new Exception("Sorry, you can't put it here");
						//}
						return obj.put(k, value, false);
					} else {
						if (value != null && value instanceof ZcMap)
							try {
								return obj.put(k, value, false);
							} catch (Exception e) {
								return obj.put(k, null, false);
							}
						else if (value == null || (value instanceof Map || !(value instanceof String)))
							return put(key, new ZcMap(), false);
						else {
							try {
								return put(key, ZcUtil.jsonToZcMap(value == null ? null : value.toString()), false);
							} catch (Exception e) {
								return put(key, null, false);
							}
						}
					}
				} else {
					if (!obj.containsKey(k)) {
						if (k.endsWith("[*]")) {
							k = k.substring(0, k.length() - 3);
							Object _o = obj.get(k);
							if (_o == null) return null;
							if (_o instanceof List) {
								String spKey="";
								while (keyTokens.hasMoreTokens()) {
									spKey = (ZcUtil.isBlank(spKey) ? "" : spKey + keyDelimiter) + keyTokens.nextToken();
								}
								List _ooLi=(List)_o;
								for(int _ooIdx=0;_ooIdx<_ooLi.size();_ooIdx++) {
									Object _oo=_ooLi.get(_ooIdx);
									if(_oo instanceof Map) {
										ZcMap _obj=new ZcMap((Map)_oo);
										_obj.put(spKey, value);
										_ooLi.set(_ooIdx, _obj);
									}else {
										log.info("2-----------");
										log.info(spKey);
										log.info(_oo==null?"null":_oo.toString());
										log.info(this.toJson()); 
										log.info("-----------");
										throw new Exception("Sorry, you can't put it here");
									}
								}
							}
							obj.put(k,_o);
							return _o;
						}else {
							obj.put(k, new ZcMap(), false);
							obj = obj.getZcMap(k, false);
						}
					} else if (!(obj.get(k, false) instanceof Map)) {
						if(obj.get(k)== null && keyTokens.hasMoreTokens()) {
							obj.put(k, new ZcMap(), false);
							obj = obj.getZcMap(k, false);
						}
					} else {
						obj = obj.getZcMap(k, false);
					}
				}
			}
		}
		return put(key, new ZcMap(), false);
	}
	public static void main(String[] args)throws Exception {
		ZcMap res=new ZcMap();
		res.putCustom("status",null );
		res.putCustom("status.uid.uid", "test");
		System.out.println(res);
		/*
		 * ZcMap map=new ZcMap(); map.put("a[0].a",1); map.put("a[0].b",4);
		 * map.put("a[0].st.arr[0]",new ZcMap()); map.put("a[0].st.arr[1].a",6);
		 * map.put("a[1].b",4); map.put("a[1].a",6); map.put("a[*].st.uid",0);
		 * map.put("a[*].st.arr[*].uid",0); map.put("myDate","2019-10-22");
		 * map.put("myDateTime","2019-10-22 14:12:33"); map.put("myTime","14:12:33");
		 * //System.out.println(map.toJson()); map.removeKeys("a[*].b");
		 * //System.out.println(map.toJson()); //System.out.println(map.get("a[*]"));
		 * //System.out.println(map.get("a[*].a"));
		 * //System.out.println(Arrays.asList(haveArraySyntax("a[]")));
		 * //System.out.println(map.base64StringQRCodeImage("Jagadish", 200));
		 * System.out.println(map.formatDate("dd-MM-yyyy"));
		 * System.out.println(map.formatDate("myDate","dd-MM-yyyy"));
		 * System.out.println(map.formatDateTime("myDateTime","dd-MM-yyyy hh:mm aa"));
		 * System.out.println(map.formatTime("myTime","hh:mm aa"));
		 */
	}
	public boolean isBlank(String k, boolean custom) {
		return ZcUtil.isBlank(get(k, custom));
	}
	
	public boolean delKeys(String...keys) {
		removeKeys(keys);
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	public void removeKeys(String... keys) {
		for (String s : keys) {
			ZcMap obj=this;
			StringTokenizer keyTokens=new StringTokenizer(s,keyDelimiter);
			int i = 0;
			int tokenCount = keyTokens.countTokens();
			while (keyTokens.hasMoreTokens()) {
				String key = keyTokens.nextToken();
				if (i++ == (tokenCount - 1)) {
					obj.remove(key);
					break;
				}else {
					try {
						Object _o=obj.get(key);
						if(_o instanceof List) {
							if(ZcUtil.isBlank(_o)) break;
							else {
								List _tl=((List)_o);
								Object _le=_tl.get(_tl.size()-1);
								if(_le instanceof Map) {
									obj=new ZcMap(_le); //(ZcMap)_le
								}else {
									break;
								}
							}
						}else
							obj = obj.getZcMap(key, false);
					} catch (Exception e) {
						break;
					}
				}
			} 
		}
	}

	public String getTitleCase(String key, boolean custom) {
		return ZcUtil.toTitleCase(getS(key, custom));
	}

	public String getSentenceCase(String key, boolean custom) {
		return ZcUtil.toSentenceCase(getS(key, custom));
	}

	public String getUpperCase(String key, boolean custom) {
		return get(key, custom) == null ? null : getS(key, custom).toUpperCase();
	}

	public String getLowerCase(String key, boolean custom) {
		return get(key, custom) == null ? null : getS(key, custom).toLowerCase();
	}

	public String getS(String k, boolean custom) {
		return get(k, custom) == null ? null : get(k, custom).toString().trim();
	}

	public Date getDateTime(String k, boolean custom) {
		return ZcUtil.getDateTime(getS(k, custom));
	}
	public Date getDate(String k, boolean custom) {
		return ZcUtil.getDate(getS(k, custom));
	}
	public Date getTime(String k, boolean custom) {
		return ZcUtil.getTime(getS(k, custom));
	}
	public Integer getIo(String k, boolean custom) {
		return ZcUtil.parseInt(get(k, custom));
	}

	public int getI(String k, boolean custom) {
		Integer i = getIo(k, custom);
		return i == null ? 0 : i;
	}
	public Double getDo(String k, boolean custom) {
		return ZcUtil.parseDouble(get(k, custom));
	}
	public String getCur(String k, boolean custom) { 
		return getCur(k,custom, 2, 3, ',', '.', "₹");
	}
	public String getCur(String k, boolean custom,int n,int x,char s,char d,String c) {
		Double i = getDo(k, custom);
		i=i == null ? 0 : i;
		return ZcUtil.formatNumber(i, n, x, s, d, c);
	}
	public String getCur(String k,int n,int x,char s,char d,String c) {
		return getCur(k,true,n,x,s,d,c);
	}	
	public String getCur(String k) {
		return getCur(k,true);
	}
	public String getFmtNum(String k,int n,int x,char s,char d,String c) {
		return getCur(k,true,n,x,s,d,c);
	}	
	public String getFmtNum(String k) {
		return getFmtNum(k,2,3,',','.',"");
	}
	public double getD(String k, boolean custom) {
		Double i = getDo(k, custom);
		return i == null ? 0 : i;
	}
	public Boolean getBo(String k, boolean custom) {
		return ZcUtil.parseBoolean(get(k, custom));
	}

	public boolean getB(String k, boolean custom) {
		Boolean res = getBo(k, custom);
		return res == null ? false : res;
	}

	public String getText(String k, boolean custom) {
		return ZcUtil.truncate(getS(k, custom), 160);
	}

	public String getDesc(String k, boolean custom) {
		return ZcUtil.truncate(getS(k, custom), 1024);
	}

	public final String toJson() {
		return ZcUtil.toJson(this);
	}

	public String toYml() {
		return ZcUtil.toYml(this);
	}

	public String getJson(String k, boolean custom) {
		if (get(k, custom) == null)
			return "{}";
		else
			return ZcUtil.toJson(get(k, custom));
	}

	public String getYml(String k, boolean custom) {
		if (get(k, custom) == null)
			return "";
		else
			return ZcUtil.toYml(get(k, custom));
	}

	@SuppressWarnings("unchecked")
	public List<String> getStringList(String k, boolean custom) {
		Object o = get(k, custom);
		if (o == null)
			return new ArrayList<>();
		else {
			try {
				return (List<String>) o;
			} catch (Exception e) {
				return new ArrayList<>();
			}
		}
	}

	public String[] getStringArray(String k, boolean custom) {
		List<String> list = getStringList(k, custom);
		return list == null ? null : list.toArray(new String[list.size()]);
	}
	
	public List<ZcMap> getZcMapList(String k, boolean custom) {
		Object o = get(k, custom);
		List<ZcMap> li = getZcMapList(o);
		this.put(k, li,custom);
		return li;
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static List<ZcMap> getZcMapList(Object o) {
		List<ZcMap> li = new ArrayList<ZcMap>();
		if (o == null)
			return li;
		List<Object> dataLi=((List<Object>) o);
		for(Object _o:dataLi)
			li.add(new ZcMap((Map)_o));
		return li;
	}
	/* Utility Methods End */

	/* Setters and Getters Start */
	public void setKeyDelimiter(String keyDelimiter) {
		this.keyDelimiter = keyDelimiter;
	}

	public String getKeyDelimiter() {
		return keyDelimiter;
	}
	/* Setters and Getters End */

	/* Over written Methods Start */
	public Object get(String key, boolean custom) {
		if (!custom)
			try {
				return getValue(key);
			} catch (Exception e) {
				log.info(ZcUtil.getStackTrace(e));
				return null;
			}
		else
			return getCustom(key);
	}

	public ZcMap put(String key, Object value, boolean custom) { 
		if (!custom) {
			try {
				putValue(key, value);
			} catch (Exception e) {
				log.info(ZcUtil.getStackTrace(e));
			}
		} else {
			try {
				putCustom(key, value);
			} catch (Exception e) {
				log.info(ZcUtil.getStackTrace(e));
			}
		}
		return this;
	}

	@Override
	public ZcMap clone() {
		 return (ZcMap) super.clone();
		// return ZcUtil.jsonToZcMap(this.toJson());
	}
	public ZcMap append(String key,String value) {
		if(get(key)==null) put(key,""); 
		if(get(key) instanceof String) {
			put(key,isBlank(key)?value:(getS(key)+", "+value)); 
		}
		return this;
	}
	/* Over written Methods End */
	public ZcMap put(String key, Object value) {
		put(key, value, true);
		return this;
	}

	public ZcMap getZcMap(String k) {
		return getZcMap(k, true);
	}

	public boolean isBlank(String k) {
		return isBlank(k, true);
	}
	
	public boolean hasData(String k) {
		return hasData(k, true);
	}
	
	public boolean hasData(String k, boolean custom) {
		return !ZcUtil.isBlank(get(k, custom));
	}
	public Object get(String key) {
		return get(key, true);
	}

	public String getTitleCase(String key) {
		return getTitleCase(key, true);
	}

	public String getSentenceCase(String key) {
		return getSentenceCase(key, true);
	}

	public String getUpperCase(String key) {
		return getUpperCase(key, true);
	}

	public String getLowerCase(String key) {
		return getLowerCase(key, true);
	}
	public String getEnv() {
		return getS("env");
	}
	public String getS(String k) {
		return getS(k, true);
	}
	public String getNullIfStrIsBlank(String k) {
		String s=getS(k);
		return ZcUtil.isBlank(s)?null:s;
	}
	public String getStrAndTruncate(String k,int len) {
		return ZcUtil.truncate(getS(k),len);
	}
	public String getBlankStrIfNullAndTruncate(String k,int len) {
		return ZcUtil.truncate(getBlankStrIfNull(k),len);
	}	
	public String getBlankStrIfNull(String k) {
		String s=getS(k);
		return s==null?"":s;
	}
	public Date getDateTime(String k) {
		return getDateTime(k, true);
	}
	public Date getDate(String k) {
		return getDate(k, true);
	}
	public Date getDateByAddingDays(String k,int no) {
		Date date= getDate(k, true);
		return ZcUtil.addMinutesToDate(no*(60*24), date);
	}
	public Date getTime(String k) {
		return getTime(k, true);
	}
	public Integer getIo(String k) {
		return getIo(k, true);
	}

	public int getI(String k) {
		return getI(k, true);
	}

	public double getD(String k) {
		return getD(k, true);
	}

	public Double getDo(String k) {
		return getDo(k, true);
	}
	
	public Boolean getBo(String k) {
		return getBo(k, true);
	}

	public boolean getB(String k) {
		return getB(k, true);
	}

	public String getText(String k) {
		return getText(k, true);
	}

	public String getDesc(String k) {
		return getDesc(k, true);
	}

	public String getJson(String k) {
		return getJson(k, true);
	}

	public String getYml(String k) {
		return getYml(k, true);
	}

	public List<String> getStringList(String k) {
		return getStringList(k, true);
	}

	public String[] getStringArray(String k) {
		return getStringArray(k, true);
	}

	public List<ZcMap> getZcMapList(String k) {
		return getZcMapList(k, true);
	}

	public static ZcMap asZcMap(Map<String,Object> x) {
		ZcMap data=new ZcMap();
		data.putAll(x);
		return data;
	}
	
	/**
	 * Returns count of files which are not deleted
	 * 
	 * @param k
	 * @author mahesh.c
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int resourcesCount(String k, boolean custom) {
		Object _o = get(k, custom);
		if (_o == null)
			return 0;
		if (!(_o instanceof List))
			return 0;
		int count = 0;
		for (Object x : (List) _o) {
			if (x == null) {
			} else if (x instanceof Map) {
				if (!(new ZcMap((Map) x)).getB("deleted"))
					count++;
			} else {
				return 0;
			}
		}
		return count;
	}
	
	public int resourcesCount(String k) {
		return resourcesCount(k,true);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean isFileSizeExceededLimit(String k, long limit) {
		Object _o = get(k);
		if (_o instanceof List) {
			for (Object x : (List) _o) {
				if (x instanceof Map) {
					if ((new ZcMap((Map) x)).getI("size") > limit)
						return true;
				} 
			}
		}
		return false;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean isFileUnsupportedType(String k, List supportedTypes) {
		List<Object> types = new ArrayList<Object>();
		for(Object type : supportedTypes)  types.add(type.toString().toLowerCase());
		Object _o = get(k);
		if (_o instanceof List) {
			for (Object x : (List) _o) {
				if (x instanceof Map) {
					String fileName = (new ZcMap((Map) x)).getS("name");
					String [] fileSplit = fileName.split("\\.");
					if(!types.contains(fileSplit[fileSplit.length - 1].toLowerCase()))
						return true;
				} 
			}
		}
		return false;
	}
	
	public int sumInt(String listKey,String fieldKey,Object[]...conds) {
		return sumInt(listKey, fieldKey,null,true,conds);
	}
	public int sumInt(String listKey,String fieldKey,String fieldKeyIfNull,Object[]...conds) {
		return sumInt(listKey, fieldKey,fieldKeyIfNull,true,conds);
	}
	public int sumInt(String listKey,String fieldKey,boolean custom,Object[]...conds) {
		return sumInt(listKey, fieldKey,null,custom,conds);
	}
	public int sumInt(String listKey,String fieldKey,String fieldKeyIfNull,boolean custom,Object[]...conds) {
		int sum=0;
		for(ZcMap x: getZcMapList(listKey)) {
			if(conds!=null && conds.length>0) {
				for(Object[] data:conds) {
					String k=data[0].toString();
					Object v=data[1];
					if(v!=null && v.equals(x.get(k))) {
						int _ov=x.getI(fieldKey);
						if(x.get(fieldKey)==null) {
							if(fieldKeyIfNull!=null) _ov=x.getI(fieldKeyIfNull);
						}
						sum+=_ov;
					}
				}
			}else {
				sum+=x.getI(fieldKey);
			}
		}
		return sum;
	}
	public double sumDouble(String listKey,String fieldKey,Object[]...conds) {
		return sumDouble(listKey, fieldKey,null,true,conds);
	}
	public double sumDouble(String listKey,String fieldKey,String fieldKeyIfNull,Object[]...conds) {
		return sumDouble(listKey, fieldKey,fieldKeyIfNull,true,conds);
	}
	public double sumDouble(String listKey,String fieldKey,boolean custom,Object[]...conds) {
		return sumDouble(listKey, fieldKey,custom, conds);
	}
 	
	public double sumDouble(String listKey,String fieldKey,String fieldKeyIfNull,boolean custom,Object[]...conds) {
		double sum=0;
		for(ZcMap x: getZcMapList(listKey)) {
			if(conds!=null && conds.length>0) {
				for(Object[] data:conds) {
					String k=data[0].toString();
					Object v=data[1];
					if(v!=null && v.equals(x.get(k))) {
						Double d=0d;						
						if(x.get(fieldKey)==null) {
							if(fieldKeyIfNull!=null && x.get(fieldKeyIfNull)!=null) {
								d=ZcUtil.parseDouble(x.get(fieldKeyIfNull));
							}
						}else {
							d=ZcUtil.parseDouble(x.get(fieldKey));
						}
						sum+=d;
						break;
					}
				}
			}else {
				Double d = 0d;
				if (x.get(fieldKey) == null || x.getD(fieldKey) == 0d) {
					if (fieldKeyIfNull != null && x.get(fieldKeyIfNull) != null) {
						d = ZcUtil.parseDouble(x.get(fieldKeyIfNull));
					}
				}else {
					d=ZcUtil.parseDouble(x.get(fieldKey));
				}
				sum += (d == null || d == 0 ) ? 0 : d;
			}
		}
		return sum;
	}	
 
	/**
	 * Returns count of list/ZcMap/Map/String object types
	 * 
	 * @param k
	 * @author mahesh.c
	 * @return
	 */
	public int count(String k, boolean custom) {
		Object o = get(k,custom);
		if (o==null)
			return 0;		
		if (o instanceof Map) {
			return ((Map<?, ?>) o).size();
		} else if (o instanceof List) {
			return ((List<?>) o).size();
		} else {
			return 1;
		}
	}
	
	public int count(String k) {
		return count(k,true);
	}
	public int countErrors() {
		return count("errors",true);
	}
	 
	public String decodeBase64(String k,boolean custom) {
		return new String(Base64.getDecoder().decode(getS(k,custom)));
	}
	public String encodeBase64(String k,boolean custom) {
		return new String(Base64.getEncoder().encode(getS(k,custom).getBytes()));
	}
	public String decodeBase64(String k) {
		return decodeBase64(k,true);
	}
	public String encodeBase64(String k) {
		return encodeBase64(k,true);
	}
	public String code() {
		return getS("code");
	}

	public String cacheKey() {
		return getS("cacheKey");
	}
	public String adminDb() {
		return getS("clientAdminDb");
	} 
	public String db() {
		return getS("clientDb");
	}   
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((keyDelimiter == null) ? 0 : keyDelimiter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ZcMap other = (ZcMap) obj;
		if (keyDelimiter == null) {
			if (other.keyDelimiter != null)
				return false;
		} else if (!keyDelimiter.equals(other.keyDelimiter))
			return false;
		return true;
	} 
	
	public String getSl(String k) {
		String val = getS(k, true);
		return val==null?null:val.toLowerCase();
	}
	public String getSl(String k, String defaultValue) {
		String val = getSl(k);
		if(ZcUtil.isBlank(val)) {
			val = defaultValue;
		}
		return val;
	}
	public String getSu(String k) {
		String val = getS(k, true);
		return val==null?null:val.toUpperCase();
	}
}
