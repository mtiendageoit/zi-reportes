package com.zonainmueble.reports.filters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

@Component
public class ApiKeyFilter implements Filter {

  @Value("${app.api-key}")
  private String apiKey;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String requestApiKey = httpRequest.getParameter("api_key");

    if (!apiKey.equals(requestApiKey)) {
      httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      httpResponse.getWriter().write("Invalid API Key");
      return;
    }

    chain.doFilter(request, response);
  }

}
