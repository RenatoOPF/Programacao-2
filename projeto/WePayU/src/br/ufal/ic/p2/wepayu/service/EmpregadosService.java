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

    public void carregarEmpregados() {
        File arquivo = new File("empregados.csv");
        if (!arquivo.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length < 9) continue; // garante que tenha todos os campos

                String id = partes[0];
                String nome = partes[1];
                String endereco = partes[2];
                String tipo = partes[3];
                double salario = Double.parseDouble(partes[4].replace(',', '.'));
                boolean sindicalizado = Boolean.parseBoolean(partes[5]);
                String idSindicato = partes[6].isEmpty() ? null : partes[6];
                double taxaSindical = Double.parseDouble(partes[7].replace(',', '.'));
                double comissao = Double.parseDouble(partes[8].replace(',', '.'));

                Empregado e = new Empregado(nome, endereco, tipo, salario);
                e.setSindicalizado(sindicalizado);
                e.setIdSindicato(idSindicato);
                e.setTaxaSindical(taxaSindical);
                e.setComissao(comissao);

                empregadosMap.put(id, e);
            }
        } catch (IOException | NumberFormatException ex) {
            System.err.println("Erro ao carregar empregados: " + ex.getMessage());
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

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) {
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

    public String criarEmpregadoComComissao(String nome, String endereco, String tipo, String salario, String comissao) {
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

    public String getAtributo(String emp, String atributo) {
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

    public String getEmpregadoPorNome(String nome, int indice) {
        List<String> encontrados = new ArrayList<>();
        for (Map.Entry<String, Empregado> entry : empregadosMap.entrySet()) {
            if (entry.getValue().getNome().equals(nome)) encontrados.add(entry.getKey());
        }
        if (encontrados.isEmpty() || indice < 1 || indice > encontrados.size()) {
            throw new EmpregadoNaoExisteException("Nao ha empregado com esse nome.");
        }
        return encontrados.get(indice - 1);
    }

    public void remover(String emp) {
        Empregado e = getEmpregado(emp);
        empregadosMap.remove(emp);
    }

    private Empregado getEmpregado(String emp) {
        if (emp == null || emp.trim().isEmpty())
            throw new IdentificacaoDoEmpregadoNulaException("Identificacao do empregado nao pode ser nula.");

        Empregado e = empregadosMap.get(emp);
        if (e == null) throw new EmpregadoNaoExisteException("Empregado nao existe.");
        return e;
    }

    // Altera empregado passando idSindicato e taxaSindical — obrigatório para sindicalizar
    public void alterarEmpregado(String empId, String atributo, String valor, String idSindicato, String taxaSindical) {
        Empregado e = getEmpregado(empId);
        if (e == null) throw new EmpregadoNaoExisteException("Empregado nao existe.");

        switch (atributo.toLowerCase()) {
            case "sindicalizado":
                boolean sindicalizado = Boolean.parseBoolean(valor);
                e.setSindicalizado(sindicalizado);

                if (sindicalizado) {
                    // valida se já existe outro empregado com mesmo idSindicato
                    boolean existe = getEmpregadosMap().values().stream()
                            .anyMatch(emp -> emp != e && idSindicato.equals(emp.getIdSindicato()));
                    if (existe) {
                        throw new RuntimeException("Ha outro empregado com esta identificacao de sindicato");
                    }
                    e.setIdSindicato(idSindicato);
                    e.setTaxaSindical(Double.parseDouble(taxaSindical.replace(',', '.')));
                } else {
                    e.setIdSindicato(null);
                    e.setTaxaSindical(0.0);
                }
                break;

            case "nome":
                e.setNome(valor);
                break;

            case "endereco":
                e.setEndereco(valor);
                break;

            case "tipo":
                e.setTipo(valor.toLowerCase());
                break;

            case "salario":
                e.setSalario(Double.parseDouble(valor.replace(',', '.')));
                break;

            default:
                throw new RuntimeException("Atributo nao existe.");
        }

        salvar(); // salva alterações
    }

    // Altera empregado sem idSindicato/taxaSindical — nunca use para sindicalizar!
    public void alterarEmpregado(String empId, String atributo, String valor) {
        Empregado e = getEmpregado(empId);
        if (e == null) throw new EmpregadoNaoExisteException("Empregado nao existe.");

        switch (atributo.toLowerCase()) {
            case "sindicalizado":
                // Se chamar este método com "true", o empregado **não terá sindicato nem taxa**, e será considerado não sindicalizado
                e.setSindicalizado(Boolean.parseBoolean(valor));
                break;

            case "nome":
                e.setNome(valor);
                break;

            case "endereco":
                e.setEndereco(valor);
                break;

            case "tipo":
                e.setTipo(valor.toLowerCase());
                break;

            case "salario":
                e.setSalario(Double.parseDouble(valor.replace(',', '.')));
                break;

            default:
                throw new RuntimeException("Atributo nao existe.");
        }

        salvar(); // salva alterações
    }

    public void salvar() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("empregados.csv"))) {
            for (Map.Entry<String, Empregado> entry : empregadosMap.entrySet()) {
                String id = entry.getKey();
                Empregado e = entry.getValue();
                pw.printf("%s;%s;%s;%s;%.2f;%b;%s;%.2f;%.2f%n",
                        id,
                        e.getNome(),
                        e.getEndereco(),
                        e.getTipo(),
                        e.getSalario(),
                        e.getSindicalizado(),
                        e.getIdSindicato() != null ? e.getIdSindicato() : "",
                        e.getTaxaSindical(),
                        e.getComissao() != 0.0 ? e.getComissao() : 0.0
                );
            }
        } catch (IOException ex) {
            System.err.println("Erro ao salvar empregados: " + ex.getMessage());
        }
    }


    public Map<String, Empregado> getEmpregadosMap() {
        return empregadosMap;
    }
}
