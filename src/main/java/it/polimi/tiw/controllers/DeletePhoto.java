package it.polimi.tiw.controllers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import it.polimi.tiw.DAO.AlbumDAO;
import it.polimi.tiw.DAO.CommentoDAO;
import it.polimi.tiw.DAO.ImmagineDAO;
import it.polimi.tiw.util.ConnectionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/DeletePhoto")
public class DeletePhoto extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;

    public DeletePhoto() {
        super();
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

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        
        String imageIdStr = request.getParameter("imageId");
        int imageId;
        try {
            imageId = Integer.parseInt(imageIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        CommentoDAO commentoDAO = new CommentoDAO(connection);
        ImmagineDAO immagineDAO = new ImmagineDAO(connection);
        AlbumDAO albumDAO = new AlbumDAO(connection);

        try {
            String filePath = immagineDAO.getImagePathById(imageId);
            if (filePath == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            albumDAO.refactorPosition(imageId);
            
            commentoDAO.deleteAllCommentsByImageId(imageId);

            albumDAO.decrementAlbumImageCount(imageId);

            immagineDAO.delinkImageToAlbum(imageId);
            
            immagineDAO.deleteImageById(imageId);

            File file = new File(filePath);
            if (file.exists()) {
                if (!file.delete()) {
                    System.err.println("Errore: non è stato possibile eliminare il file " + filePath);
                }
            }

            response.setStatus(HttpServletResponse.SC_OK);

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}

