    package br.ufal.ic.p2.wepayu;

    import br.ufal.ic.p2.wepayu.Exception.*;
    import br.ufal.ic.p2.wepayu.models.Empregado;

    import java.util.HashMap;
    import java.util.Map;
    import java.util.ArrayList;
    import java.util.List; // se você quiser usar List como tipo genérico


    public class Facade {
        private Map<String, Empregado> empregadosMap = new HashMap<>();
        private int proximoId = 1; // para gerar IDs automáticos

        public void zerarSistema() {
            empregadosMap.clear();
            proximoId = 1;
        }

        public void encerrarSistema() {
            zerarSistema();
        }

        public String criarEmpregado(String nome, String endereco, String tipo, String salarioString) throws EmpregadoNaoExisteException {
            if (nome == null || nome.trim().isEmpty()) {
                throw new NomeNuloException("Nome nao pode ser nulo.");
            }

            if (endereco == null || endereco.trim().isEmpty()) {
                throw new EnderecoNuloException("Endereco nao pode ser nulo.");
            }

            if (tipo.equalsIgnoreCase("comissionado") ) {

                throw new TipoNaoAplicavelException("Tipo nao aplicavel.");
            }

            if (!tipo.equalsIgnoreCase("horista") && !tipo.equalsIgnoreCase("assalariado")) {

                throw new TipoInvalidoException("Tipo invalido.");
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

            // Valores padrão
            if (tipo.equalsIgnoreCase("comissionado")) e.setComissao(0.0);

            empregadosMap.put(id, e);
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
                throw new IllegalArgumentException("Salario nao pode ser nulo.");
            }

            double salario;
            try {
                salario = Double.parseDouble(salarioString.replace(',', '.'));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Salario deve ser numerico.");
            }
            if (salario < 0) throw new IllegalArgumentException("Salario deve ser nao-negativo.");

            // valida comissao
            if (comissaoString == null || comissaoString.trim().isEmpty()) {
                throw new IllegalArgumentException("Comissao nao pode ser nula.");
            }
            double comissao;
            try {
                comissao = Double.parseDouble(comissaoString.replace(',', '.'));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Comissao deve ser numerica.");
            }
            if (comissao < 0) throw new IllegalArgumentException("Comissao deve ser nao-negativa.");

            String id = "id" + proximoId++;
            Empregado e = new Empregado(nome, endereco, tipo.toLowerCase(), salario);
            e.setComissao(comissao);
            empregadosMap.put(id, e);
            return id;
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
                case "sindicalizado": return e.getSindicalizado(); // implementar depois
                default: throw new AtributoNaoExisteException("Atributo nao existe.");
            }
        }

        // Método auxiliar
        private Empregado getEmpregadoPorId(String emp) {
            // Procura o empregado na sua lista/mapa de empregados
            return empregadosMap.get(emp); // Exemplo usando um HashMap<String, Empregado>
        }

    }
