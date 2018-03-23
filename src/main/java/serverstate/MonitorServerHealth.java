package serverstate;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import weblogic.health.HealthState;
import weblogic.management.runtime.ExecuteThread;
import static serverstate.Constants.*;

public class MonitorServerHealth{
	private static Properties properties = new Properties();
	private static String configPath;
	private static MBeanServerConnection connection;
	private static JMXConnector connector;
	private String hostname, portString, username, password, stuckThreadRatio, lessMemoryString;
	private String environment, smtpHost, smtpUser, toEmail, subject;
	boolean ignoreErrors, sendEmail, printSummary, takeThreadDump;
	int lessMemory;
	
	private static final List<String> TABLE_HEADER = new ArrayList<String>(){ 
		private static final long serialVersionUID = 1L; 
	};

	private String write(List<List<String>> tableRows){
		String emailBodyString = HTML_HEADER_START + this.subject + HTML_HEADER_END;
		String resultTable = EMPTY_STRING;
		resultTable = resultTable + "<table border=1>";
		resultTable = resultTable + "<tr>";
		for(String header : TABLE_HEADER){
			resultTable = resultTable + "<th>" + header + "</th>";
		}
		resultTable = resultTable + "</tr>";
		for(List<String> row : tableRows){
			resultTable = resultTable + "<tr>";
			for(String resultString : row){
				resultString =(resultString != null) &&(isHighLight(resultString)) ? 
						"<b><span style='color:red'>" + resultString + "</span></b>" : resultString;
				resultTable = resultTable + "<td>";
				resultTable = resultTable + resultString;
				resultTable = resultTable + "</td>";
			}
			resultTable = resultTable + "</tr>";
		}
		resultTable = resultTable + "</table>";
		resultTable = resultTable + "<br/>";
		emailBodyString = emailBodyString + resultTable;
		emailBodyString = emailBodyString + HTML_FOOTER;
		return emailBodyString;
	}

