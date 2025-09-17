package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.RegistroDeHoras;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Facade {
    private static Map<String, Empregado> empregadosMap = new HashMap<>();
    private static int proximoId = 1; // para gerar IDs automáticos
    private final String ARQUIVO = "empregados.csv"; // arquivo de persistência

    public Facade() throws EmpregadoNaoExisteException{
        carregarEmpregados();
        carregarRegistrosCSV();
    }

    public void zerarSistema() {
        empregadosMap.clear();
        proximoId = 1;
        salvarEmpregados();

        try (PrintWriter pw = new PrintWriter(new FileWriter("registros.csv"))) {
            // limpa o arquivo
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void encerrarSistema() {
        salvarEmpregados();
        salvarRegistros();
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salarioString) throws EmpregadoNaoExisteException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new NomeNuloException("Nome nao pode ser nulo.");
        }

        if (endereco == null || endereco.trim().isEmpty()) {
            throw new EnderecoNuloException("Endereco nao pode ser nulo.");
        }


        if (!tipo.equalsIgnoreCase("horista") && !tipo.equalsIgnoreCase("assalariado")) {
            if (tipo.equalsIgnoreCase("comissionado") ) {
                throw new TipoNaoAplicavelException("Tipo nao aplicavel.");
            } else {
                throw new TipoInvalidoException("Tipo invalido.");
            }
        }

        if (salarioString == null || salarioString.trim().isEmpty()) {
            throw new SalarioNuloException("Salario nao pode ser nulo.");
        }

        double salario;
        try {
            salario = Double.parseDouble(salarioString.replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new SalarioNaoNumericoException("Salario deve ser numerico.");
        }

        if (salario < 0) throw new SalarioNegativoException("Salario deve ser nao-negativo.");

        String id = "id" + proximoId++;
        Empregado e = new Empregado(nome, endereco, tipo.toLowerCase(), salario);
        empregadosMap.put(id, e);
        salvarEmpregados();
        return id;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salarioString, String comissaoString) throws EmpregadoNaoExisteException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new NomeNuloException("Nome nao pode ser nulo.");
        }

        if (endereco == null || endereco.trim().isEmpty()) {
            throw new EnderecoNuloException("Endereco nao pode ser nulo.");
        }

        if (!tipo.equalsIgnoreCase("comissionado")) {
            if (tipo.equalsIgnoreCase("assalariado") || tipo.equalsIgnoreCase("horista")) {
                throw new TipoNaoAplicavelException("Tipo nao aplicavel.");
            } else {
                throw new TipoInvalidoException("Tipo invalido.");
            }
        }

        if (salarioString == null || salarioString.trim().isEmpty()) {
            throw new SalarioNuloException("Salario nao pode ser nulo.");
        }

        double salario;
        try {
            salario = Double.parseDouble(salarioString.replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new SalarioNaoNumericoException("Salario deve ser numerico.");
        }

        if (salario < 0) throw new SalarioNegativoException("Salario deve ser nao-negativo.");

        // valida comissao
        if (comissaoString == null || comissaoString.trim().isEmpty()) {
            throw new ComissaoNulaException("Comissao nao pode ser nula.");
        }

        double comissao;
        try {
            comissao = Double.parseDouble(comissaoString.replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new ComissaoNaoNumericaException("Comissao deve ser numerica.");
        }
        if (comissao < 0) throw new ComissaoNegativaException("Comissao deve ser nao-negativa.");

        String id = "id" + proximoId++;
        Empregado e = new Empregado(nome, endereco, tipo.toLowerCase(), salario);
        e.setComissao(comissao);
        empregadosMap.put(id, e);
        salvarEmpregados();
        return id;
    }

    // Método auxiliar
    private Empregado getEmpregadoPorId(String emp) {
        // Procura o empregado na sua lista/mapa de empregados
        return empregadosMap.get(emp); // Exemplo usando um HashMap<String, Empregado>
    }

    public String getAtributoEmpregado(String emp, String atributo) throws EmpregadoNaoExisteException {
        if (emp == null || emp.trim().isEmpty()) {
            throw new IdentificacaoDoEmpregadoNulaException("Identificacao do empregado nao pode ser nula.");
        }

        Empregado e = getEmpregadoPorId(emp);
        if (e == null) {
            throw new EmpregadoNaoExisteException("Empregado nao existe.");
        }

        switch (atributo) {
            case "nome": return e.getNome();
            case "endereco": return e.getEndereco();
            case "tipo": return e.getTipo();
            case "salario": return String.format("%.2f", (double) e.getSalario()).replace('.', ',');
            case "comissao": return String.format("%.2f", (double) e.getComissao()).replace('.', ',');
            case "sindicalizado": return String.valueOf(e.getSindicalizado()); // implementar depois
            default: throw new AtributoNaoExisteException("Atributo nao existe.");
        }
    }

    public String getEmpregadoPorNome(String nome, int indice) throws EmpregadoNaoExisteException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new EmpregadoNaoExisteException("Nao ha empregado com esse nome.");
        }

        // procura todos com esse nome
        List<String> idsEncontrados = new ArrayList<>();
        for (Map.Entry<String, Empregado> entry : empregadosMap.entrySet()) {
            if (entry.getValue().getNome().equals(nome)) {
                idsEncontrados.add(entry.getKey());
            }
        }

        if (idsEncontrados.isEmpty() || indice < 1 || indice > idsEncontrados.size()) {
            throw new EmpregadoNaoExisteException("Nao ha empregado com esse nome.");
        }

        // retorna o ID correspondente
        return idsEncontrados.get(indice - 1);
    }

    public void removerEmpregado(String emp) throws EmpregadoNaoExisteException {
        if (emp == null || emp.trim().isEmpty()) {
            throw new IdentificacaoDoEmpregadoNulaException("Identificacao do empregado nao pode ser nula.");
        }

        Empregado e = getEmpregadoPorId(emp);
        if (e == null) {
            throw new EmpregadoNaoExisteException("Empregado nao existe.");
        }

        empregadosMap.remove(emp);
    }

    public void lancaCartao(String emp, String dataStr, String horasStr) throws EmpregadoNaoExisteException {
        if (emp == null || emp.trim().isEmpty()) {
            throw new IdentificacaoDoEmpregadoNulaException("Identificacao do empregado nao pode ser nula.");
        }
        Empregado e = getEmpregadoPorId(emp);
        if (e == null) {
            throw new EmpregadoNaoExisteException("Empregado nao existe.");
        }

        if (!"horista".equals(e.getTipo()))
            throw new NaoEhHoristaException("Empregado nao eh horista.");

        LocalDate data;
        try {
            data = LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("d/M/yyyy"));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Data invalida.");
        }

        double horas = Double.parseDouble(horasStr.replace(',', '.'));
        if (horas <= 0)
            throw new IllegalArgumentException("Horas devem ser positivas.");

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
            e.setRegistroDeHoras(new RegistroDeHoras(data, normais, extras));
        }

        salvarRegistros();
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        if (emp == null || emp.trim().isEmpty()) {
            throw new IdentificacaoDoEmpregadoNulaException("Identificacao do empregado nao pode ser nula.");
        }

        Empregado e = getEmpregadoPorId(emp);
        if (e == null) {
            throw new EmpregadoNaoExisteException("Empregado nao existe.");
        }

        if (!"horista".equals(e.getTipo())) {
            throw new NaoEhHoristaException("Empregado nao eh horista.");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/uuuu")
                .withResolverStyle(ResolverStyle.STRICT);

        LocalDate inicial;
        LocalDate fim;
        try {
            inicial = LocalDate.parse(dataInicial, formatter);
        } catch (DateTimeParseException ex) {
            throw new DataInvalidaException("Data inicial invalida.");
        }

        try {
            fim = LocalDate.parse(dataFinal, formatter);
        } catch (DateTimeParseException ex) {
            throw new DataInvalidaException("Data final invalida.");
        }

        if (inicial.isAfter(fim)) {
            throw new IllegalArgumentException("Data inicial nao pode ser posterior aa data final.");
        }

        double total = 0;
        for (RegistroDeHoras r : e.getRegistrosDeHoras()) {
            if (!r.getData().isBefore(inicial) && r.getData().isBefore(fim)) {
                total += r.getHorasNormais();
            }
        }

        if (total == (int) total) {
            return String.valueOf((int) total);
        } else {
            return String.format("%.1f", total).replace('.', ',');
        }
    }

    // Soma horas extras no intervalo
    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        if (emp == null || emp.trim().isEmpty()) {
            throw new IdentificacaoDoEmpregadoNulaException("Identificacao do empregado nao pode ser nula.");
        }

        Empregado e = getEmpregadoPorId(emp);
        if (e == null) {
            throw new EmpregadoNaoExisteException("Empregado nao existe.");
        }
        if (!"horista".equals(e.getTipo())) {
            throw new NaoEhHoristaException("Empregado nao eh horista.");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/uuuu")
                .withResolverStyle(ResolverStyle.STRICT);

        LocalDate inicial;
        LocalDate fim;
        try {
            inicial = LocalDate.parse(dataInicial, formatter);
        } catch (DateTimeParseException ex) {
            throw new DataInvalidaException("Data inicial invalida.");
        }

        try {
            fim = LocalDate.parse(dataFinal, formatter);
        } catch (DateTimeParseException ex) {
            throw new DataInvalidaException("Data final invalida.");
        }

        if (inicial.isAfter(fim)) {
            throw new IllegalArgumentException("Data inicial nao pode ser posterior aa data final.");
        }

        double total = 0;
        for (RegistroDeHoras r : e.getRegistrosDeHoras()) {
            if (!r.getData().isBefore(inicial) && r.getData().isBefore(fim)) {
                total += r.getHorasExtras();
            }
        }

        if (total == (int) total) {
            return String.valueOf((int) total);
        } else {
            return String.format("%.1f", total).replace('.', ',');
        }
    }

    private void salvarRegistros() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("registros.csv"))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

            for (Map.Entry<String, Empregado> entry : empregadosMap.entrySet()) {
                String empId = entry.getKey();
                Empregado e = entry.getValue();

                for (RegistroDeHoras r : e.getRegistrosDeHoras()) {
                    String linha = empId + ";" + r.getData().format(formatter) + ";" + r.getHorasNormais() + ";" + r.getHorasExtras();
                    pw.println(linha);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar registros: " + e.getMessage());
        }
    }

    private void salvarEmpregados() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARQUIVO))) {
            for (Map.Entry<String, Empregado> entry : empregadosMap.entrySet()) {
                Empregado e = entry.getValue();
                pw.printf("%s;%s;%s;%s;%.2f;%.2f;%b%n",
                        entry.getKey(),
                        e.getNome(),
                        e.getEndereco(),
                        e.getTipo(),
                        e.getSalario(),
                        e.getComissao(),
                        e.getSindicalizado());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void carregarEmpregados() throws EmpregadoNaoExisteException {
        Path path = Paths.get("empregados.csv");
        if (!Files.exists(path)) return; // se arquivo não existe, nada a carregar

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String linha;
            while ((linha = br.readLine()) != null) {
                // Espera-se que o CSV seja: id;nome;endereco;tipo;salario;comissao;sindicalizado
                String[] partes = linha.split(";");
                if (partes.length < 6) continue; // linha inválida, pula

                String id = partes[0].trim();
                String nome = partes[1].trim();
                String endereco = partes[2].trim();
                String tipo = partes[3].trim().toLowerCase();

                double salario = 0.0;
                try {
                    salario = Double.parseDouble(partes[4].replace(',', '.'));
                    if (salario < 0) salario = 0.0; // tratamento extra para negativo
                } catch (NumberFormatException e) {
                    System.err.println("Salario inválido para " + nome + ", usando 0.0");
                }

                double comissao = 0.0;
                try {
                    if (partes.length >= 6 && !partes[5].trim().isEmpty())
                        comissao = Double.parseDouble(partes[5].replace(',', '.'));
                    if (comissao < 0) comissao = 0.0; // tratamento extra para negativo
                } catch (NumberFormatException e) {
                    comissao = 0.0;
                }

                boolean sindicalizado = false;
                if (partes.length >= 7) {
                    sindicalizado = Boolean.parseBoolean(partes[6].trim());
                }

                Empregado e = new Empregado(nome, endereco, tipo, salario);
                e.setComissao(comissao);

                empregadosMap.put(id, e);

                // Atualiza proximoId para não sobrescrever IDs
                try {
                    int idNum = Integer.parseInt(id.replaceAll("[^0-9]", ""));
                    if (idNum >= proximoId) proximoId = idNum + 1;
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo de empregados: " + e.getMessage());
        }
    }

    public void carregarRegistrosCSV() throws EmpregadoNaoExisteException {
        File arquivo = new File("registros.csv");
        if (!arquivo.exists()) return; // Se não existir, nada a carregar

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length != 4) continue; // Linha inválida, ignora

                String empId = partes[0];
                LocalDate data = LocalDate.parse(partes[1], formatter);
                double normais = Double.parseDouble(partes[2]);
                double extras = Double.parseDouble(partes[3]);

                try {
                    Empregado e = getEmpregadoPorId(empId);
                    RegistroDeHoras registro = new RegistroDeHoras(data, normais, extras);
                    e.setRegistroDeHoras(registro);
                } catch (Exception ex) {
                    System.err.println("Registro para empregado inexistente: " + empId);
                }
            }

        } catch (IOException e) {
            System.err.println("Erro ao carregar registros: " + e.getMessage());
        }
    }
}