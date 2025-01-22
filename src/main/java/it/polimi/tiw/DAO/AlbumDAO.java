package it.polimi.tiw.DAO;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Comparator;

import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.Immagine;

public class AlbumDAO {
	private final Connection connection;

	public AlbumDAO(Connection connection) {
		this.connection = connection;
	}
	
	public Album getAlbumById(int id_album) throws SQLException {
	    String performedAction = " finding an album by id_album ";
	    PreparedStatement statement = null;
	    ResultSet result = null;
	    Album album = null;

	    String queryAlbum = "SELECT * FROM album WHERE id_album = ?";
	    String queryImages = "SELECT id_ima FROM image_album WHERE id_alb = ?";

	    try {
	        // Recupera i dati dell'album
	        statement = connection.prepareStatement(queryAlbum);
	        statement.setInt(1, id_album);
	        result = statement.executeQuery();

	        if (result.next()) {
	            album = new Album(
	                result.getInt("id_album"),
	                result.getString("titolo"),
	                result.getInt("creatore"),
	                result.getInt("totale_immagini"),
	                result.getTimestamp("data_creazione")
	            );

	            int totaleImmagini = album.getTotale_immagini();

	            // Inizializza l'array di ID delle immagini
	            if (totaleImmagini > 0) {
	            	boolean flag = false;
	                int[] id_immagini = new int[totaleImmagini];
	                Immagine[] immagini = new Immagine[totaleImmagini];
	                int[] posizioni = new int[album.getTotale_immagini()];
	                try (PreparedStatement statement2 = connection.prepareStatement(queryImages)) {
	                    statement2.setInt(1, id_album);
	                    try (ResultSet result2 = statement2.executeQuery()) {
	                        int index = 0;
	                        while (result2.next() && index < totaleImmagini) {
	                            id_immagini[index] = result2.getInt("id_ima");
	        	                posizioni[index] = result2.getInt("posizione");
	        	                if(posizioni[index] > 0)
	        	                   	flag = true;
	                            index++;
	                        }
	                    }
	                }

	                // Carica le immagini dal database
	                ImmagineDAO imageDao = new ImmagineDAO(connection);
	                for (int i = 0; i < totaleImmagini; i++) {
	                    immagini[i] = imageDao.getImageById(id_immagini[i]);
	                }

	                album.setId_immagini(id_immagini);
	                immagini = orderImage(immagini, flag,posizioni);
	                album.setImmagini(immagini);
	                album.setImmagini(immagini);
	            }
	        }
	    } catch (SQLException e) {
	        throw new SQLException("Error accessing the DB when " + performedAction + "[ " + e.getMessage() + " ]");
	    } finally {
	        // Chiude il ResultSet
	        if (result != null) {
	            try {
	                result.close();
	            } catch (SQLException e) {
	                throw new SQLException("Error closing the result set when " + performedAction + "[ " + e.getMessage() + " ]");
	            }
	        }

	        // Chiude il PreparedStatement
	        if (statement != null) {
	            try {
	                statement.close();
	            } catch (SQLException e) {
	                throw new SQLException("Error closing the statement when " + performedAction + "[ " + e.getMessage() + " ]");
	            }
	        }
	    }
	    return album;
	}

