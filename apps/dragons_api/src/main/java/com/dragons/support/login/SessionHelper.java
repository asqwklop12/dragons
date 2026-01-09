package com.dragons.support.login;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Component;

@Component
public class SessionHelper {
  public void createLoginSession(HttpServletRequest request, String email, ZonedDateTime loginTime, String provider) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    session = request.getSession(true);
    session.setAttribute("userEmail", email);
    session.setAttribute("loginTime", loginTime);
    session.setAttribute("provider", provider);
  }
}
