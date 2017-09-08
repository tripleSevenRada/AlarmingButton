package radim.alarmingLogger.logging;

import java.util.List;

public class LogToFile {

    private final LogEntryDao logEntryDao;

    public LogToFile(LogEntryDao l){
            this.logEntryDao = l;
    }

    /**
     *
     */
    public void reset(){
        synchronized (logEntryDao){
            logEntryDao.deleteAll();
        }
    }

    /**
     * @param logEntry
     */
    public void addEntry(LogEntry logEntry){
        synchronized (logEntryDao) {
            logEntryDao.insert(logEntry);
        }
    }

    /**
     * @return
     */
    public List<LogEntry> getLog(){
        synchronized (logEntryDao){
            return logEntryDao.loadAll();
        }
    }
}
