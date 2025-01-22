package it.polimi.tiw.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import it.polimi.tiw.DAO.ImmagineDAO;
import it.polimi.tiw.util.ConnectionHandler;

@WebServlet("/UploadPhoto")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1,  // 1KB
    maxFileSize = 1024 * 1024 * 10,  // 10MB
    maxRequestSize = 1024 * 1024 * 50 // 50MB
)
public class UploadPhoto extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;

    public UploadPhoto() {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int userId = (int) session.getAttribute("currentUserId");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String uploadPath = "/Users/riccardozonno/res_ria";
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }

        Random random = new Random();
        int n = random.nextInt(1000000);
        String rand = Integer.toString(n);

        try {
            Part filePart = request.getPart("file");
            String originalFilename = Paths.get(filePart.getSubmittedFileName())
                                           .getFileName().toString();
            String fileName = rand + originalFilename;
            String filePath = uploadDir + File.separator + fileName;
            filePart.write(filePath);
            File uploadedFile = new File(filePath);
            if (uploadedFile.exists()) {
            } else {
                System.err.println("Errore: il file non è stato scritto.");
            }
            String dbPath = "/Users/riccardozonno/res_ria" + "/" + fileName;
            ImmagineDAO immagineDAO = new ImmagineDAO(connection);
            immagineDAO.insertImageData(title, description, dbPath, userId);

            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
}

