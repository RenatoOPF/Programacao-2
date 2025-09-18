package br.ufal.ic.p2.wepayu.service;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.Empregado;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class EmpregadosService {
    private Map<String, Empregado> empregadosMap = new HashMap<>();
    private int proximoId = 1;
    private final String ARQUIVO = "empregados.csv";

    public void carregarEmpregados() throws EmpregadoNaoExisteException {
        Path path = Paths.get(ARQUIVO);
        if (!Files.exists(path)) return;

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length < 6) continue;

                String id = partes[0].trim();
                String nome = partes[1].trim();
                String endereco = partes[2].trim();
                String tipo = partes[3].trim().toLowerCase();

                double salario = parseDoubleSafe(partes[4]);
                double comissao = parseDoubleSafe(partes[5]);
                boolean sindicalizado = partes.length >= 7 && Boolean.parseBoolean(partes[6].trim());

                Empregado e = new Empregado(nome, endereco, tipo, salario);
                e.setComissao(comissao);

                empregadosMap.put(id, e);
                proximoId = Math.max(proximoId, extractIdNum(id) + 1);
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo de empregados: " + e.getMessage());
        }
    }

    private double parseDoubleSafe(String valor) {
        try {
            double v = Double.parseDouble(valor.replace(',', '.'));
            return v < 0 ? 0 : v;
        } catch (Exception e) {
            return 0;
        }
    }

    private int extractIdNum(String id) {
        try {
            return Integer.parseInt(id.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    public void zerar() {
        empregadosMap.clear();
        proximoId = 1;
        salvar();
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws EmpregadoNaoExisteException {
        validarEmpregadoBasico(nome, endereco, tipo, salario);
        if (tipo.equalsIgnoreCase("comissionado")) {
            throw new TipoNaoAplicavelException("Tipo nao aplicavel.");
        }

        double salarioNum = Double.parseDouble(salario.replace(',', '.'));
        String id = "id" + proximoId++;
        Empregado e = new Empregado(nome, endereco, tipo.toLowerCase(), salarioNum);
        empregadosMap.put(id, e);
        salvar();
        return id;
    }

    public String criarEmpregadoComComissao(String nome, String endereco, String tipo, String salario, String comissao) throws EmpregadoNaoExisteException {
        validarEmpregadoBasico(nome, endereco, tipo, salario);
        if (!tipo.equalsIgnoreCase("comissionado")) {
            throw new TipoNaoAplicavelException("Tipo nao aplicavel.");
        }
        if (comissao == null || comissao.trim().isEmpty()) {
            throw new ComissaoNulaException("Comissao nao pode ser nula.");
        }
        double comissaoNum;
        try {
            comissaoNum = Double.parseDouble(comissao.replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new ComissaoNaoNumericaException("Comissao deve ser numerica.");
        }
        if (comissaoNum < 0) throw new ComissaoNegativaException("Comissao deve ser nao-negativa.");

        double sal = Double.parseDouble(salario.replace(',', '.'));
        String id = "id" + proximoId++;
        Empregado e = new Empregado(nome, endereco, tipo.toLowerCase(), sal);
        e.setComissao(comissaoNum);
        empregadosMap.put(id, e);
        salvar();
        return id;
    }

    private void validarEmpregadoBasico(String nome, String endereco, String tipo, String salario) {
        if (nome == null || nome.trim().isEmpty()) throw new NomeNuloException("Nome nao pode ser nulo.");
        if (endereco == null || endereco.trim().isEmpty()) throw new EnderecoNuloException("Endereco nao pode ser nulo.");
        if (salario == null || salario.trim().isEmpty()) throw new SalarioNuloException("Salario nao pode ser nulo.");

        if (!tipo.equalsIgnoreCase("horista") && !tipo.equalsIgnoreCase("assalariado") && !tipo.equalsIgnoreCase("comissionado")) {
            throw new TipoInvalidoException("Tipo invalido.");
        }

        try {
            double sal = Double.parseDouble(salario.replace(',', '.'));
            if (sal < 0) throw new SalarioNegativoException("Salario deve ser nao-negativo.");
        } catch (NumberFormatException e) {
            throw new SalarioNaoNumericoException("Salario deve ser numerico.");
        }
    }

    public String getAtributo(String emp, String atributo) throws EmpregadoNaoExisteException {
        Empregado e = getEmpregado(emp);
        switch (atributo) {
            case "nome": return e.getNome();
            case "endereco": return e.getEndereco();
            case "tipo": return e.getTipo();
            case "salario": return String.format("%.2f", e.getSalario()).replace('.', ',');
            case "comissao": return String.format("%.2f", e.getComissao()).replace('.', ',');
            case "sindicalizado": return String.valueOf(e.getSindicalizado());
            default: throw new AtributoNaoExisteException("Atributo nao existe.");
        }
    }

    public String getEmpregadoPorNome(String nome, int indice) throws EmpregadoNaoExisteException {
        List<String> encontrados = new ArrayList<>();
        for (Map.Entry<String, Empregado> entry : empregadosMap.entrySet()) {
            if (entry.getValue().getNome().equals(nome)) encontrados.add(entry.getKey());
        }
        if (encontrados.isEmpty() || indice < 1 || indice > encontrados.size()) {
            throw new EmpregadoNaoExisteException("Nao ha empregado com esse nome.");
        }
        return encontrados.get(indice - 1);
    }

    public void remover(String emp) throws EmpregadoNaoExisteException {
        Empregado e = getEmpregado(emp);
        empregadosMap.remove(emp);
    }

    private Empregado getEmpregado(String emp) throws EmpregadoNaoExisteException {
        if (emp == null || emp.trim().isEmpty())
            throw new IdentificacaoDoEmpregadoNulaException("Identificacao do empregado nao pode ser nula.");

        Empregado e = empregadosMap.get(emp);
        if (e == null) throw new EmpregadoNaoExisteException("Empregado nao existe.");
        return e;
    }

    public void salvar() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARQUIVO))) {
            for (Map.Entry<String, Empregado> entry : empregadosMap.entrySet()) {
                Empregado e = entry.getValue();
                pw.printf("%s;%s;%s;%s;%.2f;%.2f;%b%n",
                        entry.getKey(), e.getNome(), e.getEndereco(),
                        e.getTipo(), e.getSalario(), e.getComissao(),
                        e.getSindicalizado());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Map<String, Empregado> getEmpregadosMap() {
        return empregadosMap;
    }
}
