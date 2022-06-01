package utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadAnalyzer {

	private static final Logger LOGGER = LogManager.getLogger(ThreadAnalyzer.class);
	private static final ThreadMXBean MX_BEAN = ManagementFactory.getThreadMXBean();
	private static Map<Long, ThreadInfo> ID_THREAD_MAP = new TreeMap<Long, ThreadInfo>();
	
	static {
		if(!MX_BEAN.isThreadContentionMonitoringEnabled()) {
			MX_BEAN.setThreadContentionMonitoringEnabled(true);
			LOGGER.info("execute setThreadContentionMonitoringEnabled(true) !");
		}
		if(!MX_BEAN.isThreadCpuTimeEnabled()) {
			MX_BEAN.setThreadCpuTimeEnabled(true);
			LOGGER.info("execute setThreadCpuTimeEnabled(true) !");
		}
	}
		
	public static void printSomeGenerals() {
		LOGGER.info("------------------------------------");
		LOGGER.info("------------------------------------");
		LOGGER.info("isThreadCpuTimeEnabled: " + MX_BEAN.isThreadCpuTimeEnabled()); 
		LOGGER.info("isThreadCpuTimeSupported: " + MX_BEAN.isThreadCpuTimeSupported());
		LOGGER.info("getTotalStartedThreadCount: " + MX_BEAN.getTotalStartedThreadCount());
		LOGGER.info("getThreadCount: " + MX_BEAN.getThreadCount());
		LOGGER.info("getPeakThreadCount: " + MX_BEAN.getPeakThreadCount());
		LOGGER.info("getDaemonThreadCount: " + MX_BEAN.getDaemonThreadCount());

		StringBuilder sb = new StringBuilder();
		Arrays.stream(MX_BEAN.getAllThreadIds())
			.forEach(s -> {
		        sb.append(s + "; ");
		    });
		LOGGER.info("ThreadIds: " + sb); 
		
		LOGGER.info("findDeadlockedThreads: " + MX_BEAN.findDeadlockedThreads());
		LOGGER.info("findMonitorDeadlockedThreads: " + MX_BEAN.findMonitorDeadlockedThreads());
		LOGGER.info("getTotalStartedThreadCount: " + MX_BEAN.getTotalStartedThreadCount());
		LOGGER.info("------------------------------------");
		LOGGER.info("------------------------------------");
	}
	
	public static void actualizeCurrentThreadMap() {
		long[] threadIdArray = MX_BEAN.getAllThreadIds();
		for(long l: threadIdArray)
			ID_THREAD_MAP.put(l, MX_BEAN.getThreadInfo(l));
	}
	
	public static void printThreadInfos() {
		for (Map.Entry<Long, ThreadInfo> entry : ID_THREAD_MAP.entrySet()) {
			if(entry.getKey().equals(1L) || entry.getKey() >= 16L) {
				LOGGER.info("--------------");
				LOGGER.info("ThreadID: " + entry.getKey() + Utils.FILE_SEPARATOR
					+ "getThreadName: " + entry.getValue().getThreadName() + Utils.FILE_SEPARATOR
					+ "getThreadCpuTime: " + TimeUnit.SECONDS.convert(MX_BEAN.getThreadCpuTime(entry.getKey()), TimeUnit.NANOSECONDS) + Utils.FILE_SEPARATOR
					+ "getThreadState: " + entry.getValue().getThreadState() + Utils.FILE_SEPARATOR
					+ "getWaitedCount: " + entry.getValue().getWaitedCount() + Utils.FILE_SEPARATOR
					+ "getWaitedTime: " + entry.getValue().getWaitedTime() + Utils.FILE_SEPARATOR
					+ "isDaemon: " + entry.getValue().isDaemon() + Utils.FILE_SEPARATOR
					+ "getBlockedCount: " + entry.getValue().getBlockedCount() + Utils.FILE_SEPARATOR
					+ "getBlockedTime: " + entry.getValue().getBlockedTime());
				LOGGER.info("--------------");
			}
		} 	
	}
}
