package br.ufal.ic.p2.wepayu.service;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.TaxaServico;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.*;

public class TaxaServicoService {
    private Map<String, Empregado> empregadosMap;

    public TaxaServicoService(Map<String, Empregado> empregadosMap) {
        this.empregadosMap = empregadosMap;
    }

    public void carregarTaxas() {
        File arquivo = new File("taxas.csv");
        if (!arquivo.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/uuuu");

            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length != 3) continue; // empId;data;valor

                String idSindicato = partes[0];
                LocalDate data = LocalDate.parse(partes[1], formatter);
                double valor = Double.parseDouble(partes[2]);

                Empregado e = empregadosMap.values().stream()
                        .filter(emp -> emp.getSindicalizado() && idSindicato.equals(emp.getIdSindicato()))
                        .findFirst()
                        .orElse(null);

                if (e != null) {
                    e.addTaxaServico(new TaxaServico(data, valor));
                }
            }
        } catch (IOException | NumberFormatException | DateTimeParseException e) {
            System.err.println("Erro ao carregar taxas: " + e.getMessage());
        }
    }

    public void lancarTaxaServico(String membro, String dataStr, double valor) throws Exception {
        if (membro == null || membro.trim().isEmpty())
            throw new Exception("Identificacao do membro nao pode ser nula.");

        Empregado e = empregadosMap.values().stream()
                .filter(emp -> membro.equals(emp.getIdSindicato()))
                .findFirst().orElseThrow(() -> new Exception("Membro nao existe."));

        if (!e.getSindicalizado())
            throw new Exception("Empregado nao eh sindicalizado.");

        LocalDate data;
        try {
            data = LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("d/M/uuuu"));
        } catch (DateTimeParseException ex) {
            throw new DataInvalidaException("Data invalida.");
        }

        if (valor <= 0) throw new Exception("Valor deve ser positivo.");

        e.addTaxaServico(new TaxaServico(data, valor));
        salvar();
    }

    public String getTotalTaxas(String empId, String dataInicialStr, String dataFinalStr) throws Exception {
        Empregado e = empregadosMap.get(empId);
        if (e == null)
            throw new EmpregadoNaoExisteException("Empregado nao existe.");
        if (!e.getSindicalizado())
            throw new Exception("Empregado nao eh sindicalizado.");

        LocalDate inicial = parseData(dataInicialStr, "inicial");
        LocalDate fim = parseData(dataFinalStr, "final");

        if (inicial.isAfter(fim)) {
            throw new IllegalArgumentException("Data inicial nao pode ser posterior aa data final.");
        }

        double total = 0;
        List<TaxaServico> taxas = e.getTaxasServico();
        for (TaxaServico t : taxas) {
            if (!t.getData().isBefore(inicial) && t.getData().isBefore(fim)) {
                total += t.getValor();
            }
        }

        return String.format("%.2f", total).replace('.', ',');
    }

    public void limpar() {
        empregadosMap.values().forEach(emp -> emp.getTaxasServico().clear());
    }

    public void zerar() {
        empregadosMap.values().forEach(emp -> emp.getTaxasServico().clear());
        try (PrintWriter pw = new PrintWriter(new FileWriter("taxas.csv"))) {}
        catch (IOException ignored) {}
    }

    public void salvar() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("taxas.csv"))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/uuuu");
            for (Empregado e : empregadosMap.values()) {
                for (TaxaServico t : e.getTaxasServico()) {
                    // Salva sempre pelo ID do empregado
                    pw.printf("%s;%s;%.2f%n", e.getIdSindicato() != null ? e.getIdSindicato() : e.getNome(), t.getData().format(formatter), t.getValor());
                }
            }
        } catch (IOException ex) {
            System.err.println("Erro ao salvar taxas: " + ex.getMessage());
        }
    }

    private LocalDate parseData(String dataStr, String tipo) throws DataInvalidaException {
        try {
            return LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT));
        } catch (DateTimeParseException ex) {
            throw new DataInvalidaException("Data " + tipo + " invalida.");
        }
    }

    // ---------- Clone e Restaurar ----------
    public Map<String, List<TaxaServico>> cloneTaxas() {
        Map<String, List<TaxaServico>> clone = new HashMap<>();
        for (Map.Entry<String, Empregado> entry : empregadosMap.entrySet()) {
            List<TaxaServico> listaClone = new ArrayList<>();
            for (TaxaServico t : entry.getValue().getTaxasServico()) {
                listaClone.add(t.copiar()); // implementar copiar() em TaxaServico
            }
            clone.put(entry.getKey(), listaClone);
        }
        return clone;
    }

    public void restaurarTaxas(Map<String, List<TaxaServico>> clone) {
        for (Map.Entry<String, Empregado> entry : empregadosMap.entrySet()) {
            entry.getValue().getTaxasServico().clear();
            if (clone.containsKey(entry.getKey())) {
                entry.getValue().getTaxasServico().addAll(clone.get(entry.getKey()));
            }
        }
        salvar();
    }
}