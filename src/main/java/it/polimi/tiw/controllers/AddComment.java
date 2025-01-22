package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import it.polimi.tiw.DAO.CommentoDAO;
import it.polimi.tiw.DAO.ImmagineDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.util.ConnectionHandler;

@WebServlet("/AddComment")
public class AddComment extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;

    public AddComment() {
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
     * Esempio: GET /AddComment?imageId=xxx&albumId=yyy&testo=COMMENTO
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

        // Recupera parametri
        String testo = request.getParameter("testo");
        String imageIdParam = request.getParameter("imageId");
        String albumIdParam = request.getParameter("albumId");

        if (testo == null || imageIdParam == null || albumIdParam == null 
                || testo.isBlank() || imageIdParam.isBlank() || albumIdParam.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int imageId, albumId;
        try {
            imageId = Integer.parseInt(imageIdParam);
            albumId = Integer.parseInt(albumIdParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Info utente
        User currentUser = (User) session.getAttribute("currentUser");
        int userId = currentUser.getId_user();
        String username = currentUser.getUsername();

        CommentoDAO commentoDAO = new CommentoDAO(connection);
        ImmagineDAO immagineDAO = new ImmagineDAO(connection);

        try {
            // Crea il commento
            commentoDAO.createCommento(testo, userId, imageId, username);

            // Aggiorna contatore commenti in tabella immagine (se necessario)
            immagineDAO.updateTotCommentiByImageId(imageId);

            response.setStatus(HttpServletResponse.SC_OK);

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Se arriva POST, la inoltriamo a doGet
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}

