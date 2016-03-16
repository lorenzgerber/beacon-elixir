package org.ega_archive.elixircore.service;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LogLevelFormatException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import lombok.extern.log4j.Log4j;

@Log4j
@Service
public class LogServiceImpl implements LogService {

  private static final Logger loggerManager = Logger.getLogger("eu.crg.ega");

  private static String[]
      levels =
      {"ALL", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF"};

  @Override
  public Level getLoglevel() {
    return loggerManager.getLevel();
  }

  @Override
  public void setLoglevel(String level) throws LogLevelFormatException {
    log.debug("Entering setLoglevel(" + level + ")");
    if (!Arrays.asList(levels).contains(level.toUpperCase())) {
      throw new LogLevelFormatException("Level not correct");
    }
    loggerManager.setLevel(Level.toLevel(level));

    log.debug("Exiting setLoglevel");
  }

  @Override
  public void setLoglevel(String classname, String level) throws LogLevelFormatException {
    if (level == null || level.isEmpty() || classname == null || classname.isEmpty()) {
      throw new LogLevelFormatException("");
    }
    if (!Arrays.asList(levels).contains(level.toUpperCase())) {
      throw new LogLevelFormatException("Level not correct");
    }
    Logger.getLogger(classname.toLowerCase()).setLevel(Level.toLevel(level));
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getLoglevels() {
    log.debug("Entering getLogLevels");
    return (Arrays.asList(levels));

  }

}
