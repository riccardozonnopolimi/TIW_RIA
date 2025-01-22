package it.polimi.tiw.beans;

import java.sql.Timestamp;

public class CommentoData {
    private int id_commento;
    private String testo;
    private Timestamp data_creazione;
    private int id_user;
    private int id_immagine;
    private String username;

    public CommentoData(Commento commento) {
        this.id_commento = commento.getId_commento();
        this.testo = commento.getTesto();
        this.data_creazione = commento.getData_creazione();
        this.id_user = commento.getId_user();
        this.id_immagine = commento.getId_immagine();
        this.username = commento.getUsername();
    }

    public int getId_commento() {
        return id_commento;
    }

    public String getTesto() {
        return testo;
    }

    public Timestamp getData_creazione() {
        return data_creazione;
    }

    public int getId_user() {
        return id_user;
    }

    public int getId_immagine() {
        return id_immagine;
    }

    public String getUsername() {
        return username;
    }
}

