package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import it.polimi.tiw.beans.Commento;


public class CommentoDAO {

	private final Connection connection;

    public CommentoDAO(Connection connection) {
        this.connection = connection;
    }
    
    public Commento getCommentoById(int id) throws SQLException {
		String performedAction = " finding a comment by id_comm ";
		String query = "SELECT * FROM commento WHERE id_comm = ?";
	    PreparedStatement statement = null;
	    ResultSet result = null;
	    Commento commento = null;
	    
	    try {
 	        statement = connection.prepareStatement(query);
 	        statement.setInt(1, id);
 	        result = statement.executeQuery();

 	        if (result.next()) 
 	        	commento = new Commento(id, result.getString("testo"), result.getInt("proprietario"), result.getInt("id_im"), result.getTimestamp("data_c"), result.getString("username"));
 	    } catch (SQLException e) {
 	        throw new SQLException("Error accessing the DB when " + performedAction + "[ " + e.getMessage() + " ]");
 	    } finally {
 	        
 	        if (result != null) {
 	            try {
 	                result.close();
 	            } catch (SQLException e) {
 	                throw new SQLException("Error closing the result set when " + performedAction + "[ " + e.getMessage() + " ]");
 	            }
 	        }

 	        
 	        if (statement != null) {
 	            try {
 	                statement.close();
 	            } catch (SQLException e) {
 	                throw new SQLException("Error closing the statement when " + performedAction + "[ " + e.getMessage() + " ]");
 	            }
 	        }
 	    }
	    return commento;
    }

	public void createCommento(String commento, int id_user, int id_immagine, String username) throws SQLException {
		String performedAction = "insert a new comment";
        String query = "INSERT INTO commento (testo, proprietario, id_im, username) VALUES (?, ?, ?, ?)";
        PreparedStatement statement = null;
        try  {
        	connection.setAutoCommit(false);
        	statement = connection.prepareStatement(query);
            statement.setString(1, commento);
            statement.setInt(2, id_user);
            statement.setInt(3, id_immagine);
            statement.setString(4, username);
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
        	connection.rollback();
 	        throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
 	    } finally {
 	        statement.close();
 	       connection.setAutoCommit(true);

 	    }
    }

	
	
	public void deleteAllCommentsByImageId(int id_immagine) throws SQLException {
		String performedAction = "deleting all comment of an image ";
	    String query = "DELETE FROM commento WHERE id_im = ?";
	    PreparedStatement statement = null;
	    try {
	    	connection.setAutoCommit(false);
	    	statement = connection.prepareStatement(query);
	        statement.setInt(1, id_immagine);
	        statement.executeUpdate();
	        connection.commit();
	    }catch (SQLException e) {
	    	connection.rollback();
	            throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
	    } finally {
	    	statement.close();
	    	connection.setAutoCommit(true);

	    }
	}
	
}
