package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import it.polimi.tiw.beans.Commento;
import it.polimi.tiw.beans.Immagine;

public class ImmagineDAO {
	private final Connection connection;

    public ImmagineDAO(Connection connection) {
        this.connection = connection;
    }
    
    public Immagine getImageById(int id_image) throws SQLException {
    	String performedAction = " finding an image by id";
    	String performedAction2 = " finding all comment of a image by id_image ";
    	String query = "SELECT * FROM immagine WHERE id_image = ?";
    	String query2 = "SELECT c.id_comm FROM commento c WHERE c.id_im = ?";
    	PreparedStatement statement = null;
    	PreparedStatement statement2 = null;
    	ResultSet result = null;
    	ResultSet result2 = null;
    	
    	Immagine immagine = null;
    	CommentoDAO commentoDAO = new CommentoDAO(connection);
    	
        try {

            
            statement = connection.prepareStatement(query);
            statement.setInt(1, id_image);
            result = statement.executeQuery();   
            if(result.next()) {
                String titolo = result.getString("titolo");
                int proprietario = result.getInt("proprietario");
                String descrizione = result.getString("descrizione");
                Timestamp data = result.getTimestamp("data_c");
                String percorso = result.getString("percorso");
                int totaleCommenti = result.getInt("totale_commenti");
                Commento[] commenti = null;
                
	            if (totaleCommenti > 0) {
	                int[] id_comm = new int[totaleCommenti];
	                commenti = new Commento[totaleCommenti];
	                statement2 = connection.prepareStatement(query2);
	                statement2.setInt(1, id_image);
	                result2 = statement2.executeQuery();
	                int index = 0;
	                while (result2.next() && index < totaleCommenti) {
	                	id_comm[index] = result2.getInt("id_comm");
	                	index++;
	                    }
	                      
	                for (int i = 0; i < totaleCommenti; i++) {
	                	commenti[i] = commentoDAO.getCommentoById(id_comm[i]);
	                }
	            }
            immagine = new Immagine(id_image, titolo, descrizione, percorso, proprietario, data, commenti );
            }
        }catch (SQLException e) {
            throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
        } finally {
            try {
                if (result != null) {
                    result.close();
                }
            } catch (Exception e) {
                throw new SQLException("Error closing the result set when" + performedAction + "[ " + e.getMessage() + " ]");
            }

            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
                throw new SQLException("Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
            }
            try {
                if (result2 != null) {
                    result2.close();
                }
            } catch (Exception e) {
                throw new SQLException("Error closing the result set when" + performedAction2 + "[ " + e.getMessage() + " ]");
            }

            try {
                if (statement2 != null) {
                    statement2.close();
                }
            } catch (Exception e) {
                throw new SQLException("Error closing the statement when" + performedAction2 + "[ " + e.getMessage() + " ]");
            }
        }
    	return immagine;
    }
    
	public Immagine[] getAllUserPhoto(int id_user) throws SQLException {
 	    String performedAction = " finding all image of a user by id_user ";
    	String performedAction2 = " finding all comment of a image by id_image ";
    	String query2 = "SELECT c.id_comm FROM commento c WHERE c.id_im = ?";
    	PreparedStatement statement = null;
    	PreparedStatement statement2 = null;
    	ResultSet result = null;
    	ResultSet result2 = null;
 	    Immagine immagine = null;
 	    Immagine[] allPhotos = new Immagine[5];  
 	    CommentoDAO commentoDAO = new CommentoDAO(connection);

 	    try {
 	        String query1 = "SELECT * FROM immagine WHERE proprietario = ? ORDER BY immagine.data_c DESC";
 	        statement = connection.prepareStatement(query1);
 	        statement.setInt(1, id_user);
 	        result = statement.executeQuery();

 	        int i = 0;  

 	        while (result.next()) {
 	        	
 	            
 	            if (i == allPhotos.length) {
 	                allPhotos = resizeArray(allPhotos);
 	            }

 	            String titolo = result.getString("titolo");
 	            int id_user_proprietario = result.getInt("proprietario");
 	            Timestamp data = result.getTimestamp("data_c");
 	            String descrizione= result.getString("descrizione");
 	            String percorso = result.getString("percorso");
 	            int id_immagine = result.getInt("id_image");
 	            int totaleCommenti = result.getInt("totale_commenti");
 	            Commento[] commenti = null;

	            if (totaleCommenti > 0) {
	                int[] id_comm = new int[totaleCommenti];
	                 commenti = new Commento[totaleCommenti];

	                
	                	statement2 = connection.prepareStatement(query2);
	                    statement2.setInt(1, id_immagine);
	                    result2 = statement2.executeQuery();
	                        int index = 0;
	                        while (result2.next() && index < totaleCommenti) {
	                            id_comm[index] = result2.getInt("id_comm");
	                            index++;
	                    }
	                

	                for (int j = 0; j < totaleCommenti; j++) {
	                	commenti[j] = commentoDAO.getCommentoById(id_comm[j]);
	                }
	                
	            }
 	            immagine = new Immagine(id_immagine, titolo, descrizione, percorso, id_user_proprietario, data, commenti);
 	            
 	            allPhotos[i] = immagine;
 	            i++;
 	        }

 	        
 	        allPhotos = trimArray(allPhotos, i);

 	    } catch (SQLException e) {
 	        throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
 	    } finally {
 	    	try {
 	        closeResources(result, statement);
 	    	}catch (Exception e) {
                throw new SQLException("Error closing the resources when" + performedAction + "[ " + e.getMessage() + " ]");
 	    }
 	   	try {
 	        closeResources(result2, statement2);
 	    	}catch (Exception e) {
                throw new SQLException("Error closing the second resources when" + performedAction2 + "[ " + e.getMessage() + " ]");
 	    }
 	    }
 	    return allPhotos;
 	}

