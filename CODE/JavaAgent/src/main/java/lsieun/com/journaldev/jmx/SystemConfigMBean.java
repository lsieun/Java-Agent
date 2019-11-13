package lsieun.com.journaldev.jmx;

public interface SystemConfigMBean {

    void setThreadCount(int noOfThreads);
    int getThreadCount();

    void setSchemaName(String schemaName);
    String getSchemaName();

    // any method starting with get and set are considered
    // as attributes getter and setter methods, so I am
    // using do* for operation.
    String doConfig();
}
