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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.DAO.AlbumDAO;
import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.AlbumData;
import it.polimi.tiw.util.ConnectionHandler;

@WebServlet("/GetAlbums")
public class GetAlbums extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public GetAlbums() {
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
        // Controllo sessione
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Recupera userId
        int userId = (int) session.getAttribute("currentUserId");

        // Esegui query per recuperare tutti gli album di quell'utente
        AlbumDAO albumDAO = new AlbumDAO(connection);
        Album[] userAlbums = null;

        try {
            // Metti un tuo metodo, ad es. "getAllUserAlbum2" o simile
            userAlbums = albumDAO.getAllUserAlbum2(userId);
        } catch (SQLException e) {
            // Errore DB => 500
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // Converti in array di AlbumData (per serializzare in JSON)
        AlbumData[] userAlbumsData;
        if (userAlbums == null) {
            userAlbumsData = new AlbumData[0];
        } else {
            userAlbumsData = Arrays.stream(userAlbums)
                                   .map(AlbumData::new)
                                   .toArray(AlbumData[]::new);
        }

        // Serializza in JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        out.print(gson.toJson(userAlbumsData));
        out.flush();
    }

    // Se la tua UI fa una GET, la doPost potresti mappare a doGet
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}