	public Album getAlbumByTitolo(String titolo) throws SQLException{
		
		String performedAction = " finding an album by titolo ";
		PreparedStatement statement = null;
		ResultSet result = null;
		Album album = null;
		String query = "SELECT * FROM album WHERE titolo = ?";

		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, titolo);
			result = statement.executeQuery();
			if (result.next()) {
				album = new Album(result.getInt("id_album"), result.getString("titolo"), result.getInt("creatore"), result.getInt("totale_immagini"), result.getTimestamp("data_creazione"));
			}
		} catch (SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
		} finally {
			try {
				result.close();
			} catch (Exception e) {
				throw new SQLException(
						"Error closing the result set when" + performedAction + "[ " + e.getMessage() + " ]");
			}

			try {
				statement.close();
			} catch (Exception e) {
				throw new SQLException(
						"Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
			}
		}
		return album;
		
	}
	
 	public long createAlbum(String titolo, int creatore) throws SQLException{
		String performedAction = " creating an album ";
		PreparedStatement statement = null;
		String query = "INSERT INTO album (titolo, creatore) VALUES (?, ?)";
		String query2 = "SELECT id_album FROM album WHERE titolo = ? ORDER BY id_album DESC LIMIT 1";
		ResultSet result;
		PreparedStatement statement2;
		long generatedId = 0;

		try {

			statement = connection.prepareStatement(query);
			statement.setString(1, titolo);
			statement.setInt(2, creatore);
            statement.executeUpdate();
            
            statement2 = connection.prepareStatement(query2);
			statement2.setString(1, titolo);
			result = statement2.executeQuery();
			
			if (result.next()) {
			    generatedId = result.getLong("id_album");
			}

			
			return generatedId;
		}
		 catch (SQLException e) {
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
 	
    public void updateAlbumImageCount(Connection connection, int albumId) throws SQLException {
    	String performedAction = " updating totale_immagini in album " + Integer.toString(albumId);
        String query = "UPDATE album SET totale_immagini = totale_immagini + 1 WHERE id_album = ?";
        PreparedStatement statement = null;
        try  {
        	statement = connection.prepareStatement(query);
            statement.setInt(1, albumId);
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
 	
 	public Album[] getAllOtherUserAlbums(int id_user) throws SQLException {
 	    String performedAction = " finding all albums of users different from id_user ";
 	    PreparedStatement statement = null;
 	    ResultSet result = null;
 	    Album album = null;
 	    Album[] allAlbums = new Album[5];  // Array iniziale di 5 posizioni
 	    PreparedStatement statement2 = null;
 	    ResultSet result2 = null;

 	    try {
 	        //  Query per selezionare tutti gli album tranne quelli dell'utente specificato
 	        String query1 = "SELECT a.*, u.username FROM album a JOIN user u ON a.creatore = u.id_user WHERE a.creatore <> ? ORDER BY a.data_creazione DESC";
 	        statement = connection.prepareStatement(query1);
 	        statement.setInt(1, id_user);
 	        result = statement.executeQuery();

 	        int i = 0;  // Indice per l'array

 	        while (result.next()) {
 	            //  Raddoppio la dimensione dell'array se pieno
 	            if (i == allAlbums.length) {
 	                allAlbums = resizeArray(allAlbums);
 	            }

 	            String titolo = result.getString("titolo");
 	            int id_user_proprietario = result.getInt("creatore");
 	            Timestamp data = result.getTimestamp("data_creazione");
 	            int totale_immagini = result.getInt("totale_immagini");
 	            int id_album = result.getInt("id_album");
 	            String username_proprietario = result.getString("username"); 

 	            album = new Album(id_album, titolo, id_user_proprietario, totale_immagini, data);
 	           album.setUsernameCreatore(username_proprietario);

 	            // Recupero le immagini associate all'album
 	            if (album.getTotale_immagini() > 0) {
 	                String query2 = "SELECT * FROM image_album WHERE id_alb = ?";
 	                statement2 = connection.prepareStatement(query2);
 	                statement2.setInt(1, album.getId_album());
 	                result2 = statement2.executeQuery();

 	                int j = 0;
 	                int[] id_immagini = new int[album.getTotale_immagini()];
 	                while (result2.next()) {
 	                    id_immagini[j] = result2.getInt("id_ima");
 	                    j++;
 	                }

 	                Immagine[] immagini = new Immagine[album.getTotale_immagini()];
 	                for (int k = 0; k < album.getTotale_immagini(); k++) {
 	                    ImmagineDAO imageDao = new ImmagineDAO(connection);
 	                    immagini[k] = imageDao.getImageById(id_immagini[k]);
 	                }

 	                album.setImmagini(immagini);
 	            }

 	            allAlbums[i] = album;
 	            i++;
 	        }

 	        //  Se non sono stati trovati album, ritorna null
 	        if (i == 0) {
 	            return null;
 	        }

 	        //  Ridimensiono l'array alla dimensione reale
 	        allAlbums = trimArray(allAlbums, i);

 	    } catch (SQLException e) {
 	        throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
 	    } finally {
 	        closeResources(result, statement);
 	        closeResources(result2, statement2);
 	    }

 	    return allAlbums;
 	}

 	public Album[] getAllUserAlbum2(int id_user) throws SQLException {
 	    String performedAction = " finding all album of a user by id_user ";
 	    PreparedStatement statement = null;
 	    ResultSet result = null;
 	    Album album = null;
 	    Album[] allAlbum = null;  // Inizializzo l'array con 5 posizioni
 	    PreparedStatement statement2 = null;
 	    ResultSet result2 = null;
 	    boolean alb = false;

 	    try {
 	        String query1 = "SELECT a.*, u.username FROM album a JOIN user u ON a.creatore = u.id_user WHERE creatore = ? ORDER BY a.data_creazione DESC";
 	        statement = connection.prepareStatement(query1);
 	        statement.setInt(1, id_user);
 	        result = statement.executeQuery();

 	        int i = 0;  // Indice per scorrere l'array

 	        while (result.next()) {
 	        	if(i==0) {
 	        		alb = true;
 	        		allAlbum = new Album[5];
 	        	}
 	            // Se l'array è pieno, lo raddoppio
 	            if (i == allAlbum.length) {
 	                allAlbum = resizeArray(allAlbum);
 	            }

 	            String titolo = result.getString("titolo");
 	            int id_user_proprietario = result.getInt("creatore");
 	            Timestamp data = result.getTimestamp("data_creazione");
 	            int totale_immagini = result.getInt("totale_immagini");
 	            int id_album = result.getInt("id_album");
 	            String username_proprietario = result.getString("username"); 

 	            album = new Album(id_album, titolo, id_user_proprietario, totale_immagini, data);
 	            album.setUsernameCreatore(username_proprietario);
 	           
 	            if (album.getTotale_immagini() > 0) {
 	                String query2 = "SELECT * FROM image_album WHERE id_alb = ?";
 	                statement2 = connection.prepareStatement(query2);
 	                statement2.setInt(1, album.getId_album());
 	                result2 = statement2.executeQuery();
 	                boolean flag = false;

 	                int j = 0;
 	                int[] id_immagini = new int[album.getTotale_immagini()];
 	                int[] posizioni = new int[album.getTotale_immagini()];
 	                while (result2.next()) {
 	                    id_immagini[j] = result2.getInt("id_ima");
 	                    posizioni[j] = result2.getInt("posizione");
 	                    if(posizioni[j] > 0)
 	                    	flag = true;
 	                    j++;
 	                }

 	                Immagine[] immagini = new Immagine[album.getTotale_immagini()];
 	                for (int k = 0; k < album.getTotale_immagini(); k++) {
 	                    ImmagineDAO imageDao = new ImmagineDAO(connection);
 	                    immagini[k] = imageDao.getImageById(id_immagini[k]);
 	                }
 	                
 	                System.out.println("stampo album prima di ordinamento ID: " + album.getId_album() );
 	                for(int a = 0; a < album.getTotale_immagini(); a++) {
 	                	System.out.println(immagini[a].getId_immagine());
 	                }
 	                immagini = orderImage(immagini, flag, posizioni);
 	                album.setImmagini(immagini);
 	               System.out.println("stampo album dopo ordinamento ID: " + album.getId_album() );
	                for(int a = 0; a < album.getTotale_immagini(); a++) {
	                	System.out.println(album.getImmagini()[a].getId_immagine());
	                }
 	            }

 	            allAlbum[i] = album;
 	            i++;
 	        }

 	        // Ridimensiono l'array alla dimensione esatta
 	        if(alb) {
 	        allAlbum = trimArray(allAlbum, i);
 	        }
 	        
 	    } catch (SQLException e) {
 	        throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
 	    } finally {
 	        closeResources(result, statement);
 	        closeResources(result2, statement2);
 	    }

 	    return allAlbum;
 	}
 	
 // Metodo per raddoppiare la dimensione dell'array
 	private Album[] resizeArray(Album[] original) {
 	    int newSize = original.length * 2;
 	    Album[] newArray = new Album[newSize];
 	    System.arraycopy(original, 0, newArray, 0, original.length);
 	    return newArray;
 	}

 	// Metodo per tagliare l'array alla dimensione esatta
 	private Album[] trimArray(Album[] original, int size) {
 	    Album[] trimmedArray = new Album[size];
 	    System.arraycopy(original, 0, trimmedArray, 0, size);
 	    return trimmedArray;
 	}

 // Metodo per chiudere ResultSet e PreparedStatement
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
 	
 	public void decrementAlbumImageCount(int imageId) throws SQLException {
		String performedAction = "updating totale_immagini when deleting a image";
	    String query = "UPDATE album SET totale_immagini = totale_immagini - 1 WHERE id_album = (SELECT id_alb FROM image_album WHERE id_ima = ?)";
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
 	
	private static Immagine[] orderImage(Immagine[] immagini, boolean personalizzato, int[] posizioni) {
        if (immagini == null || immagini.length == 0) {
            return null;  // Se l'array è nullo o vuoto, esce senza fare nulla
        }
        if(!personalizzato) {
        Arrays.sort(immagini, new Comparator<Immagine>() {
            @Override
            public int compare(Immagine img1, Immagine img2) {
                // Ordinamento per data_creazione in ordine decrescente
                return img2.getData_creazione().compareTo(img1.getData_creazione());
            }
        });
        }
        else {
        	 Immagine[] finale = new Immagine[posizioni.length];
        	 for(int i = 0; i < posizioni.length; i++) {
        		 finale[posizioni[i]] = immagini[i];
        	 }
        	 return finale;
        }
        return immagini;
    }
	
	//Dato albumId e un array di int ([101, 55, 72, 80]), 
	//il DAO aggiorna il campo posizione nella tabella, 
	//in modo che la prima immagine abbia posizione=0, la seconda=1, e così via.
	public void setOrder(int albumId, int[] orderArray) throws SQLException {
		String performedAction = "updating order of images in album";
	    String query = "UPDATE image_album SET posizione = ? WHERE id_alb = ? AND id_ima = ?";
	    PreparedStatement statement = null;
	    
	    try {
	    	for(int i = 0; i < orderArray.length; i++) {
	    	statement = connection.prepareStatement(query);
	        statement.setInt(1, i);
	        statement.setInt(2, albumId);
	        statement.setInt(3, orderArray[i]);
	        statement.executeUpdate();
	    	}
	        }catch (SQLException e) {
	        	throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
	        } finally {
	     		statement.close();
            }
	    
		
	}

	public boolean checkAlbumOwner(int albumId, int id_user) throws SQLException {
		
		String performedAction = "check if id_user is owner of albumId";
	    String query = "SELECT creatore FROM album WHERE id_album = ?";
	    PreparedStatement statement = null;
	    ResultSet result = null;
	    boolean isOwner = false;
	    try {
	    	statement = connection.prepareStatement(query);
	        statement.setInt(1, albumId);
	        result = statement.executeQuery();
	        while(result.next()) {
	        	if(result.getInt("creatore") == id_user)
	        		isOwner = true;
	        }
	        }catch (SQLException e) {
	        	throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
	        } finally {
	     		closeResources(result, statement);
            }
		
		return isOwner;
	}



	public int lastPositionUsed(int albumId) throws SQLException {
		String performedAction = "check last position used in an album";
	    String query = "SELECT COALESCE(MAX(posizione), -1) as posizione FROM image_album WHERE id_alb = ?";
	    PreparedStatement statement = null;
	    ResultSet result = null;
	    int posizione = 0;
	    try {
	    	statement = connection.prepareStatement(query);
	        statement.setInt(1, albumId);
	        result = statement.executeQuery();
	        while(result.next()) {
	        	posizione = result.getInt("posizione");
	        }
	        }catch (SQLException e) {
	        	throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
	        } finally {
	     		closeResources(result, statement);
            }
	    if(posizione == -1)
	    	return 0;
	    else
		return posizione + 1;
	}

	public void refactorPosition(int imageId) throws SQLException {
		String performedAction = "refactoring position in album when deleting a photo";
	    String query = "SELECT id_alb, posizione FROM image_album WHERE id_ima = ? GROUP BY id_alb, posizione";
	    PreparedStatement statement = null;
	    ResultSet result = null;
	    String query2 = "UPDATE image_album SET posizione = posizione - 1  WHERE (id_alb = ?) AND (posizione > ?)";
	    PreparedStatement statement2 = null;
	    int[] posizione = new int[1];
	    int[] id_alb = new int[1];
	    int i = 0;
	    System.out.println("----sono entrato nel refactor-----");
	    try {
	    	statement = connection.prepareStatement(query);
	        statement.setInt(1, imageId);
	        result = statement.executeQuery();
	        System.out.println("----fatta prima query-----");
	        while(result.next()) {
	        	if (i == posizione.length) {
 	                posizione = resizeArrayInt(posizione);
 	            }
	        	if (i == id_alb.length) {
 	                id_alb = resizeArrayInt(id_alb);
 	            }
	        	posizione[i] = result.getInt("posizione");
	        	id_alb[i] = result.getInt("id_alb");
	        	i++;
	        }
	        
	 	       posizione = trimArrayInt(posizione, i);
	 	       id_alb = trimArrayInt(id_alb, i);
	 	      System.out.println("----sto per fare seconda-----");
	 	      try {
	 	    	 System.out.println("----vorrei fare seconda-----" + id_alb.length);
	 	    	 for(int j = 0; j < id_alb.length; j++) {  
	 	    		
		 		       System.out.println(id_alb[j]);
		 		      System.out.println(posizione[j]);
	 		    	statement2 = connection.prepareStatement(query2);
	 		    	System.out.println(statement2);
	 		        statement2.setInt(1, id_alb[j]);
	 		        
	 		        statement2.setInt(2, posizione[j]);
	 		       System.out.println(statement2);
	 		        statement2.executeUpdate();
	 		        
	 	    	 }
	 	     }catch (SQLException e) {
		        	throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
		        } finally {
		     		statement2.close();
	            }
	        }catch (SQLException e) {
	        	throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");
	        } finally {
	     		closeResources(result, statement);
            }
		
	}
	 // Metodo per raddoppiare la dimensione dell'array
 	private int[] resizeArrayInt(int[] original) {
 	    int newSize = original.length * 2;
 	    int[] newArray = new int[newSize];
 	    System.arraycopy(original, 0, newArray, 0, original.length);
 	    return newArray;
 	}

 	// Metodo per tagliare l'array alla dimensione esatta
 	private int[] trimArrayInt(int[] original, int size) {
 	    int[] trimmedArray = new int[size];
 	    System.arraycopy(original, 0, trimmedArray, 0, size);
 	    return trimmedArray;
 	}
 	
}