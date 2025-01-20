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

import org.thymeleaf.TemplateEngine;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.DAO.UserDAO;
import it.polimi.tiw.util.ConnectionHandler;


/**
 * Servlet implementation class Register
 */
@WebServlet("/Register")
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Register() {
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

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        doPost(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Tries to register the user, if the function returns null ends (registerUser already re-directed)
        if (registerUser(request, response) == null) {
            return;
        }

        // Once the user is registered is redirected to the LoginPage
        response.sendRedirect("homePage.html");
    }


    /**
     * Verifies the input and if it is correct creates the user in the DB and an account of the user.
     * If the operation is successful returns 0, else returns null
     *
     * @param request
     * @param response
     * @return User
     * @throws ServletException
     * @throws IOException
     */
    private Integer registerUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Gets the parameters of the request and verifies if they are in the correct format (length and syntax)
        String email      = request.getParameter("email");
        String username   = request.getParameter("username");
        String password   = request.getParameter("password");
        String repeat_pwd = request.getParameter("repeat_pwd");

        // Verifies if all parameters are not null
        if( email == null || username == null || password == null || repeat_pwd == null) {

            //TODO forwardToErrorPage(request, response, "Register module missing some data");
            return null;
        }

        // Checks if the inserted string (EMAIL) matches with an e-mail syntax (RFC5322 e-mail) by using a RegEx
        String emailRegEx = "^(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])$";

        // If the string does not match the the user is redirected to the register page with an error message
        if (!email.matches(emailRegEx)) {

            request.setAttribute("warning", "Chosen email invalid!");
            response.sendRedirect("login.html");
            return null;
        }

        // Checks if the inserted string (USERNAME) is of the correct length (1-45)
        if (username.length() <= 0 || username.length() > 40) {

            request.setAttribute("warning", "Chosen username invalid (a valid username has more than one character and less than 45)!");
            response.sendRedirect("login.html");
            return null;
        }

        // Checks if the inserted strings (PASSWORD and REPEAT_PWD) are of the correct length (1-45) and equal
        if (password.length() <= 0 || password.length() > 40) {

            request.setAttribute("warning", "Chosen password invalid (a valid password has more than one character and less than 45)!");
            response.sendRedirect("login.html"); 
            return null;
        }

        if (!password.equals(repeat_pwd)) {

            request.setAttribute("warning", "Password and repeat password field not equal!");
            response.sendRedirect("login.html");
            return null;
        }

        // CHECKS that the submitted user for the registration has not the SAME EMAIL
        // of another user in the DB.
        // If another user with the same email is present redirects to the to the RegisterPage
        // with a warning and error message
        UserDAO userDAO = new UserDAO(connection);
        User user = null;

        try {

            user = userDAO.findUserByEmail(email);

        } catch (SQLException e) {

            //TODO forwardToErrorPage(request, response, e.getMessage());
            return null;
        }

        if(user != null) {

            request.setAttribute("warning", "Chosen email already in use!");
            response.sendRedirect("login.html");
            return null;
        }

        // CHECKS that the submitted user for the registration has not the SAME USERNAME
        // of another user in the DB.
        // If another user with the same username is present redirects to the to the RegisterPage
        // with a warning and error message
        try {

            user = userDAO.findUserByUsername(username);

        } catch (SQLException e) {

            //TODO forwardToErrorPage(request, response, e.getMessage());
            return null;
        }

        if(user != null) {

            request.setAttribute("warning", "Chosen username already in use!");
            response.sendRedirect("login.html");
            return null;
        }

        // If all the parameters are correct creates the user in the DB
        try {


            userDAO.createUser( username, email, password);

        } catch (SQLException e) {

            //TODO forwardToErrorPage(request, response, e.getMessage());
            return null;
        }

        // Gets the created user in the DB to verify it has been correctly created in the DB,
        // else if an Exception is raised forwards to the ErrorPage
        try {

            user = userDAO.findUserByEmail(email);

        } catch (SQLException e) {

            //TODO forwardToErrorPage(request, response, e.getMessage());
            return null;
        }

        if(user == null) {

            //TODO forwardToErrorPage(request, response, "Error: user non correctly created - registerPage!");
            return null;
        }

        return 0;
    }


}
