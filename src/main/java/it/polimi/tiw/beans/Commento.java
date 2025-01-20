package it.polimi.tiw.beans;

import java.sql.Timestamp;

public class Commento {
	private int id_commento;
    private String testo;
    private Timestamp data_creazione;
    private int id_user;
    private int id_immagine;
    private String username;
    
    public Commento(int id_commento, String testo, int id_user, int id_immagine,  Timestamp data_creazione, String username) {
    		this.id_commento = id_commento;
	        this.data_creazione = data_creazione;
	        this.testo = testo;
	        this.id_user = id_user;
	        this.id_immagine = id_immagine;
	        this.username = username;
	        
	    }
    public String getUsername() {
        return this.username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    
    public Timestamp getData_creazione() {
        return data_creazione;
    }

    public void setData_creazione(Timestamp data_creazione) {
        this.data_creazione = data_creazione;
    }

    public int getId_commento() {
        return id_commento;
    }

    public void setId_commento(int id_commento) {
        this.id_commento = id_commento;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public int getId_immagine() {
        return id_immagine;
    }

    public void setId_immagine(int id_immagine) {
        this.id_immagine = id_immagine;
    }
}