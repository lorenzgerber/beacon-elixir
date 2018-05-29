package org.ega_archive.elixircore.jetty;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Dispatcher;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.ByteArrayISO8859Writer;
import org.eclipse.jetty.util.log.Log;
import org.ega_archive.elixircore.dto.Base;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

@Log4j
public class JettyCustomErrorHandler extends ErrorHandler {

  //We cannot use HttpConversionService because the web context does not exist
  //private HttpConversionService httpConversionService;

  @Override
  public void handle(String target, Request baseRequest, HttpServletRequest request,
                     HttpServletResponse response) throws IOException {
    AbstractHttpConnection connection = AbstractHttpConnection.getCurrentConnection();
    String method = request.getMethod();
    if (!method.equals(HttpMethods.GET) && !method.equals(HttpMethods.POST)
        && !method.equals(HttpMethods.HEAD)) {
      connection.getRequest().setHandled(true);
      return;
    }

    if (this instanceof ErrorPageMapper) {
      String error_page = ((ErrorPageMapper) this).getErrorPage(request);
      if (error_page != null && request.getServletContext() != null) {
        String old_error_page = (String) request.getAttribute(ERROR_PAGE);
        if (old_error_page == null || !old_error_page.equals(error_page)) {
          request.setAttribute(ERROR_PAGE, error_page);

          Dispatcher dispatcher =
              (Dispatcher) request.getServletContext().getRequestDispatcher(error_page);
          try {
            if (dispatcher != null) {
              dispatcher.error(request, response);
              return;
            }
            log.warn("No error page " + error_page);
          } catch (ServletException e) {
            log.warn(Log.EXCEPTION, e);
            return;
          }
        }
      }
    }

    connection.getRequest().setHandled(true);
    if (getCacheControl() != null) {
      response.setHeader(HttpHeaders.CACHE_CONTROL, getCacheControl());
    }
    ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer(4096);
    Base<String>
        result =
        new Base<String>(String.valueOf(connection.getResponse().getStatus()),
                         new Exception(connection.getResponse()
                                           .getReason()));
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(writer, result);
    } catch (IOException e) {
      log.error("Could not serialize exception");
      e.printStackTrace();
    }
    writer.flush();
    response.setContentLength(writer.size());
    writer.writeTo(response.getOutputStream());
    writer.destroy();
  }
}
