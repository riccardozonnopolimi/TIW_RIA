package it.polimi.tiw.beans;

import java.sql.Timestamp;
import java.util.Arrays;

public class AlbumData {
    private int id_album;
    private String titolo;
    private Timestamp data_creazione;
    private int id_user_proprietario;
    private int totale_immagini;
    private int[] id_immagini;
    private String usernameCreatore;

    // Se vuoi includere direttamente le immagini di questo album,
    // puoi farlo con un array di ImmagineData
    private ImmagineData[] immagini;

    public AlbumData(Album album) {
        this.id_album = album.getId_album();
        this.titolo = album.getTitolo();
        this.data_creazione = album.getData_creazione();
        this.id_user_proprietario = album.getId_user_proprietario();
        this.totale_immagini = album.getTotale_immagini();
        this.id_immagini = album.getId_immagini();
        this.usernameCreatore = album.getUsernameCreatore();

        // Se l'album contiene già un array di Immagine, convertiamolo
        if (album.getImmagini() != null) {
            this.immagini = Arrays.stream(album.getImmagini())
                                  .map(ImmagineData::new)
                                  .toArray(ImmagineData[]::new);
        } else {
            this.immagini = new ImmagineData[0];
        }
    }

    // GETTER
    public int getId_album() {
        return id_album;
    }

    public String getTitolo() {
        return titolo;
    }

    public Timestamp getData_creazione() {
        return data_creazione;
    }

    public int getId_user_proprietario() {
        return id_user_proprietario;
    }

    public int getTotale_immagini() {
        return totale_immagini;
    }

    public int[] getId_immagini() {
        return id_immagini;
    }

    public String getUsernameCreatore() {
        return usernameCreatore;
    }

    public ImmagineData[] getImmagini() {
        return immagini;
    }
}
