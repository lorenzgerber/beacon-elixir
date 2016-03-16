package org.ega_archive.elixircore.service;

import org.apache.log4j.Level;
import org.apache.log4j.lf5.LogLevel;
import org.apache.log4j.lf5.LogLevelFormatException;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface LogService {

  public Level getLoglevel();

  public void setLoglevel(String level) throws LogLevelFormatException;

  public void setLoglevel(String classname, String level) throws LogLevelFormatException;

  public List<String> getLoglevels();

}
