package br.ufal.ic.p2.wepayu.service;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.Venda;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class VendasService {

    private final String ARQUIVO = "vendas.csv";
    private Map<String, Empregado> empregadosMap;

    public VendasService(Map<String, Empregado> empregadosMap) {
        this.empregadosMap = empregadosMap;
    }

    public void carregarVendas() {
        File arquivo = new File(ARQUIVO);
        if (!arquivo.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length != 3) continue; // id;data;valor

                String empId = partes[0];
                LocalDate data = LocalDate.parse(partes[1], formatter);
                double valor = Double.parseDouble(partes[2]);

                Empregado e = empregadosMap.get(empId);
                if (e != null) {
                    e.addVenda(new Venda(data, valor));
                }
            }

        } catch (IOException e) {
            System.err.println("Erro ao carregar vendas: " + e.getMessage());
        } catch (NumberFormatException | DateTimeParseException e) {
            System.err.println("Erro ao processar linha de venda: " + e.getMessage());
        }
    }

    public void lancarVenda(String empId, String dataStr, String valorStr) throws EmpregadoNaoExisteException {
        if (empId == null || empId.trim().isEmpty()) {
            throw new IdentificacaoDoEmpregadoNulaException("Identificacao do empregado nao pode ser nula.");
        }

        Empregado e = empregadosMap.get(empId);
        if (e == null) {
            throw new EmpregadoNaoExisteException("Empregado nao existe.");
        }

        if (!"comissionado".equals(e.getTipo())) {
            throw new NaoEhComissionadoException("Empregado nao eh comissionado.");
        }

        LocalDate data;
        try {
            data = LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("d/M/yyyy"));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Data invalida.");
        }

        double valor;
        try {
            valor = Double.parseDouble(valorStr.replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Valor invalido.");
        }

        if (valor <= 0) throw new IllegalArgumentException("Valor deve ser positivo.");

        e.addVenda(new Venda(data, valor));
        salvar();
    }

    public String getVendas(String empId, String dataInicialStr, String dataFinalStr) throws EmpregadoNaoExisteException {
        if (empId == null || empId.trim().isEmpty()) {
            throw new IdentificacaoDoEmpregadoNulaException("Identificacao do empregado nao pode ser nula.");
        }

        Empregado e = empregadosMap.get(empId);
        if (e == null) {
            throw new EmpregadoNaoExisteException("Empregado nao existe.");
        }

        if (!"comissionado".equals(e.getTipo())) {
            throw new NaoEhComissionadoException("Empregado nao eh comissionado.");
        }

        LocalDate inicial = parseData(dataInicialStr, "inicial");
        LocalDate fim = parseData(dataFinalStr, "final");

        if (inicial.isAfter(fim)) {
            throw new IllegalArgumentException("Data inicial nao pode ser posterior aa data final.");
        }

        double total = 0;
        List<Venda> vendas = e.getVendas();
        for (Venda v : vendas) {
            if (!v.getData().isBefore(inicial) && v.getData().isBefore(fim)) {
                total += v.getValor();
            }
        }

        return String.format("%.2f", total).replace('.', ',');
    }

    public void zerar() {
        for (Empregado e : empregadosMap.values()) {
            e.getVendas().clear();
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARQUIVO))) {
            // limpa arquivo
        } catch (IOException ignored) {}
    }

    public void salvar() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARQUIVO))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
            for (Map.Entry<String, Empregado> entry : empregadosMap.entrySet()) {
                Empregado e = entry.getValue();
                for (Venda v : e.getVendas()) {
                    pw.printf("%s;%s;%.2f%n", entry.getKey(), v.getData().format(formatter), v.getValor());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar vendas: " + e.getMessage());
        }
    }

    private LocalDate parseData(String dataStr, String tipo) throws DataInvalidaException {
        try {
            return LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT));
        } catch (DateTimeParseException ex) {
            throw new DataInvalidaException("Data " + tipo + " invalida.");
        }
    }
}