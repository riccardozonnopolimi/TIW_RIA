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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import it.polimi.tiw.DAO.ImmagineDAO;
import it.polimi.tiw.util.ConnectionHandler;

@WebServlet("/DownloadPhoto")
public class DownloadPhoto extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;

    public DownloadPhoto() {
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
     * Gestisce GET: "DownloadPhoto?imageId=xxx"
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Verifica sessione e utente loggato
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            // Se non loggato => 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 2. Recupera il parametro "imageId"
        String imageIdParam = request.getParameter("imageId");
        if (imageIdParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int imageId;
        try {
            imageId = Integer.parseInt(imageIdParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 3. Recupera il percorso dal DB (tramite ImmagineDAO)
        String imagePath;
        try {
            ImmagineDAO immagineDAO = new ImmagineDAO(connection);
            imagePath = immagineDAO.getImagePathById(imageId);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        if (imagePath == null) {
            // Non esiste quell'immagine
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File file = new File(imagePath);
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 4. Determiniamo il mimeType
        String mimeType = getServletContext().getMimeType(file.getName());
        if (mimeType == null) {
            // fallback
            mimeType = "application/octet-stream";
        }

        // 5. Scriviamo l'immagine nel response
        response.setContentType(mimeType);
        response.setContentLengthLong(file.length());
        // "inline" => visualizza l'immagine nel browser
        response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");

        try (OutputStream out = response.getOutputStream()) {
            Files.copy(file.toPath(), out);
            out.flush();
        } catch (IOException e) {
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

