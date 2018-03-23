package serverstate;

import java.util.HashMap;
import java.util.Map;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public class Constants{
	public static final String SUMMARY = "SUMMARY";
	public static final String THREADDUMP = "THREADDUMP";

	public static final String HEALTH_UNKNOWN = "UNKNOWN";
	public static final String HEALTH_OK = "HEALTH_OK";
	public static final String HEALTH_WARN = "HEALTH_WARN";
	public static final String HEALTH_CRITICAL = "HEALTH_CRITICAL";
	public static final String HEALTH_FAILED = "HEALTH_FAILED";
	public static final String HEALTH_OVERLOADED = "HEALTH_OVERLOADED";
	
	public static Map<Integer, String> HEALTH_STATE_MAP = new HashMap<Integer, String>();
	public static final String DOMAIN_SERVICE_OBJ = "com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean";
	public static final ObjectName service;
	
	static{
		try{
			service = new ObjectName(DOMAIN_SERVICE_OBJ);
		}catch (MalformedObjectNameException e){
			throw new AssertionError(e.getMessage());
		}

		HEALTH_STATE_MAP.put(Integer.valueOf(0), HEALTH_OK);
		HEALTH_STATE_MAP.put(Integer.valueOf(1), HEALTH_WARN);
		HEALTH_STATE_MAP.put(Integer.valueOf(2), HEALTH_CRITICAL);
		HEALTH_STATE_MAP.put(Integer.valueOf(3), HEALTH_FAILED);
		HEALTH_STATE_MAP.put(Integer.valueOf(4), HEALTH_OVERLOADED);
	}
	
	public static final String TRUE_STRING = "true";
	
	public static final String PROTOCOL = "t3";
	public static final String JNDIROOT = "/jndi/";
	public static final String MBSERVER = "weblogic.management.mbeanservers.domainruntime";
	public static final String WLREMOTE = "weblogic.management.remote";
	public static final String SERVERRT = "ServerRuntimes";
	
	public static final String NAME = "Name";
	public static final String HEALTHSTATE = "HealthState";
	public static final String THREADPOOLRUNTIME = "ThreadPoolRuntime";
	public static final String EXECUTETHREADS = "ExecuteThreads";
	public static final String EXECUTETHREADTOTALCOUNT = "ExecuteThreadTotalCount";
	public static final String HOGGINGTHREADCOUNT = "HoggingThreadCount";
	public static final String JVMRUNTIME = "JVMRuntime";
	public static final String THREADSTACKDUMP = "ThreadStackDump";
	public static final String HEAPFREEPERCENT = "HeapFreePercent";
	public static final String THREADDUMP_EXT = "_ThreadDump.log";
	
	public static final String STREAM_TYPE = "application/octet-stream";
	public static final String UTF_ENCODING = "UTF-8";
	
	public static final String EMPTY_STRING = "";
	public static final String COMMA = ",";
	
	public static final String CONTENT_TYPE_KEY = "Content-type";
	public static final String CONTENT_TYPE_VAL = "text/HTML; charset=UTF-8";
	public static final String CONTENT_TYPE_HTML = "text/html";
	
	public static final String FORMAT_KEY = "format";
	public static final String FORMAT_VAL = "flowed";
	
	public static final String CONTENT_TRFR_ENCDG_KEY = "Content-Transfer-Encoding";
	public static final String CONTENT_TRFR_ENCDG_VAL = "8bit";
	
	public static final String MAIL_SMTP_HOST = "mail.smtp.host";
	public static final String MAIL_SMTP_USER = "mail.smtp.user";
	public static final String MAIL_SMTP_FROM = "mail.smtp.from";
	
	public static final String PROPERTY_CONFIG_PATH = "com.serverstate.monitorconfig";
	public static final String PROPERTY_CONFIG_ERR1 = "Property File not configured properly.";
	
	public static final String PROPERTY_ENVIRONMENT = "env";
	public static final String PROPERTY_HOSTNAME = "hostname";
	public static final String PROPERTY_PORTSTRING = "port";
	public static final String PROPERTY_WL_USERNAME = "username";
	public static final String PROPERTY_WL_PASSWORD = "password";
	public static final String PROPERTY_THREADRATIO = "threadRatio";
	public static final String PROPERTY_LESSMEMORY = "lessmemory";
	
	public static final String PROPERTY_SMTP_HOST = "smtp.host";
	public static final String PROPERTY_SMTP_USER = "smtp.user";
	public static final String PROPERTY_TO_EMAIL = "toemail";
	public static final String PROPERTY_SUBJECT = "subject";
	
	public static final String PROPERTY_BOOL_SENDALWAYS = "emailIgnoringErrors";
	public static final String PROPERTY_BOOL_EMAIL = "sendEmail";
	public static final String PROPERTY_BOOL_PRINT_SUM = "printSummary";
	public static final String PROPERTY_BOOL_THREAD = "collectThreadDump";
	
	public static final String HTML_HEADER_START = "<html><head><style>html *{  font-size: 10px;  color: #000;  font-family: Tahoma !important;}table, th, td {    font-size: 10px;    font-family: Tahoma !important;    border: 1px solid black;    border-collapse: collapse;}th {    background-color:DodgerBlue;    color:white;}</style></head><body><b><u><span style='font-family: Tahoma; font-size:16px;'>";
	public static final String HTML_HEADER_END = "</span></u></b><br/><br/>";
	public static final String HTML_FOOTER = "</body></html>";
	
	public static final String FAILURE_RED_SPAN_START = "<b><span style='color:red'>";
	public static final String FAILURE_RED_SPAN_END = "</span></b>";
}