package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Empregado {
    private String nome;
    private String endereco;
    private String tipo;
    private double salario;

    // atributos opcionais
    private Double comissao;     // usado só para comissionado
    private boolean sindicalizado;     // usado só para comissionado
    private List<RegistroDeHoras> registrosDeHoras = new ArrayList<>();
    private List<Venda> vendas = new ArrayList<>();

    public Empregado(String nome, String endereco, String tipo, double salario) throws EmpregadoNaoExisteException {
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
        this.salario = salario;
        this.comissao = 0.0;
        this.sindicalizado = false;
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

    public Boolean getSindicalizado() { return sindicalizado; } // Implementar depois

    public List<RegistroDeHoras> getRegistrosDeHoras() { return registrosDeHoras; }

    // ---------- Registros de horas ----------
    public void setComissao(Double comissao) { this.comissao = comissao; }
    public void setRegistroDeHoras(RegistroDeHoras registro) { registrosDeHoras.add(registro); }

    // ---------- Vendas ----------
    public void addVenda(Venda venda) { vendas.add(venda); }
    public List<Venda> getVendas() { return vendas; }
}