	private boolean isHighLight(String resultString){
		return TRUE_STRING.equalsIgnoreCase(resultString);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void init() throws Exception{
		initializeProperties();

		this.environment = getProperty(PROPERTY_ENVIRONMENT);

		this.hostname = getProperty(PROPERTY_HOSTNAME);
		this.portString = getProperty(PROPERTY_PORTSTRING);
		this.username = getProperty(PROPERTY_WL_USERNAME);
		this.password = getProperty(PROPERTY_WL_PASSWORD);
		this.stuckThreadRatio = getProperty(PROPERTY_THREADRATIO);
		this.lessMemoryString = getProperty(PROPERTY_LESSMEMORY);
		this.lessMemory = Integer.parseInt(this.lessMemoryString);

		this.smtpHost = getProperty(PROPERTY_SMTP_HOST);
		this.smtpUser = getProperty(PROPERTY_SMTP_USER);
		this.toEmail = getProperty(PROPERTY_TO_EMAIL);
		this.subject = getProperty(PROPERTY_SUBJECT);

		this.ignoreErrors = "true".equalsIgnoreCase(getProperty(PROPERTY_BOOL_SENDALWAYS));
		this.sendEmail = "true".equalsIgnoreCase(getProperty(PROPERTY_BOOL_EMAIL));
		this.printSummary = "true".equalsIgnoreCase(getProperty(PROPERTY_BOOL_PRINT_SUM));
		this.takeThreadDump = "true".equalsIgnoreCase(getProperty(PROPERTY_BOOL_THREAD));

		int port = Integer.parseInt(this.portString);
		JMXServiceURL serviceURL = new JMXServiceURL(PROTOCOL, this.hostname, port, JNDIROOT+MBSERVER);

		Hashtable h = new Hashtable();
		h.put("java.naming.security.principal", this.username);
		h.put("java.naming.security.credentials", this.password);
		h.put("jmx.remote.protocol.provider.pkgs", WLREMOTE);

		connector = JMXConnectorFactory.connect(serviceURL, h);
		connection = connector.getMBeanServerConnection();
	}

	private void initializeProperties() throws Exception{
		configPath = configPath == null ? System.getProperty(PROPERTY_CONFIG_PATH) : configPath;
		if((configPath != null) &&(!configPath.isEmpty())){
			InputStream input = null;
			try{
				input = new FileInputStream(configPath);
				properties.load(input);
			}catch(Exception e){
				throw new IllegalArgumentException(PROPERTY_CONFIG_ERR1, e);
			}finally{
				if(input != null)
					input.close();
			}
		}
		else{
			throw new IllegalArgumentException(PROPERTY_CONFIG_ERR1);
		}
	}

	private String getProperty(String keyword){
		String propertyValue = null;
		if(properties != null){
			propertyValue = properties.getProperty(keyword);
		}
		return propertyValue;
	}

	private static ObjectName[] getServerRuntimes() throws Exception{
		return(ObjectName[])connection.getAttribute(Constants.service, SERVERRT);
	}

	private void checkHealthAndStuckThreads() throws Exception{
		String stuckThreadRatio = this.stuckThreadRatio;

		Map<String, Map<String, Object>> emailContent = new TreeMap<String, Map<String, Object>>();
		ObjectName[] serverRTs = getServerRuntimes();

		for(ObjectName serverRT : serverRTs){
			boolean isHealthNotOk = false;
			boolean isStuckThread = false;
			boolean isPossiblyStuck = false;
			boolean isLessHeap = false;

			String serverName =(String)connection.getAttribute(serverRT, NAME);
			String healthState = getServerHealthString((HealthState)connection.getAttribute(serverRT, HEALTHSTATE));
			isHealthNotOk = !HEALTH_OK.equals(healthState);

			ObjectName threadBean =(ObjectName)connection.getAttribute(serverRT, THREADPOOLRUNTIME);
			isStuckThread = detectStuckThread(threadBean);

			Integer executeThreads =(Integer)connection.getAttribute(threadBean, EXECUTETHREADTOTALCOUNT);
			Integer hoggingThreads =(Integer)connection.getAttribute(threadBean, HOGGINGTHREADCOUNT);
			double threadRatio = hoggingThreads.doubleValue() / executeThreads.doubleValue();
			isPossiblyStuck = threadRatio > Double.parseDouble(stuckThreadRatio);

			ObjectName jvmBean =(ObjectName)connection.getAttribute(serverRT, JVMRUNTIME);
			int heapFreePercent =((Integer)connection.getAttribute(jvmBean, HEAPFREEPERCENT)).intValue();
			isLessHeap = heapFreePercent < this.lessMemory;

			String summary = "Server Name : " + serverName + 
					"; \tHealth : " + healthState + 
					"; \tMemory : " + heapFreePercent + 
					"; \tStuck  : " + isStuckThread + 
					"; \tPossiblility to Stuck : " + isPossiblyStuck + 
					"; \t(Thread Ratio :" + String.format("%.2f", new Object[]{ Double.valueOf(threadRatio) }) + " [" + hoggingThreads + "/" + executeThreads + "] > " + stuckThreadRatio + ") \n\n";

			List<String> summaryRow = new ArrayList<String>();
			summaryRow.add(serverName);
			summaryRow.add(healthState);
			summaryRow.add(String.valueOf(heapFreePercent));
			summaryRow.add(String.valueOf(isStuckThread).toUpperCase());
			summaryRow.add(String.valueOf(isPossiblyStuck).toUpperCase());
			summaryRow.add(String.format("%.2f", new Object[]{ Double.valueOf(threadRatio) }) + " [" + hoggingThreads + "/" + executeThreads + "] > " + stuckThreadRatio);

			if(this.printSummary){
				System.out.println(new Date());
				System.out.println(summary);
			}

			if((isHealthNotOk) ||(isStuckThread) ||(isPossiblyStuck) ||(isLessHeap) ||(this.ignoreErrors)){
				String threadDump =((this.takeThreadDump) &&(this.sendEmail)) ||(this.ignoreErrors) ? getThreadDump(jvmBean) : null;

				Map<String, Object> emailBodyMap = new HashMap<String, Object>();
				emailBodyMap.put(SUMMARY, summaryRow);
				emailBodyMap.put(THREADDUMP, threadDump);

				emailContent.put(serverName, emailBodyMap);
			}
		}
		checkAndSendEmail(emailContent);
	}

	private boolean detectStuckThread(ObjectName threadBean) throws Exception{
		boolean isStuckThread = false;

		ExecuteThread[] executeThreads =(ExecuteThread[])connection.getAttribute(threadBean, EXECUTETHREADS);
		if((executeThreads != null) &&(executeThreads.length > 0)){
			for(ExecuteThread executeThread : executeThreads){
				if(executeThread.isStuck()){
					isStuckThread = true;
					break;
				}
			}
		}
		return isStuckThread;
	}

	private String getThreadDump(ObjectName jvmBean) throws Exception{
		String threadDump = EMPTY_STRING;
		threadDump =(String)connection.getAttribute(jvmBean, THREADSTACKDUMP);
		return threadDump;
	}

	@SuppressWarnings("unchecked")
	private void checkAndSendEmail(Map<String, Map<String, Object>> emailContent) throws UnsupportedEncodingException, MessagingException
	{
		if((!emailContent.isEmpty()) &&((this.sendEmail) ||(this.ignoreErrors))){
			Properties props = System.getProperties();
			props.put(MAIL_SMTP_HOST, this.smtpHost);
			props.put(MAIL_SMTP_USER, this.smtpUser);
			props.put(MAIL_SMTP_FROM, this.smtpUser);
			Session session = Session.getInstance(props, null);

			MimeMessage msg = new MimeMessage(session);
			msg.addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VAL);
			msg.addHeader(FORMAT_KEY, FORMAT_VAL);
			msg.addHeader(CONTENT_TRFR_ENCDG_KEY, CONTENT_TRFR_ENCDG_VAL);
			msg.setFrom(new InternetAddress(this.smtpUser, this.environment));
			msg.setReplyTo(InternetAddress.parse(this.smtpUser, false));
			msg.setSubject(this.subject,UTF_ENCODING);
			msg.setSentDate(new Date());
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(this.toEmail, false));

			Multipart multipart = new MimeMultipart();

			List<List<String>> tableRows = new ArrayList<List<String>>();

			for(String serverName : emailContent.keySet()){
				Map<String, Object> bodyContent =(Map<String, Object>)emailContent.get(serverName);
				tableRows.add((List<String>)bodyContent.get(SUMMARY));
				String threadDump =(String)bodyContent.get(THREADDUMP);
				if(threadDump != null){
					BodyPart messageBodyPart = new MimeBodyPart();
					DataSource source = new ByteArrayDataSource(threadDump.getBytes("UTF-8"), "application/octet-stream");
					messageBodyPart.setDataHandler(new DataHandler(source));
					messageBodyPart.setFileName(serverName + THREADDUMP_EXT);
					multipart.addBodyPart(messageBodyPart);
				}
			}

			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(write(tableRows), CONTENT_TYPE_HTML);
			multipart.addBodyPart(messageBodyPart);

			msg.setContent(multipart);
			Transport.send(msg);
		}
	}

	private String getServerHealthString(HealthState serverHealthState){
		int hState = serverHealthState.getState();
		String serverHealth =(String)Constants.HEALTH_STATE_MAP.get(Integer.valueOf(hState));
		serverHealth = serverHealth == null ? HEALTH_UNKNOWN : serverHealth;
		return serverHealth;
	}

	public static void main(String[] args) throws Exception{
		try{
			MonitorServerHealth monitor = new MonitorServerHealth();
			monitor.init();
			monitor.checkHealthAndStuckThreads();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(connector != null)
				connector.close();
		}
	}
}