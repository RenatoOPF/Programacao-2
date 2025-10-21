package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Empregado {
    private String id;
    private String nome;
    private String endereco;
    private String tipo;
    private double salario;

    // ---------- Atributos opcionais ----------
    private double comissao;       // usado apenas para comissionados
    private boolean sindicalizado; // true apenas se o empregado for sindicalizado
    private String idSindicato;
    private double taxaSindical;

    // --- NOVOS ATRIBUTOS ---
    private String dataUltimoPagamento; // Data do último pagamento (formato d/M/yyyy)
    private double taxaSindicalDiaria;  // Valor diário da taxa sindical
    private double debitoSindicalAcumulado;
    private LocalDate dataAdmissao;

    // ---------- Pagamento ----------
    private String metodoPagamento; // "emMaos", "correios", "banco"
    private String banco;
    private String agencia;
    private String contaCorrente;

    private List<RegistroDeHoras> registrosDeHoras = new ArrayList<>();
    private List<Venda> vendas = new ArrayList<>();
    private List<TaxaServico> taxasServico = new ArrayList<>();

    public Empregado(String id, String nome, String endereco, String tipo, double salario) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
        this.salario = salario;

        this.comissao = 0.0;
        this.sindicalizado = false;
        this.idSindicato = null;
        this.taxaSindical = 0.0;
        this.debitoSindicalAcumulado = 0.0;

        this.metodoPagamento = "emMaos"; // default
        this.banco = null;
        this.agencia = null;
        this.contaCorrente = null;
    }

    // ---------- Getters e Setters básicos ----------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }

    public double getComissao() { return comissao; }
    public void setComissao(double comissao) {
        if ("comissionado".equalsIgnoreCase(this.tipo)) {
            this.comissao = comissao;
        } else {
            this.comissao = 0.0; // zera se não for comissionado
        }
    }

    // ---------- Sindicalizado ----------
    public boolean getSindicalizado() {
        // só é considerado sindicalizado se o flag estiver ativo e tiver idSindicato
        return sindicalizado && idSindicato != null;
    }

    public void setSindicalizado(boolean sindicalizado) {
        this.sindicalizado = sindicalizado;
        if (!sindicalizado) {
            this.idSindicato = null;
            this.taxaSindical = 0.0;
        }
    }

    public String getIdSindicato() { return idSindicato; }
    public void setIdSindicato(String idSindicato) {
        if (sindicalizado) {
            this.idSindicato = idSindicato;
        } else {
            this.idSindicato = null;
        }
    }

    public double getTaxaSindical() { return taxaSindical; }
    public void setTaxaSindical(double taxaSindical) {
        if (sindicalizado) {
            this.taxaSindical = taxaSindical;
        } else {
            this.taxaSindical = 0.0;
        }
    }

    // ---------- Registro de horas ----------
    public List<RegistroDeHoras> getRegistrosDeHoras() { return registrosDeHoras; }
    public void addRegistroDeHoras(RegistroDeHoras registro) { registrosDeHoras.add(registro); }

    // ---------- Vendas ----------
    public List<Venda> getVendas() { return vendas; }
    public void addVenda(Venda venda) { vendas.add(venda); }

    // ---------- Taxas de serviço ----------
    public List<TaxaServico> getTaxasServico() { return taxasServico; }
    public void addTaxaServico(TaxaServico taxa) {
        if (getSindicalizado()) {
            taxasServico.add(taxa);
        }
    }

    // ---------- Getters de Pagamento ----------
    public String getMetodoPagamento() { return metodoPagamento; }
    public String getMetodoPagamentoFormatado() {
        if (metodoPagamento == null) return "";

        switch (metodoPagamento) {
            case "correios":
                return "Correios, " + endereco;
            case "em maos":
            case "mãos":
            case "maos":
            case "emMaos":
            case "Em Maos":
            case "em Maos":
                return "Em maos";

            case "banco":
            case "contaCorrente":
            case "deposito":
            case "depósito":
            case "banco do brasil":
                // Garante que agência e contaCorrente não sejam nulos
                String agenciaStr = (agencia != null) ? agencia : "----";
                String contaCorrenteStr = (contaCorrente != null) ? contaCorrente : "-----";
                return String.format("Banco do Brasil, Ag. %s CC %s", agenciaStr, contaCorrenteStr);

            default:
                return metodoPagamento;
        }
    }
    public String getBanco() { return banco; }
    public String getAgencia() { return agencia; }
    public String getContaCorrente() { return contaCorrente; }

    // ---------- Setters de Pagamento ----------
    public void setMetodoPagamento(String metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public void setBanco(String banco) { this.banco = banco; }
    public void setAgencia(String agencia) { this.agencia = agencia; }
    public void setContaCorrente(String contaCorrente) { this.contaCorrente = contaCorrente; }

    // --- Novos Getters e Setters ---
    public String getDataUltimoPagamento() {
        return dataUltimoPagamento;
    }

    /** Define a data do último pagamento (String) */
    public void setDataUltimoPagamento(String data) {
        if (data != null) {
            this.dataUltimoPagamento = data;
        }
    }

    public double getTaxaSindicalDiaria() {
        return taxaSindicalDiaria;
    }

    public void setTaxaSindicalDiaria(double taxaSindicalDiaria) {
        this.taxaSindicalDiaria = taxaSindicalDiaria;
    }

    public double getDebitoSindicalAcumulado() {
        return debitoSindicalAcumulado;
    }

    public void setDebitoSindicalAcumulado(double debito) {
        this.debitoSindicalAcumulado = debito;
    }

    public void addDebitoSindicalAcumulado(double debito) { this.debitoSindicalAcumulado = debitoSindicalAcumulado + debito; }

    public LocalDate getDataAdmissao() { return dataAdmissao; }

    public void setDataAdmissao(LocalDate dataAdmissao) { this.dataAdmissao = dataAdmissao; }

}