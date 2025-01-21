package it.polimi.tiw.beans;

import java.sql.Timestamp;
import java.util.Arrays;

public class ImmagineData {
    private Timestamp data_creazione;
    private int id_immagine;
    private String titolo;
    private String descrizione;
    private String percorso;
    private int id_user_proprietario;

    // Se l'immagine ha dei commenti, li convertiamo in un array di CommentoData
    private CommentoData[] commenti;

    public ImmagineData(Immagine immagine) {
        this.data_creazione = immagine.getData_creazione();
        this.id_immagine = immagine.getId_immagine();
        this.titolo = immagine.getTitolo();
        this.descrizione = immagine.getDescrizione();
        this.percorso = immagine.getPercorso();
        this.id_user_proprietario = immagine.getId_user_proprietario();

        // Se ci sono commenti, li convertiamo
        if (immagine.getCommenti() != null) {
            this.commenti = Arrays.stream(immagine.getCommenti())
                                  .map(CommentoData::new)
                                  .toArray(CommentoData[]::new);
        } else {
            this.commenti = new CommentoData[0];
        }
    }

    // GETTER
    public Timestamp getData_creazione() {
        return data_creazione;
    }

    public int getId_immagine() {
        return id_immagine;
    }

    public String getTitolo() {
        return titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getPercorso() {
        return percorso;
    }

    public int getId_user_proprietario() {
        return id_user_proprietario;
    }

    public CommentoData[] getCommenti() {
        return commenti;
    }
}
