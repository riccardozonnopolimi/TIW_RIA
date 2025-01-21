package it.polimi.tiw.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.DAO.UserDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.util.ConnectionHandler;

@WebServlet("/Register")
public class Register extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;

    public Register() {
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
        doPost(request, response);
    }

    /**
     * doPost:
     * - Controlla i parametri.
     * - Se errori => setStatus(400) e body di testo con il messaggio di errore.
     * - Se errore DB => setStatus(500) e body con messaggio d'errore.
     * - Se tutto OK => setStatus(200).
     * Nessun forward/redirect: la logica è gestita dal front-end in AJAX.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        // Indichiamo che rispondiamo in testo semplice (plain text).
        // Se vuoi, potresti lasciare text/html, ma di solito text/plain è più lineare.
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        // Recupera i parametri
        String email = request.getParameter("email");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String repeat_pwd = request.getParameter("repeat_pwd");

        // 1. Check di base
        if (email == null || username == null || password == null || repeat_pwd == null 
                || email.isBlank() || username.isBlank() || password.isBlank() || repeat_pwd.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.println("Missing data for registration");
            }
            return;
        }

        // 2. Check formale email
        String emailRegEx = "^(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\."
                + "[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c"
                + "\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09"
                + "\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*"
                + "[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|" 
                + "\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?"
                + "[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|"
                + "[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b"
                + "\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09"
                + "\\x0b\\x0c\\x0e-\\x7f])+))$";

        if (!email.matches(emailRegEx)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.println("Chosen email is invalid!");
            }
            return;
        }

        // 3. Check lunghezza username
        if (username.length() <= 0 || username.length() > 40) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.println("Chosen username is invalid!");
            }
            return;
        }

        // 4. Check password e ripetizione
        if (password.length() <= 0 || password.length() > 40) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.println("Chosen password is invalid!");
            }
            return;
        }

        if (!password.equals(repeat_pwd)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.println("Password and repeat password do not match!");
            }
            return;
        }

        // 5. Check unicità su DB
        UserDAO userDAO = new UserDAO(connection);
        try {
            // Verifichiamo se email esiste già
            User userByEmail = userDAO.findUserByEmail(email);
            if (userByEmail != null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                try (PrintWriter out = response.getWriter()) {
                    out.println("Chosen email already in use!");
                }
                return;
            }

            // Verifichiamo se username esiste già
            User userByUsername = userDAO.findUserByUsername(username);
            if (userByUsername != null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                try (PrintWriter out = response.getWriter()) {
                    out.println("Chosen username already in use!");
                }
                return;
            }

            // 6. Creiamo l'utente
            userDAO.createUser(username, email, password);

            // Verifichiamo che sia stato effettivamente creato
            User newUser = userDAO.findUserByEmail(email);
            if (newUser == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                try (PrintWriter out = response.getWriter()) {
                    out.println("Error: user not correctly created!");
                }
                return;
            }

            // SE TUTTO OK => 200
            response.setStatus(HttpServletResponse.SC_OK);

            // A tuo piacere, potresti anche scrivere un messaggio nel body:
            //  es: "Registration successful. You can now log in."
            //  Ma se preferisci un body vuoto, puoi omettere.
            //  Lato front-end, se status=200, mostrerai in pagina il messaggio di successo.
            try (PrintWriter out = response.getWriter()) {
                out.println("OK"); // oppure "Registration successful"
            }

        } catch (SQLException e) {
            // Errore a livello DB
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.println("DB Error: " + e.getMessage());
            }
        }
    }
}
