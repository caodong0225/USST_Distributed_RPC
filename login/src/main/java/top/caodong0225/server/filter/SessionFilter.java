package top.caodong0225.server.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import top.caodong0225.server.util.JWTUtil;

import java.io.IOException;
import java.util.Map;

@Component
public class SessionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // Validate the token (this is a placeholder, implement your own validation logic)
            if (JWTUtil.isTokenValid(token)) {
                // Token is valid, proceed with the request
                Map<String, Object> res = JWTUtil.verifyToken(token);
                // 把信息放入session
                if (res != null) {
                    httpRequest.getSession().setAttribute("userId", res.get("userId"));
                    httpRequest.getSession().setAttribute("role", res.get("role"));
                    httpRequest.getSession().setAttribute("userName", res.get("userName"));
                    httpRequest.getSession().setAttribute("email", res.get("email"));
                }
                chain.doFilter(httpRequest, response);
                return;
            }
        }

        chain.doFilter(httpRequest, httpResponse);
    }
}