	public void linkImageToAlbum(int imageId, int albumId, int userId) throws SQLException {
		String performedAction = "create record in image_album to link an image to an album";
        String query = "INSERT INTO image_album (id_ima, id_alb, id_use, posizione) VALUES (?, ?, ?, ?)";
        PreparedStatement statement = null;
        int posizione;
        AlbumDAO albumDAO= new AlbumDAO(connection);
        posizione = albumDAO.lastPositionUsed(albumId);
        
        
        try  {
        	statement = connection.prepareStatement(query);
            statement.setInt(1, imageId);
            statement.setInt(2, albumId);
            statement.setInt(3, userId);
            statement.setInt(4, posizione);
            statement.executeUpdate();
        } catch (SQLException e) {
 	        throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
 	    } finally {
 	        statement.close();
 	    }
    }
	
	public void delinkImageToAlbum(int imageId) throws SQLException {
		String performedAction = "delete record in image_album to delink an image from an album";
        String query = "DELETE FROM image_album WHERE id_ima = ?";
        PreparedStatement statement = null;
        try  {
        	statement = connection.prepareStatement(query);
            statement.setInt(1, imageId);
            statement.executeUpdate();
        } catch (SQLException e) {
 	        throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
 	    } finally {
 	        statement.close();
 	    }
    }
	
