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
import it.polimi.tiw.DAO.ImmagineDAO;
import it.polimi.tiw.DAO.UserDAO;
import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.AlbumData;
import it.polimi.tiw.beans.Immagine;
import it.polimi.tiw.beans.ImmagineData;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.beans.UserData;
import it.polimi.tiw.util.ConnectionHandler;

@WebServlet("/Login")
public class Login extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public Login() {
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
        // Reindirizziamo a doPost
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Rispondiamo in JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Recupero parametri
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Crea un oggetto UserData (per la risposta)
        UserData userData;

        // Check parametri
        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            userData = new UserData(false, "Email or password missing");
            writeJsonResponse(response, userData);
            return;
        }

        // Verifica credenziali con UserDAO
        UserDAO userDAO = new UserDAO(connection);
        User user = null;
        try {
            user = userDAO.getUserLog(email, password);
        } catch (SQLException e) {
            userData = new UserData(false, "Database error: " + e.getMessage());
            writeJsonResponse(response, userData);
            return;
        }

        // Se l'utente non esiste => login fallita
        if (user == null) {
            userData = new UserData(false, "Email or password incorrect!");
            writeJsonResponse(response, userData);
            return;
        }

        // LOGIN OK => impostiamo la sessione
        HttpSession session = request.getSession();
        session.setAttribute("currentUser", user);
        session.setAttribute("currentUserId", user.getId_user());
        session.setAttribute("currentUserUsername", user.getUsername());

        // Creiamo un UserData con success = true e dati utente
        userData = new UserData(user);
        // Se vuoi cambiare "message" di default
        // userData.setMessage("Welcome user " + user.getUsername() + "!");

        // Carichiamo i dati extra
        AlbumDAO albumDAO = new AlbumDAO(connection);
        ImmagineDAO immagineDAO = new ImmagineDAO(connection);

        Album[] userAlbums;
        Album[] otherAlbums;
        Immagine[] userImmagini;

        try {
            userAlbums = albumDAO.getAllUserAlbum2(user.getId_user());
        } catch (SQLException e) {
            userData.setSuccess(false);
            userData.setMessage("Error retrieving userAlbums: " + e.getMessage());
            writeJsonResponse(response, userData);
            return;
        }
        try {
            otherAlbums = albumDAO.getAllOtherUserAlbums(user.getId_user());
        } catch (SQLException e) {
            userData.setSuccess(false);
            userData.setMessage("Error retrieving otherAlbums: " + e.getMessage());
            writeJsonResponse(response, userData);
            return;
        }
        try {
            userImmagini = immagineDAO.getAllUserPhoto(user.getId_user());
        } catch (SQLException e) {
            userData.setSuccess(false);
            userData.setMessage("Error retrieving userImages: " + e.getMessage());
            writeJsonResponse(response, userData);
            return;
        }

        // Convertiamo in data
        AlbumData[] userAlbumsData = (userAlbums != null)
            ? Arrays.stream(userAlbums).map(AlbumData::new).toArray(AlbumData[]::new)
            : new AlbumData[0];

        AlbumData[] otherAlbumsData = (otherAlbums != null)
            ? Arrays.stream(otherAlbums).map(AlbumData::new).toArray(AlbumData[]::new)
            : new AlbumData[0];

        ImmagineData[] userImagesData = (userImmagini != null)
            ? Arrays.stream(userImmagini).map(ImmagineData::new).toArray(ImmagineData[]::new)
            : new ImmagineData[0];

        // Assegnali a userData
        userData.setMyAlbums(userAlbumsData);
        userData.setOtherAlbums(otherAlbumsData);
        userData.setMyImages(userImagesData);

        // Invio JSON finale
        writeJsonResponse(response, userData);
    }

    private void writeJsonResponse(HttpServletResponse response, UserData data) 
            throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }
}

