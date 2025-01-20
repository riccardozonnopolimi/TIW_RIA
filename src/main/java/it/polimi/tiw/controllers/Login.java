package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.util.ConnectionHandler;
import it.polimi.tiw.DAO.UserDAO;

/**
 * Servlet implementation class Login
 */
@WebServlet("/Login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Login() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init() throws ServletException {

		ServletContext servletContext = getServletContext();
		this.connection = ConnectionHandler.getConnection(servletContext);
	}

	@Override
	public void destroy() {

		try {

			ConnectionHandler.closeConnection(connection);

		} catch (SQLException e) {

			e.printStackTrace();
		}
	}
	
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String email = request.getParameter("email");
		String password = request.getParameter("password");

		// Verify if the given argument are null and if so forward to errorPage
		if (email == null || password == null) {

			// TODO forwardToErrorPage(request, response, "Null email or password");
			return;
		}

		// Query DB to authenticate user
		// If user not present, forward to ErrorPage
		UserDAO userDAO = new UserDAO(connection);
		User user = null;

		try {

			user = userDAO.getUserLog(email, password);

		} catch (SQLException e) {

			request.setAttribute("warning", "Email or password incorrect!");
			//TODO forward(request, response, "/WEB-INF/login.html");
			return;
		}

		// If the user exists, add info to the session and go to home page, otherwise
		// show login page with error message
		if (user == null) {

			request.setAttribute("warning", "Email or password incorrect!");
			//TODO forward(request, response, "/WEB-INF/login.html");
			return;
		}

		HttpSession session = request.getSession();
		session.setAttribute("currentUser", user);
		session.setAttribute("currentUserId", user.getId_user());
		response.sendRedirect("homePage.html");
	}

}
