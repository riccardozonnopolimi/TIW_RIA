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

import it.polimi.tiw.DAO.AlbumDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.util.ConnectionHandler;

@WebServlet("/UpdateOrder")
public class UpdateOrder extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;

    public UpdateOrder() {
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
     * Riceve ad es.:
     *  - albumId = ...
     *  - order = "101,55,72,80" (lista di id immagine in ordine)
     *  GET o POST => albumId=?&order=??
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check login
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        User user = (User) session.getAttribute("currentUser");

        // Parametri
        String albumIdStr = request.getParameter("albumId");
        String orderStr = request.getParameter("order"); 
        // Esempio: "101,55,72,80"

        if (albumIdStr == null || orderStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int albumId;
        try {
            albumId = Integer.parseInt(albumIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Convertiamo la stringa in un array di int
        String[] parts = orderStr.split(",");
        int[] orderArray = new int[parts.length];
        try {
            for (int i=0; i<parts.length; i++) {
                orderArray[i] = Integer.parseInt(parts[i].trim());
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Chiama albumDAO.setOrder per aggiornare
        AlbumDAO albumDAO = new AlbumDAO(connection);
        try {
            // Esempio: controlla se albumId appartiene a user
            boolean isOwner = albumDAO.checkAlbumOwner(albumId, user.getId_user());
            if (!isOwner) {
                // non sei il proprietario => 403
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // Aggiorna l'ordine
            albumDAO.setOrder(albumId, orderArray);

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

