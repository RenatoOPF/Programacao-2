package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
import br.ufal.ic.p2.wepayu.service.EmpregadosService;
import br.ufal.ic.p2.wepayu.service.RegistroDeHorasService;
import br.ufal.ic.p2.wepayu.service.TaxaServicoService;
import br.ufal.ic.p2.wepayu.service.VendasService;

import java.util.Map;

public class Facade {
    private EmpregadosService empregadosService;
    private RegistroDeHorasService registroService;
    private VendasService vendaService;
    private TaxaServicoService taxaService;

    public Facade() {
        empregadosService = new EmpregadosService();
        empregadosService.carregarEmpregados();

        registroService = new RegistroDeHorasService(empregadosService.getEmpregadosMap());
        registroService.carregarRegistros();

        vendaService = new VendasService(empregadosService.getEmpregadosMap());
        vendaService.carregarVendas();

        taxaService = new TaxaServicoService(empregadosService.getEmpregadosMap());
        taxaService.carregarTaxas();
    }

    public void zerarSistema() {
        empregadosService.zerar();
        registroService.zerar();
        vendaService.zerar();
        taxaService.zerar();
    }

    public void encerrarSistema() {
        empregadosService.salvar();
        registroService.salvar();
        vendaService.salvar();
        taxaService.salvar();
    }

    // ---------- Empregados ----------
    public String criarEmpregado(String nome, String endereco, String tipo, String salario) {
        return empregadosService.criarEmpregado(nome, endereco, tipo, salario);
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) {
        return empregadosService.criarEmpregadoComComissao(nome, endereco, tipo, salario, comissao);
    }

    public String getAtributoEmpregado(String emp, String atributo) {
        return empregadosService.getAtributo(emp, atributo);
    }

    public String getEmpregadoPorNome(String nome, int indice) {
        return empregadosService.getEmpregadoPorNome(nome, indice);
    }

    public void removerEmpregado(String emp) {
        empregadosService.remover(emp);
    }

    // ---------- Registro de horas ----------
    public void lancaCartao(String emp, String data, String horas) {
        registroService.lancarCartao(emp, data, horas);
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) {
        return registroService.getHorasNormais(emp, dataInicial, dataFinal);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) {
        return registroService.getHorasExtras(emp, dataInicial, dataFinal);
    }

    // ---------- Vendas ----------
    public void lancaVenda(String emp, String data, String valor) {
        vendaService.lancarVenda(emp, data, valor);
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) {
        return vendaService.getVendas(emp, dataInicial, dataFinal);
    }

    public void alteraEmpregado(String empId, String atributo, String valor) {
        empregadosService.alterarEmpregado(empId, atributo, valor, null, null, null);
    }

    public void alteraEmpregado(String empId, String atributo, String valor, String idSindicato, String taxaSindical) {
        empregadosService.alterarEmpregado(empId, atributo, valor,  idSindicato, taxaSindical, null);
    }

    public void alteraEmpregado(String empId, String atributo, String valor1, String banco, String agencia, String contaCorrente) {
        empregadosService.alterarEmpregado(empId, atributo, valor1,  banco, agencia, contaCorrente);
    }

    public void alteraEmpregado(String empId, String atributo, String valor, String comissao) {
        empregadosService.alterarEmpregado(empId, atributo, valor,  comissao, null, null);
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws Exception {
        taxaService.lancarTaxaServico(membro, data, Double.parseDouble(valor.replace(',', '.')));
    }

    public String getTaxasServico(String emp, String dataInicial, String dataFinal) throws Exception {
        return taxaService.getTaxas(emp, dataInicial, dataFinal);
    }

    public void imprimirEmpregados() {
        empregadosService.getEmpregadosMap().forEach((id, e) -> {
            System.out.println("ID: " + id
                    + " | Nome: " + e.getNome()
                    + " | Sindicalizado: " + e.getSindicalizado()
                    + " | ID Sindicato: " + e.getIdSindicato()
                    + " | TaxaSindical: " + e.getTaxaSindical()
                    + " | Tipo: " + e.getTipo()
            );
        });
    }
}