    public int insertImageData( String title, String description, String filePath, int userId) throws SQLException {
    	String performedAction = "create a new image";
        String query = "INSERT INTO immagine (titolo, percorso, descrizione, proprietario) VALUES (?, ?, ?, ?)";
        PreparedStatement statement = null;
   
        try  {
        	statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, title);
            statement.setString(2, filePath);
            statement.setString(3, description);
            statement.setInt(4, userId);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                	throw new SQLException("Errore nell'inserimento dell'immagine, nessun ID generato.");
                }
            }
        } catch (SQLException e) {
 	        throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
 	    } finally {
 	        statement.close();
 	    }
    }
	
 	private Immagine[] resizeArray(Immagine[] original) {
 	    int newSize = original.length * 2;
 	    Immagine[] newArray = new Immagine[newSize];
 	    System.arraycopy(original, 0, newArray, 0, original.length);
 	    return newArray;
 	}

 	private Immagine[] trimArray(Immagine[] original, int size) {
 	    Immagine[] trimmedArray = new Immagine[size];
 	    System.arraycopy(original, 0, trimmedArray, 0, size);
 	    return trimmedArray;
 	}
 	
 	private void closeResources(ResultSet resultSet, PreparedStatement statement) throws SQLException {
 	    if (resultSet != null) {
 	        try {
 	            resultSet.close();
 	        } catch (SQLException e) {
 	            throw new SQLException("Error closing ResultSet: " + e.getMessage());
 	        }
 	    }
 	    if (statement != null) {
 	        try {
 	            statement.close();
 	        } catch (SQLException e) {
 	            throw new SQLException("Error closing PreparedStatement: " + e.getMessage());
 	        }
 	    }
 	}

	
	public void deleteImageById(int imageId) throws SQLException {
		String performedAction = "deleting an image ";
	    String query = "DELETE FROM immagine WHERE id_image = ?";
	    PreparedStatement statement = null;
	    try {
	    	statement = connection.prepareStatement(query);
	        statement.setInt(1, imageId);
	        statement.executeUpdate();
	    }catch (SQLException e) {
	            throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
	    } finally {
	    	statement.close();
	    }
	}


	public String getImagePathById(int imageId) throws SQLException {
	    String query = "SELECT percorso FROM immagine WHERE id_image = ?";
	    PreparedStatement statement = null;
	    ResultSet resultSet = null;
	    try {
	    	statement = connection.prepareStatement(query);
	        statement.setInt(1, imageId);
	        resultSet= statement.executeQuery();
	        if (resultSet.next()) {
	        	return resultSet.getString("percorso");
	        	} else {
	                throw new SQLException("Nessuna immagine trovata con ID: " + imageId);
	            }
	    } finally {
	    	closeResources(resultSet, statement);
	    }
	}

	
	public void updateTotCommentiByImageId(int id_immagine) throws SQLException {

    	String performedAction = " updating totale_commenti in immagine " + Integer.toString(id_immagine);
        String query = "UPDATE immagine SET totale_commenti = totale_commenti + 1 WHERE id_image = ?";
        PreparedStatement statement = null;
        try  {
        	statement = connection.prepareStatement(query);
            statement.setInt(1, id_immagine);
            statement.executeUpdate();
        }catch (SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
        } finally {
        	try {
			statement.close();
			} catch (Exception e) {
				throw new SQLException(
						"Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
			}
 	    }
    }
	
	public Immagine[] getAllUserImageExceptAlbum(int userId, int albumId) throws SQLException {
 	    String performedAction = " finding all images of a user except images in album " + albumId;
 	    PreparedStatement statement = null;
 	    ResultSet result = null;
 	    PreparedStatement statement2 = null;
 	    ResultSet result2 = null;
 	    Immagine immagine = null;
 	    Immagine[] allPhotos = new Immagine[5];  
 	    
 	    
 	    try {
 	    	String query1 = "SELECT i1.* "
 	    			+ "FROM immagine AS i1 "
 	    			+ "LEFT JOIN image_album AS ia ON i1.id_image = ia.id_ima "
 	    			+ "AND ia.id_alb = ? "
 	    			+ "WHERE i1.proprietario = ? "
 	    			+ "AND ia.id_ima IS NULL";


 	        		
 	        statement = connection.prepareStatement(query1);
 	        statement.setInt(1, albumId);
 	        statement.setInt(2, userId);
 	        result = statement.executeQuery();

 	        int i = 0;  

 	        while (result.next()) {
 	            if (i == allPhotos.length) {
 	                allPhotos = resizeArray(allPhotos);
 	            }

 	            String titolo = result.getString("titolo");
 	            int id_user_proprietario = result.getInt("proprietario");
 	            Timestamp data = result.getTimestamp("data_c");
 	            String descrizione= result.getString("descrizione");
 	            String percorso = result.getString("percorso");
 	            int id_immagine = result.getInt("id_image");
 	            int totale_commenti = result.getInt("totale_commenti");
 	            Commento[] commenti = new Commento[totale_commenti];
 	            if(totale_commenti > 0) {
 	            	try {
 	            	int j = 0;
 	            	String query2 = "SELECT * FROM commento WHERE id_im = ?";
	                statement2 = connection.prepareStatement(query2);
	                statement2.setInt(1, id_immagine);
	                result2 = statement2.executeQuery();
 	            	int[] id_commenti = new int[totale_commenti];
 	            	
 	            	CommentoDAO commentoDAO = new CommentoDAO(connection);
 	            	
	                while (result2.next()) {
	                    id_commenti[j] = result2.getInt("id_ima");
	                    j++;
	                }
 	            
 	            	for(int k = 0; k < totale_commenti; k++) {
 	            		commenti[k] = commentoDAO.getCommentoById(id_commenti[k]);
 	            	}
 	            	}catch (SQLException e) {
 	        	        throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
 	        	    } finally {
 	        	        closeResources(result2, statement2);
 	        	    }
 	            }       
 	            
 	            immagine = new Immagine(id_immagine, titolo, descrizione, percorso, id_user_proprietario, data, commenti);
 	            allPhotos[i] = immagine;
 	            i++;
 	        }
 	        allPhotos = trimArray(allPhotos, i);

 	    } catch (SQLException e) {
 	        throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
 	    } finally {
 	        closeResources(result, statement);
 	        
 	    }
 	    return allPhotos;
 	}

}