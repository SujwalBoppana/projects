package de.zeroco.core.util;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
 
/**
 * This class contains globally used methods.
 * 
 * @author Jagadeesh T
 *
 */
public class ZcUtil {
	
	public static void main(String[] args) {
//		BCryptPasswordEncoder encode =new BCryptPasswordEncoder();
//		System.out.println(encode.encode("JavaTeam@401"));
		System.out.println(getDiffMins("2023-06-09 11:44:59", "2023-05-30 20:05:12"));
		System.out.println(calculateLastUpdatedDateTime("2023-06-09 12:11:26", 13926));
	}
	
	public static BCryptPasswordEncoder encoder() {
	    return new BCryptPasswordEncoder();
	}

	public static String getBlankStrIfNull(String str) {
		return str == null ? "" : str;
	}
	public static Double getRoundValue(double value, int position) {
		if (position < 0) return 0d;
		return parseDouble(String.format("%." + position + "f", value));
	}

	public static Double getRoundValue(double value) {
		return getRoundValue(value, 2);
	}

	public static double getFloorValue(double num) {
		return isBlank(num) ? 0d : Math.floor(num);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ZcMap getZcMapWithObject(Object obj) {
		if(obj!=null && obj instanceof Map) {
			return new ZcMap((Map)obj);
		}else {
			return new ZcMap();
		}
	}	
	
	public static String getConcatatenatedStringFromList(List<ZcMap> rows,String key) {
		return rows.stream().map(x->x.getS(key)).collect(Collectors.joining(","));
	}
	
	public static boolean isNumeric(Object num) {
	    if (num == null) {
	        return false;
	    }
	    try {
	        Double.parseDouble(num.toString());
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
	
	public static String getHostName(String url) throws URISyntaxException {
	    URI uri = new URI(url);
	    String hostname = uri.getHost();
	    // to provide faultproof result, check if not null then return only hostname, without www.
	    if (hostname != null) {
	        return hostname.startsWith("www.") ? hostname.substring(4) : hostname;
	    }
	    return hostname;
	}
	public static void putException(ZcMap map,Exception e) {
		putException(map, e, "Exception");
	}
	public static void putException(ZcMap map,Exception e,String key) {
		if(map==null || e==null || key==null) return;
		if(e.getCause()==null) {
			map.put(key,e.getMessage());
		}else {
			map.put(key,e.getCause().getMessage());
		}
	}
   
	public static String decodeBase64(String s) {
		return new String(Base64.getDecoder().decode(s));
	}
	public static String encodeBase64(String s) {
		return new String(Base64.getEncoder().encode(s.getBytes()));
	}
	
	public static List<String> getStringList(String str) {
		return getStringList(str, ":");
	}
	
	public static List<String> getStringList(String str,String seperator) {
		if(isBlank(str)) return new ArrayList<String>();
		return Arrays.asList(str.split(seperator));
	}
	
	public static String getIp() throws Exception {
		return InetAddress.getLocalHost().getHostAddress();
	}
 
	public static String getStackTrace(Exception e) {
		if(e==null) return "";
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void removeBlanks(Map map) {
		if (map == null || map.isEmpty())
			return;
		List<Object> keys = new ArrayList<>();
		for (Object s : map.keySet())
			keys.add(s);
		for (Object s : keys) {
			Object o = map.get(s);
			if (o instanceof Map) {
				removeBlanks((Map) o);
			}else if(o instanceof List) {
				List li=new ArrayList();
				for(Object x:(List)o) {
					if (x instanceof Map) {
						removeBlanks((Map) x);
					}
					if(ZcUtil.hasData(x)) li.add(x);
				}
				map.put(s,li);
			}// else {
				if(o==null ) map.remove(s);
				else if ((o instanceof Collection<?> ||
					 o instanceof Map<?,?> ||
					 o.getClass().isArray()) 
				&& ZcUtil.isBlank(o)) map.remove(s);
				else if (ZcUtil.isBlank(o.toString())) map.remove(s);
					
			//}
		}
	}
	

	public static String concat(ZcMap map, String seperator, String... keys ) {
		String res= "";
		for(String x:keys) {
			if(hasData(x) && hasData(map.get(x))) {
				res+= ((hasData(res))?seperator:"") + map.get(x);
			}
		}
		return res;
	}
	
	public static String concat(Object...s) {
		String res="";
		for(Object x:s) {
			if(hasData(x)) {
				res+= (hasData(res)?" ":"") + x;
			}
		}
		return res;
	}
	public static String getFileContentAsString(File f)throws Exception{
		/*
		String content="";
		for(String s:Files.readAllLines(Paths.get(f.toURI()))) content+=s;
		return content;
		*/
		return FileUtils.readFileToString(f, StandardCharsets.UTF_8);
	}

	public static ZcMap jsonToZcMap(String s) {
		try {
			return new ObjectMapper().readValue(s == null ? null : s, ZcMap.class);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static List<ZcMap> jsonArrayToZcMapList(String s) {
		try {
			return jsonArrayToZcMap(s).getZcMapList("jsonArray");
		}catch (Exception e) {
			return null;
		}
	}
	
	public static ZcMap jsonArrayToZcMap(String s) {
		try {
			return jsonToZcMap("{\"jsonArray\":"+s+"}");
		}catch (Exception e) {
			return null;
		}
	}
 
	/**
	 * Convenience method to determine if a Object is null or blank
	 * 
	 * @param Object
	 * @return boolean
	 * @author Jagadeesh.T
	 * @since 2017-07-15
	 */
	public static boolean isBlank(Object o) {
		if (o == null)
			return true;
		else if (o instanceof String) {
			if (((String) o).trim().equals(""))
				return true;
		} else if (o instanceof Collection<?>) {
			if (((Collection<?>) o).isEmpty())
				return true;
		} else if (o instanceof Integer) {
			if (((Integer) o) <= 0)
				return true;
		} else if (o instanceof Long) {
			if (((Long) o) <= 0)
				return true;
		} else if (o instanceof Short) {
			if (((Short) o) <= 0)
				return true;
		} else if (o instanceof Byte) {
			if (((Byte) o) <= 0)
				return true;
		} else if (o instanceof Double) {
			if (((Double) o) <= 0)
				return true;
		} else if (o instanceof Float) {
			if (((Float) o) <= 0)
				return true;
		} else if (o instanceof Map<?, ?>) {
			if (((Map<?, ?>) o).isEmpty())
				return true;
		} else if (o.getClass().isArray()) {
			return Array.getLength(o) == 0;
		} else {
			if (o.toString().trim().equals(""))
				return true;
		}
		return false;
	}

	public static boolean hasData(Object o) {
		return !isBlank(o);
	}
	public static ZcMap toZcMap(Map<String,Object> map) {
		ZcMap res=new ZcMap();
		res.putAllCustom(map);
		return res;
	}
	public static String formatDate(String date,String format) {
		try {
			Date d= new SimpleDateFormat("yyyy-MM-dd").parse(date);
			return new SimpleDateFormat(format).format(d);
		}catch (Exception e) {
			return "";
		}
	}
	
	public static String calculateLastUpdatedDateTime(String currrentDateTime, long min) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime b = LocalDateTime.parse(currrentDateTime, formatter);
		LocalDateTime c = b.minusMinutes(min);
		return c.format(formatter);
	}
	public static long getDiffMins(String first, String last) {
		if (isBlank(first)) return 0;
		if (isBlank(last)) last = getFormattedDateTime();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return Duration.between(LocalDateTime.parse(first, formatter), LocalDateTime.parse(last, formatter)).toMinutes();
	}
	public static String formatDateTime(String date,String format) {
		try {
			Date d= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
			return new SimpleDateFormat(format).format(d);
		}catch (Exception e) {
			return "";
		}
	}	
	public static String formatTime(String date,String format) {
		try {
			Date d= new SimpleDateFormat("HH:mm:ss").parse(date);
			return new SimpleDateFormat(format).format(d);
		}catch (Exception e) {
			return "";
		}
	}		
	/**
	 * Converts the given input string to TitleCase, which is to remove underscores
	 * and replace it with spaces and Capitalize the first letter of all words.
	 *
	 * @param input
	 * @return String, Title case of the given input
	 * @author Jagadeesh.T
	 * @since 2018-07-20
	 */
	public static String toTitleCase(String input) {
		if (input == null)
			return null;
		if (input.trim().equals(""))
			return "";
		input = input.replace("_", " ").toLowerCase();
		String result = "";
		char firstChar = input.charAt(0);
		result = result + Character.toUpperCase(firstChar);
		for (int i = 1; i < input.length(); i++) {
			char currentChar = input.charAt(i);
			char previousChar = input.charAt(i - 1);
			if (previousChar == ' ') {
				result = result + Character.toUpperCase(currentChar);
			} else {
				result = result + currentChar;
			}
		}
		return result;
	}

	/**
	 * Converts the given String to pascalCase
	 * 
	 * @param input
	 * @return String, Pascal case of the given input
	 * @author Jagadeesh.T
	 * @since 2018-07-20
	 */
	public static String pascalCase(String input) {
		if (input == null)
			return null;
		if (input.trim().equals(""))
			return "";
		input = toTitleCase(input);
		input = input.replaceAll("\\s+", "");
		char c = (input.charAt(0) + "").toLowerCase().charAt(0);
		return c + input.substring(1);
	}

	public static String toSentenceCase(String input) {
		if (input == null)
			return "";
		input = input.trim();
		StringBuilder titleCase = new StringBuilder();
		boolean nextTitleCase = true;
		boolean isDot = false;
		for (char c : input.toCharArray()) {
			if (Character.isSpaceChar(c)) {
				nextTitleCase = isDot;
			} else if (c == '.') {
				nextTitleCase = isDot = true;
			} else if (nextTitleCase) {
				c = Character.toUpperCase(c);
				nextTitleCase = isDot = false;
			} else {
				c = Character.toLowerCase(c);
				isDot = false;
			}
			titleCase.append(c);
		}
		return titleCase.toString();
	}
	public static String formatData(String dateString,String format) {
		return new SimpleDateFormat(format).format(getDateFromUtcString(dateString));
	}
	public static String formatData(Date date,String format) {
		return new SimpleDateFormat(format).format(date);
	}
	public static String getDateTimeString(java.sql.Timestamp timeStamp) {
		if (timeStamp == null)	return null;
		SimpleDateFormat converter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//converter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return converter.format(timeStamp); 
	}
	public static String getDateString(java.sql.Timestamp timeStamp) {
		if (timeStamp == null)	return null;
		SimpleDateFormat converter=new SimpleDateFormat("yyyy-MM-dd");
		//converter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return converter.format(timeStamp); 
	}
	public static String getDateString(java.sql.Date date) {
		if (date == null)	return null;
		SimpleDateFormat converter=new SimpleDateFormat("yyyy-MM-dd");
		//converter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return converter.format(date); 
	}
	
	
	public static String getDateString(java.util.Date date) {
		return getDateString(date, "yyyy-MM-dd");
	}
	
	public static String getDateString(java.util.Date date, String format) {
		if (date == null)	return null;
		SimpleDateFormat converter=new SimpleDateFormat(format);
		//converter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return converter.format(date); 
	}
	
	public static String getTimeString(java.sql.Time time) {
		if (time == null)	return null;
		SimpleDateFormat converter=new SimpleDateFormat("HH:mm:ss");
		//converter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return converter.format(time); 
	}
	public static String getTimeString(java.sql.Timestamp time) {
		if (time == null)	return null;
		SimpleDateFormat converter=new SimpleDateFormat("HH:mm:ss");
		//converter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return converter.format(time); 
	}
	public static String getTimeString(java.util.Date time,String format) {
		if (time == null)	return null;
		SimpleDateFormat converter=new SimpleDateFormat(format);
		//converter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return converter.format(time); 
	}
	
	
	public static String getTimeString(java.util.Date time) {
		return getTimeString(time,"HH:mm:ss");
	}
	
	public static String getDateTimeString(java.util.Date time, String format) {
		if (time == null)	return null;
		SimpleDateFormat converter=new SimpleDateFormat(format);
		//converter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return converter.format(time); 
	}
	
	public static String getDateTimeString(java.util.Date date) {
		return getDateTimeString(date,"yyyy-MM-dd HH:mm:ss");
	}
	public static Date getDateTime(String date, String format) { 
		return getDate(date,format);
	}
	
	public static Date getDateTime(String date) { 
		return getDate(date,"yyyy-MM-dd HH:mm:ss");
	}
	public static Date getDate(String date,String format) {
		if (date == null)	return null;
		try {
			return simpleDateFormat(format).parse(date);
		} catch (ParseException e) {
			return null;
		}
	}
	public static boolean contains(String element,String[] elements) {
		return Arrays.asList(elements).contains(element);
	}
	
	public static String getFormattedDate() {
		SimpleDateFormat converter = new SimpleDateFormat("yyyy-MM-dd");
		return converter.format(new Date());
	}
	
	public static String getFormattedDate(String format) {
		SimpleDateFormat converter = new SimpleDateFormat(format);
		return converter.format(new Date());
	}

	public static String getFormattedTime() {
		SimpleDateFormat converter = new SimpleDateFormat("HH:mm:ss");
		return converter.format(new Date());
	}

	public static String getFormattedDateTime() {
		SimpleDateFormat converter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return converter.format(new Date());
	}
	
	public static String getFormattedDateTime(Date date) {
		SimpleDateFormat converter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return converter.format(date);
	}
	
	private static SimpleDateFormat simpleDateFormat(String format) {
		SimpleDateFormat sf=new SimpleDateFormat(format);
		sf.setLenient(false);
		return sf;
	}
	public static Date getLastDateOfMonth() { 
		return getLastDateOfMonth(new Date());
	}
	public static Date getFirstDateOfMonth() {
		return getFirstDateOfMonth(new Date());
	}
	public static Date getLastDateOfMonth(Date dt) {
		Calendar calendar = Calendar.getInstance();  
		calendar.setTime(dt);  
		calendar.add(Calendar.MONTH, 1);  
		calendar.set(Calendar.DAY_OF_MONTH, 1);  
		calendar.add(Calendar.DATE, -1);  
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		return calendar.getTime();
	}
	public static Date getFirstDateOfMonth(Date dt) {
		Calendar calendar = Calendar.getInstance();  
		calendar.setTime(dt);  
		calendar.set(Calendar.DAY_OF_MONTH, 1);  
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}
	public static Date getDate(String date) {
		if (date == null)	return null;
		try {
			return simpleDateFormat("yyyy-MM-dd").parse(date);
		} catch (ParseException e) {
			return getDateTime(date);
		}
	}
	
	public static String parse(String value, String format) {
		if (value != null && format != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			Date d;
			try {
				d = sdf.parse(value);
				sdf.applyPattern("yyyy-MM-dd");
				String newDateString = sdf.format(d);
				return newDateString;
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static Date getDateFilterValue(String date) {
		if (date == null)	return null;
		try {
			return simpleDateFormat("yyyy-MM-dd").parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	public static Date getTime(String date) {
		return getTime(date,"HH:mm:ss");
	}
	
	private static boolean isDateTimeInBtwStartAndEnd(String start, String end, String current) throws ParseException {
		Calendar calendarStartTime = Calendar.getInstance();
		calendarStartTime.setTime(getDateTime(start));
		Calendar calendarEndTime = Calendar.getInstance();
		calendarEndTime.setTime(getDateTime(end));
		Calendar calendarTime = Calendar.getInstance();
		calendarTime.setTime(getDateTime(current));
		if (calendarTime.getTime().after(calendarStartTime.getTime())
				&& calendarTime.getTime().before(calendarEndTime.getTime())) {
			return true;
		}
		return false;
	}

	public static boolean isValidDateTime(String date) {
		boolean isValid = false;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setLenient(false);
		try {
			dateFormat.parse(date);
			isValid = true;
		} catch (Exception e) {
			isValid = false;
		}
		return isValid;
	}

	public static boolean isTimeInBtwStartAndEnd(String start, String end, String current) throws ParseException {
		if(isBlank(current)) current = getFormattedTime();
		if (isValidDateTime(current)) {
			return isDateTimeInBtwStartAndEnd(start, end, current);
		}
		Calendar calendarStartTime = Calendar.getInstance();
		calendarStartTime.setTime(getTime(start));
		Calendar calendarEndTime = Calendar.getInstance();
		calendarEndTime.setTime(getTime(end));
		Calendar calendarTime = Calendar.getInstance();
		calendarTime.setTime(getTime(current));
		if (calendarTime.getTime().after(calendarStartTime.getTime())
				&& calendarTime.getTime().before(calendarEndTime.getTime())) {
			return true;
		}
		return false;
	}
	
	public static Date getTime(String date, String format) {
		if (date == null)	return null;
		try {
			return simpleDateFormat(format).parse(date);
		} catch (ParseException e) {
			return null;
		}
	}	
	/**
	 * This method takes String as input and gives UTC formatted date in return
	 * 
	 * @param date
	 * @return UTC formatted date
	 * @throws Exception when the given input String is in not parsable to UTC date
	 *                   then it will through exception
	 * @author Jagadeesh.T
	 * @since 2018-07-20
	 */
	public static Date getDateFromUtcString(String date) {
		if (date == null)
			return null;
		return Date.from(Instant.parse(date));
	}

	/**
	 * This method takes Date as input and gives UTC formatted date in return
	 * 
	 * @param date
	 * @return UTC formatted date
	 * @author Jagadeesh.T
	 * @since 2018-07-20
	 */
	public static String getUtcStringFromDate(Date date) {
		if (date == null)
			return null;
		return date.toInstant().toString();
	}

	public static Date getCurrentDate() {
		return new Date();
	}
	
	/**
	 * Converts the requested obj to JSON String
	 * 
	 * @param obj
	 * @return JSON String
	 * @author Jagadeesh.T
	 * @since 2018-07-20
	 */
	public static String toJson(Object obj, boolean compress) {
		try {
			ObjectMapper om = new ObjectMapper();
			// om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
			if (compress) {
				return om.writer().writeValueAsString(obj);
			} else {
				return om.writer().withDefaultPrettyPrinter().writeValueAsString(obj);
			}
		} catch (JsonProcessingException e) {
			//log.error(e);
			return null;
		}
	}

	public static String toJson(Object obj) {
		return toJson(obj, true);
	}

	public static String toYml(Object obj) {
		if (obj == null || isBlank(obj))
			return "";
		if (obj instanceof String) {
			return (new Yaml()).dumpAsMap(jsonToZcMap((String) obj));
		} else
			return (new Yaml()).dumpAsMap(jsonToZcMap(toJson(obj)));
	}

	public static String truncate(String s, int len) {
		if(isBlank(len)) len = s.length();
		return s == null ? null : (s.length() <= len ? s : s.substring(0, len));
	}

	/**
	 * Given a <code>String</code>, replaces all occurrences of a given
	 * <code>String</code> with the new <code>String</code>.
	 * 
	 * @param String - Original string
	 * @param String - String to replace
	 * @param String - Value to replace with
	 * 
	 * @return String
	 * @author Jagadeesh.T
	 * @since 2018-07-20
	 */
	public static String replace(String sOriginal, String sReplaceThis, String sWithThis) {
		int start = 0;
		int end = 0;
		StringBuffer sbReturn = new StringBuffer();
		if (sOriginal != null) {
			while ((end = sOriginal.indexOf(sReplaceThis, start)) >= 0) {
				sbReturn.append(sOriginal.substring(start, end));
				sbReturn.append(sWithThis);
				start = end + sReplaceThis.length();
			}
			sbReturn.append(sOriginal.substring(start));
		} else {
			sbReturn.append("");
		}
		return (sbReturn.toString());
	}

	/**
	 * Sorting order ASC or DESC will be formated by the given input
	 * 
	 * @param key
	 * @param msgType
	 * @param lang
	 * @return String
	 * @throws Exception
	 * @author Jagadeesh.T
	 * @since 2018-07-20
	 */
	public static String formatSord(String key){
		if (isBlank(key))
			return "DESC";
		key = key.toUpperCase();
		if (!"DESC".equals(key) && !"ASC".equals(key))
			key = "DESC";
		return key;
	}

	/* Parsings Start */
	/**
	 * Parses the given object to Specified Object type
	 * 
	 * @param o
	 * @return Specified Object
	 * @author Jagadeesh.T
	 * @since 2018-07-20
	 */
	public static Byte parseByte(Object o) {
		if (o == null)
			return null;
		if (isBlank(o)) {
			return 0;
		} else {
			try {
				return Byte.valueOf(o.toString().trim());
			} catch (NumberFormatException e) {
				return 0;
			}
		}
	}

	public static Short parseShort(Object o) {
		if (o == null)
			return null;
		if (isBlank(o)) {
			return 0;
		} else {
			try {
				return Short.valueOf(o.toString().trim());
			} catch (NumberFormatException e) {
				return 0;
			}
		}
	}

	public static Integer parseInt(Object o) {
		if (o == null)
			return null;
		if (isBlank(o)) {
			return 0;
		} else {
			try {
				return (int)Double.parseDouble(o.toString().trim());
			} catch (NumberFormatException e) {
				//log.info(ZcUtil.getStackTrace(e));
				return 0;
			}
		}
	}

	public static Long parseLong(Object o) {
		if (o == null)
			return null;
		if (isBlank(o)) {
			return 0l;
		} else {
			try {
				return Long.valueOf(o.toString().trim());
			} catch (NumberFormatException e) {
				return 0l;
			}
		}
	}

	public static Float parseFloat(Object o) {
		if (o == null)
			return null;
		if (isBlank(o)) {
			return 0f;
		} else {
			try {
				return Float.valueOf(o.toString().trim());
			} catch (NumberFormatException e) {
				return 0f;
			}
		}
	}

	public static Double parseDouble(Object o) {
		if (o == null)
			return null;
		if (isBlank(o)) {
			return 0d;
		} else {
			try {
				return Double.valueOf(o.toString().trim());
			} catch (NumberFormatException e) {
				return 0d;
			}
		}
	}

	public static Boolean parseBoolean(Object o) {
		if (o == null)
			return false;
		if (isBlank(o)) {
			return false;
		} else {
			try {
				return Boolean.valueOf(o.toString().trim()) || "1".equals(o.toString().trim());
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}

	/**
	 * List's all the children nodes of the given map from the given list of ZcMap's
	 * and Arranges them in an order and inserts Children into the given map
	 * 
	 * @param map
	 * @param actList
	 * @author Jagadeesh.T
	 * @since 2018-07-20
	 */
	public static void arrangeTree(ZcMap map, List<ZcMap> actList, String uidKey, String parentKey) {
		List<ZcMap> childs = actList.stream()
				.filter(x -> map.hasData(uidKey) && map.getS(uidKey).equals(x.getS(parentKey)))
				.collect(Collectors.toList());
		childs.forEach(x -> {
			arrangeTree(x, actList, uidKey, parentKey);
		});
		map.put("childs", childs);
	}
	
	public static void arrangeTreeForAdmin(ZcMap map, List<ZcMap> actList, String uidKey, String parentKey) {
		List<ZcMap> childs = new ArrayList<ZcMap>();
		for (ZcMap data : actList) {
			if(map.equals(data))
				continue;
			if(map.getS("uid").equals(data.getS(parentKey))) 
				childs.add(data);
		}
		actList.removeAll(childs);
		childs.forEach(x -> {
			arrangeTreeForAdmin(x, actList, uidKey, parentKey);
		});
		map.put("childs", childs);
	}
	
	/**
	 * 
	 * @param minutes
	 * @param beforeTime
	 * @return
	 * @author Jagadeesh.T
	 * @since 2018-07-20
	 */
	public static Date addMinutesToDate(int minutes, Date beforeTime) {
		final long ONE_MINUTE_IN_MILLIS = 60000;// millisecs
		long curTimeInMs = beforeTime.getTime();
		Date afterAddingMins = new Date(curTimeInMs + (minutes * ONE_MINUTE_IN_MILLIS));
		return afterAddingMins;
	}
	
	public static String addMinutesToDate(int minutes) {
		Date date= addMinutesToDate(minutes, new Date());
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDate = dateFormat.format(date);
		return strDate;  
	}
	
	public static int yearDiff(String fmVal,Date toDate) {
		if(isBlank(fmVal)) return 0;
		Date fromDate=null;
		try {
			fromDate=getDateFromUtcString(fmVal.toString());
		}catch (Exception e) {
			return 0;
		}
		return getDiffYears(fromDate, toDate);
	}

	public  static long getMinDiff(String first, String last) {
		return getMinDiff(getDateTime(first), getDateTime(last));
	}

	
	public  static long getMinDiff(String last) {
		return getMinDiff(getDateTime(last));
	}
	public  static long getMinDiff(Date last) {
		return getMinDiff(new Date(), last);
	}
	
	public  static long getMinDiff(Date first, Date last) {
		long diff=last.getTime()-first.getTime();
		return diff/(1000*60);
	}
	public  static long getSecDiff(Date first, Date last) {
		long diff=last.getTime()-first.getTime();
		diff=diff/1000;
		return diff<0?diff*-1:diff;
	}
	public static int getDiffDays(Date first,Date last) {
		long diff = last.getTime() - first.getTime();
	    return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	}
	public static int getDiffDays(String first, String last) throws Exception {
		long diff = getTime(last,"yyyy-MM-dd HH:mm:ss").getTime() - getTime(first,"yyyy-MM-dd HH:mm:ss").getTime();
		return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	}
	
	public  static int getDiffYears(Date first, Date last) {
	    Calendar a = getCalendar(first);
	    Calendar b = getCalendar(last);
	    int diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
	    if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) || 
	        (a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE))) {
	        diff--;
	    }
	    return diff;
	}
	public static Calendar getCalendar(Date date) {
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    return cal;
	}
	
	public static Date addDurationToDate(Date inputDate, String durationType, Integer duration) {
		if(isBlank(inputDate)) inputDate = getCurrentDate();
		if(isBlank(durationType)) durationType = "days";
		if(isBlank(duration)) duration = 1;
		Calendar calender = Calendar.getInstance();
		calender.setTime(inputDate);
		if (durationType.equalsIgnoreCase("months"))  calender.add(Calendar.MONTH, duration);
		else if (durationType.equalsIgnoreCase("year")) calender.add(Calendar.YEAR, duration);
		else if (durationType.equalsIgnoreCase("days")) calender.add(Calendar.DATE, duration);
		return calender.getTime();
	}
	
	
	public static String addDurationToDate(Date inputDate, String durationType, Integer duration, String format) {
		if(isBlank(format)) format = "yyy-MM-dd HH:mm:ss";
		if(isBlank(inputDate)) inputDate = getCurrentDate();
		if(isBlank(durationType)) durationType = "days";
		if(duration==null) duration = 0;
		Calendar calender = Calendar.getInstance();
		calender.setTime(inputDate);
		if (durationType.equalsIgnoreCase("months"))  calender.add(Calendar.MONTH, duration);
		else if (durationType.equalsIgnoreCase("year")) calender.add(Calendar.YEAR, duration);
		else if (durationType.equalsIgnoreCase("days")) calender.add(Calendar.DATE, duration);
		return ZcUtil.getDateString(calender.getTime(),format);
	}
	
	public static String subtractDurationFromDate(Date inputDate, String durationType, Integer duration, String format) {
		if(isBlank(format)) format = "yyy-MM-dd HH:mm:ss";
		if(isBlank(inputDate)) inputDate = getCurrentDate();
		if(isBlank(durationType)) durationType = "days";
		if(duration==null) duration = 0;
		Calendar calender = Calendar.getInstance();
		calender.setTime(inputDate);
		if (durationType.equalsIgnoreCase("months"))  calender.add(Calendar.MONTH,  -duration);
		else if (durationType.equalsIgnoreCase("year")) calender.add(Calendar.YEAR, -duration);
		else if (durationType.equalsIgnoreCase("days")) calender.add(Calendar.DATE, -duration);
		return ZcUtil.getDateString(calender.getTime(),format);
	}
	
	/**
	 * This method take date as params and compare fromDate and toDate, if fromDate is < toDate return true else false
	 * @param fromDate
	 * @param toDate
	 * @return true if fromDate < toDate else false
	 * @author NARESH G
	 */
	public static boolean compareDate(String fromDate, String toDate ) {
		return getDate(toDate).compareTo(getDate(fromDate)) > 0;
	}
	
	/**
	 * This method take datetime as params and compare fromDateTime and toDateTime, if fromDateTime is < toDateTime return true else false
	 * @param fromDate
	 * @param toDate
	 * @return true if fromDateTime < toDateTime else false
	 * @author NARESH G
	 */
	public static boolean compareDateTime(String fromDate, String toDate ) {
		return (isBlank(fromDate) || isBlank(fromDate)) ? false : getDateTime(toDate).compareTo(getDateTime(fromDate)) > 0;
	}
	
	public static String convertNameToCode(String name){
		return ZcUtil.convertNameToCode(name, "_");
	}
	public static String convertCodeToName(String code) {
		return ZcUtil.convertCodeToName(code, "_");
	}
	public static String convertCodeToName(String code,String delimiter) {
		if(ZcUtil.isBlank(code)) return "";
		code = code.replaceAll(delimiter+"+", " ");
		return code.substring(0, 1).toUpperCase() + code.substring(1);
	}
	public static String convertNameToCode(String name,String delimiter){
		if(ZcUtil.isBlank(name)) return "";
		String _v= name.trim().replaceAll("\\s+"," ").replaceAll("[^a-zA-Z0-9]", delimiter).replaceAll(delimiter+"+", delimiter).toLowerCase();
		if(_v.endsWith(delimiter)) _v=_v.substring(0, _v.length()-1);
		return _v;
	}

	public static String convertToTitleCase(String inputString) {
		String result = "";
	       if (ZcUtil.isBlank(inputString) || inputString.trim().length() == 0)  return result;
	       result = result + Character.toUpperCase(inputString.charAt(0));
	       boolean terminalCharacterEncountered = false;
	       char[] terminalCharacters = {'.', '?', '!'};
	       for (int i = 1; i < inputString.length(); i++) {
	           char currentChar = inputString.charAt(i);
	           if (terminalCharacterEncountered) {
	               if (currentChar == ' ') {
	                   result = result + currentChar;
	               } else {
	                   result = result + Character.toUpperCase(currentChar);
	                   terminalCharacterEncountered = false;
	               }
	           } else 
	               result = result + Character.toLowerCase(currentChar);
	           for (int j = 0; j < terminalCharacters.length; j++) {
	               if (currentChar == terminalCharacters[j]) {
	                   terminalCharacterEncountered = true;
	                   break;
	               }
	           }
	       }
	       return result;
	}
	
	public static String convertToToggleCase(String inputString) {
		if (ZcUtil.isBlank(inputString) || inputString.trim().length() == 0) return "";
		if (inputString.trim().length() == 1) return inputString.toUpperCase();
		String result = "";
		inputString = inputString.trim();
		for (char c : inputString.toCharArray()) {
			if (Character.isUpperCase(c))  result = result + Character.toLowerCase(c);
			else if (Character.isLowerCase(c)) result = result + Character.toUpperCase(c);
			else  result = result + c;
		}
		return result;
	}
	
	 public static String convertToCamelCase(String inputString) {
	       String result = "";
	       if (ZcUtil.isBlank(inputString) || inputString.trim().length() == 0)  return result;
	       result = result + Character.toUpperCase(inputString.charAt(0));
	       for (int i = 1; i < inputString.length(); i++) {
	           char currentChar = inputString.charAt(i);
	           result = result + ((inputString.charAt(i - 1) == ' ') ?  Character.toUpperCase(currentChar) : Character.toLowerCase(currentChar));
	       }
	       return result;
	}
    public static boolean checkDataBetween(List<ZcMap> list,String start,String to) {
    	return checkDataBetween(list, start, to,new Date());
    }
    public static boolean checkDataBetween(List<ZcMap> list,String start,String to,Date date) {
    	if(list==null || list.isEmpty()) return false;
    	return list.stream().filter(x-> !(date.before(x.getDate(start)) || date.after(x.getDate(to)))).count()>0;
    }
    
	/**
	 * Returns the string with no of ch character
	 * @param size
	 * @param ch
	 * @return
	 */
	public static String getCharBasedOfSize(int no,char ch) {
		return new String(new char[no]).replace((new char[1])[0], ch);
	}
	
	/**
	 * Formats the given value with 2 decimals as .as decimal separator and places , as separator for every 3 digits 
	 * @param val
	 * @return
	 */
	public static String formatNumber(Double val) {
		return formatNumber(val, 2,3,',','.',"");
	}
	/**
	 * Formats the given value based on the following params
	 * @param val 	Double value
	 * @param n 	no of decimals 
	 * @param x		section size
	 * @param s		section separator
	 * @param d		decimal separator
	 * @param c		Currency symbol
	 * @return		Formatted number
	 */
	public static String formatNumber(Double val,int n,int x, char s,char d,String c) {
		if(val==null) val=0d; 
		String pattern = getCharBasedOfSize(x,'#')+","+getCharBasedOfSize(x,'#')+"."+getCharBasedOfSize(n,'#');
		String res=c+new DecimalFormat(pattern).format(val);
		res=res.replaceAll("\\.", ":.:").replaceAll(",", ":,:").replaceAll(":,:", s+"").replaceAll(":.:", d+"");
		if(res.contains(d+"")) res=res+getCharBasedOfSize(n-res.split(d=='.'?("\\."):(d+""))[1].length(),'0');
		else res=res+d+getCharBasedOfSize(n,'0');
		return res;//flag?(res+d+getFormatHash(n,'0')):res;
	}

	
	public static boolean validationForPassedDates(Date date) {
		if (date.before(getCurrentDate())) return true;
		else return false;
	}
	
	public static int calculateAge(Date date) {
		LocalDate birthDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate currentDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		if ((birthDate != null) && (currentDate != null)) {
			return Period.between(birthDate, currentDate).getYears();
		} else {
			return 0;
		}
	}
	
	public static String getTime() {
		Format f = new SimpleDateFormat("hhmmss");
		return f.format(new Date());
	}
	
	public static void ignoreFieldsInResponse(ZcMap fields, String prefix, List<String> result) {
		if (ZcUtil.hasData(fields)) {
			for (String key : fields.keySet()) {
				ZcMap value = fields.getZcMap(key);
				if (value.getB("ignoreFromResponse")) {
					String field = ZcUtil.hasData(prefix) ? prefix + "." + key : key;
					result.add(field);
				} else if (value.getS("type").equalsIgnoreCase("relation")) {
					String field = ZcUtil.hasData(prefix) ? prefix + "." + key : key;
					ignoreFieldsInResponse(value.getZcMap("fields"), field, result);
				}
			}
		}
	}
	
	public static double getCeilValue(double num) {
		return Math.ceil(num);
	}

	/**
	 * Given comma separated string converts to List<String> by removing empty and
	 * duplicate values
	 * 
	 * @param commaSepStr
	 * @return List<String>
	 */
	public static List<String> commaSepStrToList(String commaSepStr) {
		if (isBlank(commaSepStr)) return null;
		Set<String> set = new LinkedHashSet<String>();
		for (String val : commaSepStr.split(",")) {
			if (hasData(val)) set.add(val.trim());
		}
		return new ArrayList<String>(set);
	}
	
	/**
	 * Converts given key value data to comma separated string from list of maps
	 * 
	 * @param list
	 * @param keyToCommaSep
	 * @return List<String>
	 */
	public static String valueFromListOfMapToCommaSepStr(List<ZcMap> list, String keyToCommaSep) {
		if(ZcUtil.isBlank(list) || ZcUtil.isBlank(keyToCommaSep)) return null;
		String s = list.stream().map(x -> x.getS(keyToCommaSep)).filter(value -> value != null && !value.isEmpty())
		.collect(Collectors.joining(","));
		return s;
	}
	
	public static Map<String,String> zcMapToMapOfStringAndString(ZcMap data) {
		if(ZcUtil.isBlank(data)) return null;
		Map<String, String> res = data.entrySet().stream()
			    .filter(entry -> entry.getValue() != null)
			    .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().toString()));
		return res;
	}

	public static boolean contains(String str, String search) {
		char[] elements = search.toCharArray();
		for (int i = 0; i < str.length(); i++) {
			for (int j = 0; j < elements.length; j++) {
				if (elements[j] == str.charAt(i)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Object get(ZcMap data, String... keys) {
		for (String key : keys) {
			Object o = data.get(key);
			if (hasData(o)) return o;
		}
		return null;
	}

	public static String getS(ZcMap data, String... keys) {
		Object o = get(data, keys);
		return o == null ? null : o.toString();
	}

	@SuppressWarnings("unchecked")
	public static ZcMap getZcMap(ZcMap data, String... keys) {
		Object o = get(data, keys);
		return o == null ? new ZcMap() : toZcMap((Map<String, Object>) o);
	}

	public static List<ZcMap> getZcMapList(ZcMap data, String... keys) {
		Object o = get(data, keys);
		return o == null ? new ArrayList<ZcMap>() : ZcMap.getZcMapList(o);
	}
	
	public static List<ZcMap> buildList(int size) {
		List<ZcMap> op = new ArrayList<ZcMap>();
		int i=1;
		while(i<=size) {
			op.add(new ZcMap());
			i++;
		}
		return op;
	}
	
	public static List<String> substract(List<String> a, List<String> b) {
		List<String> res = new ArrayList<String>();
		for(String _s : a) {
			if(!b.contains(_s)) res.add(_s);
		}
		return res;
	}
	
	/**
	 * this method is used to get the matched key value object from the list of map, if value is null return first matched key object.
	 * @author NARESH GUMMAGUTTA
	 * @since 2023-02-16
	 * @return matched Object as ZcMap
	 */
	public static ZcMap getObjectFromZcMapList(List<ZcMap> list, String fieldKey, String fieldValue) {
		if (isBlank(list) || isBlank(fieldKey)) return null;
		List<ZcMap> data = new ArrayList<ZcMap>();
		if (hasData(fieldValue)) {
			return list.stream().filter(m -> fieldValue.equals(m.get(fieldKey))).findFirst().orElse(null);
		} else {
			data = list.stream().filter(m -> m.containsKey(fieldKey)).collect(Collectors.toList());
			return data.isEmpty() ? null : data.get(0);
		}
	}
}
