package com.romkalkylator.model;

/**
 * Beräknad ersättning för en enskild deltagare under en månad.
 */
public class DeltagareErsattning {

    private String fornamn;
    private String efternamn;
    private String personnummer;
    private String niva;
    private int dagsbelopp;
    private int ersattaDagar;
    private int belopp;
    private String kommentar;

    public DeltagareErsattning() {
    }

    public DeltagareErsattning(String fornamn, String efternamn, String personnummer,
                               String niva, int dagsbelopp, int ersattaDagar, int belopp,
                               String kommentar) {
        this.fornamn = fornamn;
        this.efternamn = efternamn;
        this.personnummer = personnummer;
        this.niva = niva;
        this.dagsbelopp = dagsbelopp;
        this.ersattaDagar = ersattaDagar;
        this.belopp = belopp;
        this.kommentar = kommentar;
    }

    public String getFornamn() {
        return fornamn;
    }

    public void setFornamn(String fornamn) {
        this.fornamn = fornamn;
    }

    public String getEfternamn() {
        return efternamn;
    }

    public void setEfternamn(String efternamn) {
        this.efternamn = efternamn;
    }

    public String getPersonnummer() {
        return personnummer;
    }

    public void setPersonnummer(String personnummer) {
        this.personnummer = personnummer;
    }

    public String getNiva() {
        return niva;
    }

    public void setNiva(String niva) {
        this.niva = niva;
    }

    public int getDagsbelopp() {
        return dagsbelopp;
    }

    public void setDagsbelopp(int dagsbelopp) {
        this.dagsbelopp = dagsbelopp;
    }

    public int getErsattaDagar() {
        return ersattaDagar;
    }

    public void setErsattaDagar(int ersattaDagar) {
        this.ersattaDagar = ersattaDagar;
    }

    public int getBelopp() {
        return belopp;
    }

    public void setBelopp(int belopp) {
        this.belopp = belopp;
    }

    public String getKommentar() {
        return kommentar;
    }

    public void setKommentar(String kommentar) {
        this.kommentar = kommentar;
    }
}
