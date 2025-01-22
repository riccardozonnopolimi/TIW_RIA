package it.polimi.tiw.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet Filter implementation class CheckNotLoggedUser
 */
public class CheckNotLoggedUser implements Filter {

	/**
	 * Default constructor.
	 */
	public CheckNotLoggedUser() {
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest  req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		HttpSession           s = req.getSession(false);

		if (s != null) {
			Object user = s.getAttribute("currentUser");
			
			if (user != null) {		
				res.sendRedirect("homePage.html");
				return;
			}
		}
		chain.doFilter(request, response);
	}
}
