package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;

public class Empregado {
    private String nome;
    private String endereco;
    private String tipo;
    private double salario;

    // atributos opcionais
    private Double comissao;     // usado só para comissionado

    public Empregado(String nome, String endereco, String tipo, double salario) throws EmpregadoNaoExisteException {
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
        this.salario = salario;
    }

    public String getNome() {
        return nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getTipo() {
        return tipo;
    }

    public double getSalario() {
        return salario;
    }

    public Double getComissao() { return comissao; }

    public String getSindicalizado() { return "false"; } // Implementar depois

    // Setters opcionais
    public void setComissao(Double comissao) { this.comissao = comissao; }
}
