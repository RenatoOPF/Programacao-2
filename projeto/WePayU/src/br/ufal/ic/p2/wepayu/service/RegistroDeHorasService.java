package br.ufal.ic.p2.wepayu.service;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.RegistroDeHoras;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Map;

public class RegistroDeHorasService {
    private Map<String, Empregado> empregadosMap;

    public RegistroDeHorasService(Map<String, Empregado> empregadosMap) {
        this.empregadosMap = empregadosMap;
    }

    public void lancarCartao(String empId, String dataStr, String horasStr) {
        Empregado e = validarEmpregado(empId);

        if (!"horista".equalsIgnoreCase(e.getTipo())) {
            throw new NaoEhHoristaException("Empregado nao eh horista.");
        }

        LocalDate data;
        try {
            data = LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("d/M/uuuu"));
        } catch (DateTimeParseException ex) {
            throw new DataInvalidaException("Data invalida.");
        }

        if (e.getDataAdmissao() == null) {
            e.setDataAdmissao(data);
        }

        double horas;
        try {
            horas = Double.parseDouble(horasStr.replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Horas devem ser numericas.");
        }

        if (horas <= 0) {
            throw new IllegalArgumentException("Horas devem ser positivas.");
        }

        double normais = Math.min(horas, 8);
        double extras = Math.max(0, horas - 8);

        RegistroDeHoras registroExistente = null;
        for (RegistroDeHoras r : e.getRegistrosDeHoras()) {
            if (r.getData().equals(data)) {
                registroExistente = r;
                break;
            }
        }

        if (registroExistente != null) {
            registroExistente.SetHorasNormais(normais);
            registroExistente.SetHorasExtras(extras);
        } else {
            e.addRegistroDeHoras(new RegistroDeHoras(data, normais, extras));
        }

        salvar();
    }

    public LocalDate getDataAdmissao(Empregado e) {
        if ("horista".equalsIgnoreCase(e.getTipo())) {
            return e.getDataAdmissao(); // primeira vez que lançou cartão
        } else {
            return LocalDate.of(2005, 1, 1); // assalariado ou comissionado
        }
    }

    public String getHorasNormais(String empId, String dataInicial, String dataFinal) {
        Empregado e = validarEmpregado(empId);

        if (!"horista".equalsIgnoreCase(e.getTipo())) {
            throw new NaoEhHoristaException("Empregado nao eh horista.");
        }

        if (dataInicial == null || dataFinal == null || e.getRegistrosDeHoras().isEmpty()) {
            return "0";
        }

        LocalDate inicial = parseData(dataInicial, "inicial");
        LocalDate fim = parseData(dataFinal, "final");

        if (inicial.isAfter(fim)) {
            throw new IllegalArgumentException("Data inicial nao pode ser posterior aa data final.");
        }

        double total = 0;
        for (RegistroDeHoras r : e.getRegistrosDeHoras()) {
            if (!r.getData().isBefore(inicial) && r.getData().isBefore(fim)) {
                total += r.getHorasNormais();
            }
        }

        return formatHoras(total);
    }

    public String getHorasExtras(String empId, String dataInicial, String dataFinal) {
        Empregado e = validarEmpregado(empId);

        if (!"horista".equalsIgnoreCase(e.getTipo())) {
            throw new NaoEhHoristaException("Empregado nao eh horista.");
        }

        if (dataInicial == null || dataFinal == null || e.getRegistrosDeHoras().isEmpty()) {
            return "0";
        }

        LocalDate inicial = parseData(dataInicial, "inicial");
        LocalDate fim = parseData(dataFinal, "final");

        if (inicial.isAfter(fim)) {
            throw new IllegalArgumentException("Data inicial nao pode ser posterior aa data final.");
        }

        double total = 0;
        for (RegistroDeHoras r : e.getRegistrosDeHoras()) {
            if (!r.getData().isBefore(inicial) && r.getData().isBefore(fim)) {
                total += r.getHorasExtras();
            }
        }

        return formatHoras(total);
    }

    public void carregarRegistros() {
        File arquivo = new File("registros.csv");
        if (!arquivo.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/uuuu");

            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length != 4) continue;

                String empId = partes[0];
                LocalDate data = LocalDate.parse(partes[1], formatter);
                double normais = Double.parseDouble(partes[2]);
                double extras = Double.parseDouble(partes[3]);

                Empregado e = empregadosMap.get(empId);
                if (e != null) {
                    e.addRegistroDeHoras(new RegistroDeHoras(data, normais, extras));
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar registros: " + e.getMessage());
        }
    }

    public void zerar() {
        for (Empregado e : empregadosMap.values()) {
            e.getRegistrosDeHoras().clear();
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter("registros.csv"))) {
            // limpa o arquivo
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void salvar() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("registros.csv"))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/uuuu");

            for (Map.Entry<String, Empregado> entry : empregadosMap.entrySet()) {
                String empId = entry.getKey();
                Empregado e = entry.getValue();
                for (RegistroDeHoras r : e.getRegistrosDeHoras()) {
                    pw.println(empId + ";" + r.getData().format(formatter) + ";" + r.getHorasNormais() + ";" + r.getHorasExtras());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar registros: " + e.getMessage());
        }
    }

    private Empregado validarEmpregado(String empId) {
        if (empId == null || empId.trim().isEmpty()) {
            throw new IdentificacaoDoEmpregadoNulaException("Identificacao do empregado nao pode ser nula.");
        }

        Empregado e = empregadosMap.get(empId);
        if (e == null) {
            throw new EmpregadoNaoExisteException("Empregado nao existe.");
        }

        return e;
    }

    private LocalDate parseData(String dataStr, String tipo) throws DataInvalidaException {
        try {
            return LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT));
        } catch (DateTimeParseException ex) {
            throw new DataInvalidaException("Data " + tipo + " invalida.");
        }
    }

    private String formatHoras(double total) {
        if (total == (int) total) return String.valueOf((int) total);
        return String.format("%.1f", total).replace('.', ',');
    }
}
