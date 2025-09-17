package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;

public class RegistroDeHoras {
    private LocalDate data;
    private double horasNormais;
    private double horasExtras;

    public RegistroDeHoras(LocalDate data, double horasNormais, double horasExtras) {
        this.data = data;
        this.horasNormais = horasNormais;
        this.horasExtras = horasExtras;
    }

    public LocalDate getData() { return data; }
    public double getHorasNormais() { return horasNormais; }
    public double getHorasExtras() { return horasExtras; }

    public void SetHorasNormais(double horasNormais) {
        this.horasNormais += horasNormais;
    }
    public void SetHorasExtras(double horasExtras) {
        this.horasExtras += horasExtras;
    }
}
