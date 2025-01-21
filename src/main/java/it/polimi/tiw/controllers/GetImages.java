package it.polimi.tiw.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.DAO.ImmagineDAO;
import it.polimi.tiw.beans.Immagine;
import it.polimi.tiw.util.ConnectionHandler;
/**
 * Servlet implementation class GetImages
 */
@WebServlet("/GetImages")
public class GetImages extends HttpServlet {
    private static final long serialVersionUID = 1L;
	private Connection connection;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetImages() {
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
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int userId = (int) session.getAttribute("currentUserId");

        try {
            ImmagineDAO immagineDAO = new ImmagineDAO(connection);
            Immagine[] immagini = immagineDAO.getAllUserPhoto(userId);

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.println(new Gson().toJson(immagini));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
