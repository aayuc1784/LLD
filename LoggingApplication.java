import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoggingApplication {
    public static void main(String[] args){
        Logger logger = Logger.getInstance();

        logger.info("This is info");
        logger.error("This is error");
        logger.warn("This is warning");
        logger.debug("This is debug");
    }
}

enum LogLevel{

    INFO(1), DEBUG(2), WARN(3), ERROR(4);

    private final int logLevel;

    LogLevel(int logLevel){
        this.logLevel=logLevel;
    }

    public int getLogLevel() {
        return logLevel;
    }
}

abstract class LoggerHandler{
    public LogLevel logLevel;
    private LoggerHandler nextLoggerHandler;

    public void setNextLoggerHandler(LoggerHandler nextLoggerHandler){
        this.nextLoggerHandler = nextLoggerHandler;
    }

    public void logMessage(LogLevel logLevel, String message, LogObservable logObservable){
        if(logLevel.getLogLevel() == this.logLevel.getLogLevel()){
            publishLog(message, logObservable);
        }

        if(nextLoggerHandler != null){
            nextLoggerHandler.logMessage(logLevel,message, logObservable);
        }

    }

    public abstract void publishLog(String message, LogObservable logObservable);
}

class InfoHandler extends LoggerHandler{

    public InfoHandler(LogLevel logLevel){
        this.logLevel=logLevel;
    }

    @Override
    public void publishLog(String message, LogObservable logObservable) {
        String infoMessage = "INFO: " + message;
        logObservable.notifyObserver(LogLevel.INFO, infoMessage);
    }
}

class DebugHandler extends LoggerHandler{

    public DebugHandler(LogLevel logLevel){
        this.logLevel=logLevel;
    }

    @Override
    public void publishLog(String message, LogObservable logObservable) {
        String debugMessage = "DEBUG: " + message;
        logObservable.notifyObserver(LogLevel.DEBUG, debugMessage);
    }
}

class ErrorHandler extends LoggerHandler{

    public ErrorHandler(LogLevel logLevel){
        this.logLevel=logLevel;
    }

    @Override
    public void publishLog(String message, LogObservable logObservable) {
        String errorMessage = "ERROR: " + message;
        logObservable.notifyObserver(LogLevel.ERROR, errorMessage);
    }
}

class WarnHandler extends LoggerHandler{

    public WarnHandler(LogLevel logLevel) {
        this.logLevel=logLevel;

    }

    @Override
    public void publishLog(String message, LogObservable logObservable) {
        String warnMessage = "WARN: " + message;
        logObservable.notifyObserver(LogLevel.WARN, warnMessage);
    }
}

class LogManager{

    public static LoggerHandler buildChainLogging(){
        LoggerHandler infoLogger = new InfoHandler(LogLevel.INFO);
        LoggerHandler debugLogger = new DebugHandler(LogLevel.DEBUG);
        LoggerHandler warnLogger = new WarnHandler(LogLevel.WARN);
        LoggerHandler errorLogger = new ErrorHandler(LogLevel.ERROR);

        infoLogger.setNextLoggerHandler(debugLogger);
        debugLogger.setNextLoggerHandler(warnLogger);
        warnLogger.setNextLoggerHandler(errorLogger);

        return infoLogger;
    }

    public static LogObservable buildLogObservable(){
        LogObservable logObservable = new LogObservable();
        logObservable.addObserver(LogLevel.INFO, new FileLog());
        logObservable.addObserver(LogLevel.ERROR, new ConsoleLog());
        logObservable.addObserver(LogLevel.WARN, new DatabaseLog());

        return logObservable;
    }

}

class Logger{
    private final static Logger instance = new Logger();
    private final static LoggerHandler loggerHandler = LogManager.buildChainLogging();
    private final static LogObservable logObservable = LogManager.buildLogObservable();

    private Logger() {};

    public static Logger getInstance() {
        return instance;
    }

    public void info(String message){
        loggerHandler.logMessage(LogLevel.INFO, message, logObservable);
    }

    public void error(String message){
        loggerHandler.logMessage(LogLevel.ERROR, message, logObservable);
    }

    public void debug(String message){
        loggerHandler.logMessage(LogLevel.DEBUG, message, logObservable);
    }

    public void warn(String message){
        loggerHandler.logMessage(LogLevel.WARN, message, logObservable);
    }
}

class LogObservable{
    Map<LogLevel, List<LogObserver>> observables = new HashMap<>();

    public void addObserver(LogLevel logLevel, LogObserver logObserver){
        List<LogObserver> logObservers = observables.getOrDefault(logLevel,new ArrayList<>());
        logObservers.add(logObserver);
        observables.put(logLevel,logObservers);

    }
    public void removeObserver(){

    }

    public void notifyObserver(LogLevel logLevel, String message){
        for(Map.Entry<LogLevel,List<LogObserver>> entry: observables.entrySet()){
            if(entry.getKey().getLogLevel() == logLevel.getLogLevel()){
                entry.getValue().forEach(observables -> observables.log(message));
            }
        }
    }
}

interface LogObserver{
    void log(String message);
}

class FileLog implements LogObserver{

    @Override
    public void log(String message) {
        System.out.println("Logging into file " + message);
    }
}

class ConsoleLog implements LogObserver{

    @Override
    public void log(String message) {
        System.out.println("Logging into console " + message);
    }
}
class DatabaseLog implements LogObserver{

    @Override
    public void log(String message) {
        System.out.println("Logging into database " + message);
    }
}
