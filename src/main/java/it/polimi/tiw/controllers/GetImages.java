package it.polimi.tiw.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;

import it.polimi.tiw.DAO.ImmagineDAO;
import it.polimi.tiw.beans.Immagine;
import it.polimi.tiw.beans.ImmagineData;
import it.polimi.tiw.util.ConnectionHandler;

@WebServlet("/GetImages")
public class GetImages extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public GetImages() {
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

        int userId = (int) session.getAttribute("currentUserId");

        ImmagineDAO immagineDAO = new ImmagineDAO(connection);
        Immagine[] userImages;
        try {
            userImages = immagineDAO.getAllUserPhoto(userId);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        ImmagineData[] imagesData;
        if (userImages == null) {
            imagesData = new ImmagineData[0];
        } else {
            imagesData = Arrays.stream(userImages)
                               .map(ImmagineData::new)
                               .toArray(ImmagineData[]::new);
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Gson gson = new Gson();
        String json = gson.toJson(imagesData);
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
