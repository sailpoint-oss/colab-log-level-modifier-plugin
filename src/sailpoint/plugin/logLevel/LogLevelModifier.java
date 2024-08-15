package sailpoint.plugin.logLevel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.Level;

import org.apache.logging.log4j.core.config.LoggerConfig;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.SystemAdmin;
import sailpoint.tools.GeneralException;


@Path("logLevelModifier")
public class LogLevelModifier extends BasePluginResource {

	protected static final org.apache.logging.log4j.Logger log = LogManager.getLogger();

	
	@Override
	public String getPluginName() {
		return "loglevelmodifier";
	}
	
	@SuppressWarnings("unchecked")
	
	@GET
	@Path("getLoggers")
	@SystemAdmin
	@Produces(MediaType.APPLICATION_JSON)
	public List<Map<String, String>> gerLoggers() throws GeneralException {
		
		log.trace("Running getLoggers. By user "+ getLoggedInUser().getName());

		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		
		Collection<Logger> loggers = ctx.getLoggers();
		List<Map<String, String>> loggersList = new ArrayList<>();
		
		for(Logger logger: loggers) {
			Map<String, String> loggerMap = new HashMap<>();
            loggerMap.put("LoggerName", logger.getName());
            loggerMap.put("Parent", (logger.getParent() == null ? null : logger.getParent().getName()));
            loggerMap.put("EffectiveLevel", String.valueOf(logger.getLevel()));
            loggerMap.put("show","true");
            
            loggersList.add(loggerMap);
		}
        Collections.sort(loggersList, Comparator.comparing(m -> m.get("LoggerName")));
        
        return loggersList;
	}
	

	@GET
	@Path("setLogLevel")
	@SystemAdmin
	@Produces(MediaType.APPLICATION_JSON)
	public String setLogLevel(@QueryParam("lName") String loggerName, @QueryParam("level") String level) throws Exception {
		log.trace("Setting logger "+loggerName+ " to "+level);
		Level newLevel = Level.getLevel(level.toUpperCase());

		if(newLevel == null) {
			throw new Exception("Log level not recognized: "+level);
		}

		final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
		final Configuration config = loggerContext.getConfiguration();

		LoggerConfig loggerConfig = config.getLoggerConfig(loggerName);
		log.trace("Got LoggerConfig: "+loggerConfig.getName()+" with level "+loggerConfig.getLevel());

		if (!loggerConfig.getName().equals(loggerName)) {
			LoggerConfig newLoggerConfig = new LoggerConfig(loggerName, newLevel, true);
			newLoggerConfig.setParent(loggerConfig);
			config.addLogger(loggerName, newLoggerConfig);
			log.trace("New Logger configuration name: "+newLoggerConfig.getName()+" level: "+newLoggerConfig.getLevel());
		} else {
			loggerConfig.setLevel(newLevel);
			log.trace("Existing logger configuration name: "+loggerConfig.getName()+" level: "+loggerConfig.getLevel());
		}
		loggerContext.updateLoggers();
		log.trace("Log level update done.");

		return "OK";
	}
}
