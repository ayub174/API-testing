package com.romkalkylator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Sammanställt resultat av en månadsberäkning: en rad per deltagare samt
 * totalsummor för hela filen.
 */
public class RomResultat {

    private String manad;
    private int antalDeltagare;
    private int totalErsattaDagar;
    private int totalBelopp;
    private List<DeltagareErsattning> deltagare = new ArrayList<>();
    private List<String> varningar = new ArrayList<>();

    public String getManad() {
        return manad;
    }

    public void setManad(String manad) {
        this.manad = manad;
    }

    public int getAntalDeltagare() {
        return antalDeltagare;
    }

    public void setAntalDeltagare(int antalDeltagare) {
        this.antalDeltagare = antalDeltagare;
    }

    public int getTotalErsattaDagar() {
        return totalErsattaDagar;
    }

    public void setTotalErsattaDagar(int totalErsattaDagar) {
        this.totalErsattaDagar = totalErsattaDagar;
    }

    public int getTotalBelopp() {
        return totalBelopp;
    }

    public void setTotalBelopp(int totalBelopp) {
        this.totalBelopp = totalBelopp;
    }

    public List<DeltagareErsattning> getDeltagare() {
        return deltagare;
    }

    public void setDeltagare(List<DeltagareErsattning> deltagare) {
        this.deltagare = deltagare;
    }

    public List<String> getVarningar() {
        return varningar;
    }

    public void setVarningar(List<String> varningar) {
        this.varningar = varningar;
    }
}
