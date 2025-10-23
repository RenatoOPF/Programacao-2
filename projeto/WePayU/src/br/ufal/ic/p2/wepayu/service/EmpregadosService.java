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
                if (partes.length < 10) continue; // garante que tenha todos os campos

                String id = partes[0];
                String nome = partes[1];
                String endereco = partes[2];
                String tipo = partes[3];
                double salario = Double.parseDouble(partes[4].replace(',', '.'));
                boolean sindicalizado = Boolean.parseBoolean(partes[5]);
                String idSindicato = partes[6].isEmpty() ? null : partes[6];
                double taxaSindical = Double.parseDouble(partes[7].replace(',', '.'));
                double comissao = Double.parseDouble(partes[8].replace(',', '.'));
                String metodoDePagamento = partes[9].isEmpty() ? null : partes[9];

                Empregado e = new Empregado(id, nome, endereco, tipo, salario);
                e.setSindicalizado(sindicalizado);
                e.setIdSindicato(idSindicato);
                e.setTaxaSindical(taxaSindical);
                e.setComissao(comissao);
                e.setMetodoPagamento(metodoDePagamento);

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

    public void limpar() {
        empregadosMap.clear(); // Zera o mapa de empregados em memória
        proximoId = 1;
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
        Empregado e = new Empregado(id, nome, endereco, tipo, salarioNum);
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
        Empregado e = new Empregado(id, nome, endereco, tipo, sal);
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

    public String getAtributo(String empId, String atributo) {
        Empregado e = getEmpregado(empId);
        if (e == null) throw new EmpregadoNaoExisteException("Empregado nao existe.");
        if (empId == null || empId.trim().isEmpty()) throw new IdentificacaoDoEmpregadoNulaException("Identificacao do empregado nao pode ser nula.");

        switch (atributo) {
            case "nome":
                return e.getNome();

            case "endereco":
                return e.getEndereco();

            case "tipo":
                return e.getTipo();

            case "salario":
                return String.format("%.2f", e.getSalario()).replace('.', ',');

            case "comissao":
                if (!"comissionado".equals(e.getTipo()))
                    throw new RuntimeException("Empregado nao eh comissionado.");
                return String.format("%.2f", e.getComissao()).replace('.', ',');

            case "metodoPagamento":
                return e.getMetodoPagamento();

            case "banco":
            case "agencia":
            case "contaCorrente":
                if (!"banco".equalsIgnoreCase(e.getMetodoPagamento()))
                    throw new RuntimeException("Empregado nao recebe em banco.");
                switch (atributo) {
                    case "banco": return e.getBanco();
                    case "agencia": return e.getAgencia();
                    case "contaCorrente": return e.getContaCorrente();
                }

            case "sindicalizado":
                return String.valueOf(e.getSindicalizado());

            case "idSindicato":
                if (!e.getSindicalizado())
                    throw new RuntimeException("Empregado nao eh sindicalizado.");
                return e.getIdSindicato();

            case "taxaSindical":
                if (!e.getSindicalizado())
                    throw new RuntimeException("Empregado nao eh sindicalizado.");
                return String.format("%.2f", e.getTaxaSindical()).replace('.', ',');

            default:
                throw new RuntimeException("Atributo nao existe.");
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

    public void alterarEmpregado(String empId, String atributo, String valor, String valor1, String valor2, String valor3) {
        Empregado e =  getEmpregado(empId);

        validarEmpregadoBasico(e.getNome(), e.getEndereco(), e.getTipo(), String.valueOf(e.getSalario()));

        switch (atributo) {
            case "sindicalizado":
                boolean sindicalizado = Boolean.parseBoolean(valor);

                if (valor.equalsIgnoreCase("true")) {
                    e.setSindicalizado(true);
                } else if (valor.equalsIgnoreCase("false")) {
                    e.setSindicalizado(false);
                } else {
                    throw new RuntimeException("Valor deve ser true ou false.");
                }

                if (sindicalizado) {
                    String idSindicato = valor1;
                    if (idSindicato == null || idSindicato.trim().isEmpty()) {
                        throw new RuntimeException("Identificacao do sindicato nao pode ser nula.");
                    }

                    String taxaSindical = valor2;
                    if (taxaSindical == null || taxaSindical.trim().isEmpty()) {
                        throw new RuntimeException("Taxa sindical nao pode ser nula.");
                    }
                    try {
                        double d = Double.parseDouble(taxaSindical.replace(',', '.'));
                        if (d < 0) {
                            throw new RuntimeException("Taxa sindical deve ser nao-negativa.");
                        }
                    } catch (NumberFormatException ex) {
                        throw new RuntimeException("Taxa sindical deve ser numerica.");
                    }

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
            case "comissao":
                if (valor == null || valor.trim().isEmpty()) {
                    throw new RuntimeException("Comissao nao pode ser nula.");
                }
                if (!e.getTipo().equals("comissionado")) {
                    throw new RuntimeException("Empregado nao eh comissionado.");
                }
                try {
                    double d = Double.parseDouble(valor.replace(',', '.'));
                    if (d < 0) {
                        throw new RuntimeException("Comissao deve ser nao-negativa.");
                    }
                } catch (NumberFormatException ex) {
                    throw new RuntimeException("Comissao deve ser numerica.");
                }
                e.setComissao(Double.parseDouble(valor.replace(',', '.')));
                break;
            case "metodoPagamento":
                switch (valor) {
                    case "banco":
                        String banco = valor1;
                        String agencia = valor2;
                        String contaCorrente = valor3;

                        if (banco == null || banco.trim().isEmpty()) {
                            throw new RuntimeException("Banco nao pode ser nulo.");
                        }
                        if (agencia == null || agencia.trim().isEmpty()) {
                            throw new RuntimeException("Agencia nao pode ser nulo.");
                        }
                        if (contaCorrente == null || contaCorrente.trim().isEmpty()) {
                            throw new RuntimeException("Conta corrente nao pode ser nulo.");
                        }

                        e.setMetodoPagamento(valor);
                        e.setBanco(banco);
                        e.setAgencia(agencia);
                        e.setContaCorrente(contaCorrente);
                        break;
                    case "correios":
                        e.setMetodoPagamento(valor);
                        break;
                    case "emMaos":
                        e.setMetodoPagamento(valor);
                        break;
                    default:
                        throw new RuntimeException("Metodo de pagamento invalido.");
                }
                break;
            case "nome":
                if (valor == null || valor.trim().isEmpty()) {
                    throw new RuntimeException("Nome nao pode ser nulo.");
                }
                e.setNome(valor);
                break;
            case "endereco":
                if (valor == null || valor.trim().isEmpty()) {
                    throw new RuntimeException("Endereco nao pode ser nulo.");
                }
                e.setEndereco(valor);
                break;
            case "tipo":
                switch (valor) {
                    case "comissionado":
                        e.setTipo(valor);
                        e.setComissao(Double.parseDouble(valor1.replace(',', '.')));
                        break;
                    case "horista":
                        e.setTipo(valor);
                        e.setSalario(Double.parseDouble(valor1.replace(',', '.')));
                        break;
                    case "assalariado":
                        e.setTipo(valor);
                        break;
                    default:
                        throw new RuntimeException("Tipo invalido.");
                }
                break;
            case "salario":
                if (valor == null || valor.trim().isEmpty()) {
                    throw new RuntimeException("Salario nao pode ser nulo.");
                }
                try {
                    double d = Double.parseDouble(valor.replace(',', '.'));
                    if (d < 0) {
                        throw new RuntimeException("Salario deve ser nao-negativo.");
                    }
                } catch (NumberFormatException ex) {
                    throw new RuntimeException("Salario deve ser numerico.");
                }
                e.setSalario(Double.parseDouble(valor.replace(",", ".")));
                break;
            case "banco":
                break;
            case "agencia":
                break;
            case "contaCorrente":
                break;
            case "idSindicato":
                break;
            case "taxaSindical":
                break;
            default:
                throw new RuntimeException("Atributo nao existe.");
        }

        salvar();
    }

    public void salvar() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("empregados.csv"))) {
            for (Map.Entry<String, Empregado> entry : empregadosMap.entrySet()) {
                String id = entry.getKey();
                Empregado e = entry.getValue();
                pw.printf("%s;%s;%s;%s;%.2f;%b;%s;%.2f;%.2f;%s%n",
                        id,
                        e.getNome(),
                        e.getEndereco(),
                        e.getTipo(),
                        e.getSalario(),
                        e.getSindicalizado(),
                        e.getIdSindicato() != null ? e.getIdSindicato() : "",
                        e.getTaxaSindical(),
                        e.getComissao() != 0.0 ? e.getComissao() : 0.0,
                        e.getMetodoPagamento()
                );
            }
        } catch (IOException ex) {
            System.err.println("Erro ao salvar empregados: " + ex.getMessage());
        }
    }

    public Map<String, Empregado> cloneEmpregadosMap() {
        Map<String, Empregado> clone = new HashMap<>();
        for (Map.Entry<String, Empregado> entry : empregadosMap.entrySet()) {
            clone.put(entry.getKey(), entry.getValue().copiar()); // cada empregado precisa de um método copiar()
        }
        return clone;
    }

    public void restaurarEmpregados(Map<String, Empregado> clone) {
        empregadosMap.clear();
        empregadosMap.putAll(clone);
        salvar(); // opcional: sobrescrever CSV
    }

    public Map<String, Empregado> getEmpregadosMap() {
        return empregadosMap;
    }
}
