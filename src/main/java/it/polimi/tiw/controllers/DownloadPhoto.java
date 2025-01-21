package it.polimi.tiw.controllers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.DAO.ImmagineDAO;
import it.polimi.tiw.util.ConnectionHandler;


/**
 * Servlet implementation class DownloadPhoto
 */
@WebServlet("/DownloadPhoto")
public class DownloadPhoto extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DownloadPhoto() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws ServletException {

        ServletContext servletContext = getServletContext();

        this.connection = ConnectionHandler.getConnection(servletContext);
    }
    
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
        String imageId = request.getParameter("imageId");
        if (imageId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            ImmagineDAO immagineDAO = new ImmagineDAO(connection);
            String imagePath = immagineDAO.getImagePathById(Integer.parseInt(imageId));

            File file = new File(imagePath);
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            String mimeType = getServletContext().getMimeType(file.getName());
            if (mimeType == null) {
                mimeType = "application/octet-stream";  
            }
            response.setContentType(mimeType);
            response.setContentLengthLong(file.length());
            OutputStream out = response.getOutputStream();
            response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");Files.copy(file.toPath(), out);
            out.flush();
            out.close();
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
