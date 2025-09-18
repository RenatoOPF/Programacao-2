package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
import br.ufal.ic.p2.wepayu.service.EmpregadosService;
import br.ufal.ic.p2.wepayu.service.RegistroDeHorasService;
import br.ufal.ic.p2.wepayu.service.VendasService;

public class Facade {
    private EmpregadosService empregadosService;
    private RegistroDeHorasService registroService;
    private VendasService vendaService;

    public Facade() throws EmpregadoNaoExisteException {
        empregadosService = new EmpregadosService();
        empregadosService.carregarEmpregados();

        registroService = new RegistroDeHorasService(empregadosService.getEmpregadosMap());
        registroService.carregarRegistros();

        vendaService = new VendasService(empregadosService.getEmpregadosMap());
        vendaService.carregarVendas();
    }

    public void zerarSistema() {
        empregadosService.zerar();
        registroService.zerar();
        vendaService.zerar();
    }

    public void encerrarSistema() {
        empregadosService.salvar();
        registroService.salvar();
        vendaService.salvar();
    }

    // ---------- Empregados ----------
    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws EmpregadoNaoExisteException {
        return empregadosService.criarEmpregado(nome, endereco, tipo, salario);
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws EmpregadoNaoExisteException {
        return empregadosService.criarEmpregadoComComissao(nome, endereco, tipo, salario, comissao);
    }

    public String getAtributoEmpregado(String emp, String atributo) throws EmpregadoNaoExisteException {
        return empregadosService.getAtributo(emp, atributo);
    }

    public String getEmpregadoPorNome(String nome, int indice) throws EmpregadoNaoExisteException {
        return empregadosService.getEmpregadoPorNome(nome, indice);
    }

    public void removerEmpregado(String emp) throws EmpregadoNaoExisteException {
        empregadosService.remover(emp);
    }

    // ---------- Registro de horas ----------
    public void lancaCartao(String emp, String data, String horas) throws EmpregadoNaoExisteException {
        registroService.lancarCartao(emp, data, horas);
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        return registroService.getHorasNormais(emp, dataInicial, dataFinal);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        return registroService.getHorasExtras(emp, dataInicial, dataFinal);
    }

    // ---------- Vendas ----------
    public void lancaVenda(String emp, String data, String valor) throws EmpregadoNaoExisteException {
        vendaService.lancarVenda(emp, data, valor);
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        return vendaService.getVendas(emp, dataInicial, dataFinal);
    }
}
