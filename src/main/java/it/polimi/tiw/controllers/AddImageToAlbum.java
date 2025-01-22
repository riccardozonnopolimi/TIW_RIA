package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.DAO.AlbumDAO;
import it.polimi.tiw.DAO.ImmagineDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.util.ConnectionHandler;

@WebServlet("/AddImageToAlbum")
public class AddImageToAlbum extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;

    public AddImageToAlbum() {
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

    /**
     * Gestisce GET: /AddImageToAlbum?albumId=xxx&imageId=yyy
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Recupera userId
        User currentUser = (User) session.getAttribute("currentUser");
        int userId = currentUser.getId_user();

        // Legge parametri albumId, imageId
        String albumIdParam = request.getParameter("albumId");
        String imageIdParam = request.getParameter("imageId");
        if (albumIdParam == null || imageIdParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int albumId, imageId;
        try {
            albumId = Integer.parseInt(albumIdParam);
            imageId = Integer.parseInt(imageIdParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        ImmagineDAO immagineDAO = new ImmagineDAO(connection);
        AlbumDAO albumDAO = new AlbumDAO(connection);

        try {
            // Aggiunge il link (riga in tabella di join, ecc.)
            immagineDAO.linkImageToAlbum(imageId, albumId, userId);

            // Aggiorna contatore di immagini in quell’album
            albumDAO.updateAlbumImageCount(connection, albumId);

            // OK => status 200
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
