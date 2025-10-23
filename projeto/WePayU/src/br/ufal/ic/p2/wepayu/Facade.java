package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.service.EmpregadosService;
import br.ufal.ic.p2.wepayu.service.RegistroDeHorasService;
import br.ufal.ic.p2.wepayu.service.TaxaServicoService;
import br.ufal.ic.p2.wepayu.service.VendasService;
import br.ufal.ic.p2.wepayu.service.FolhaDePagamentoService;
import br.ufal.ic.p2.wepayu.backup.BackupManager;

public class Facade {
    private EmpregadosService empregadosService;
    private RegistroDeHorasService registroService;
    private VendasService vendaService;
    private TaxaServicoService taxaService;
    private boolean sistemaEncerrado = false;

    public Facade() {
        empregadosService = new EmpregadosService();
        empregadosService.carregarEmpregados();

        registroService = new RegistroDeHorasService(empregadosService.getEmpregadosMap());
        registroService.carregarRegistros();

        vendaService = new VendasService(empregadosService.getEmpregadosMap());
        vendaService.carregarVendas();

        taxaService = new TaxaServicoService(empregadosService.getEmpregadosMap());
        taxaService.carregarTaxas();

        BackupManager.inicializar(empregadosService, registroService, vendaService, taxaService);
    }

    // ---------- Sistema ----------
    public void zerarSistema() {
        BackupManager.salvarEstado();
        empregadosService.zerar();
        registroService.zerar();
        vendaService.zerar();
        taxaService.zerar();
    }

    public void testLimparHistorico() {
        BackupManager.limparHistorico();
    }

    public void encerrarSistema() {
        sistemaEncerrado = true;
        empregadosService.salvar();
        registroService.salvar();
        vendaService.salvar();
        taxaService.salvar();
    }

    // ---------- Empregados ----------
    public String criarEmpregado(String nome, String endereco, String tipo, String salario) {
        BackupManager.salvarEstado();
        return empregadosService.criarEmpregado(nome, endereco, tipo, salario);
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) {
        BackupManager.salvarEstado();
        return empregadosService.criarEmpregadoComComissao(nome, endereco, tipo, salario, comissao);
    }

    public String getAtributoEmpregado(String emp, String atributo) {
        return empregadosService.getAtributo(emp, atributo);
    }

    public String getEmpregadoPorNome(String nome, int indice) {
        return empregadosService.getEmpregadoPorNome(nome, indice);
    }

    public void removerEmpregado(String emp) {
        BackupManager.salvarEstado();
        empregadosService.remover(emp);
    }

    // ---------- Registro de horas ----------
    public void lancaCartao(String emp, String data, String horas) {
        BackupManager.salvarEstado();
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
        BackupManager.salvarEstado();
        vendaService.lancarVenda(emp, data, valor);
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) {
        return vendaService.getTotalVendas(emp, dataInicial, dataFinal);
    }

    // ---------- Alterações ----------
    public void alteraEmpregado(String empId, String atributo, String valor) {
        BackupManager.salvarEstado();
        empregadosService.alterarEmpregado(empId, atributo, valor, null, null, null);
    }

    public void alteraEmpregado(String empId, String atributo, String valor, String idSindicato, String taxaSindical) {
        BackupManager.salvarEstado();
        empregadosService.alterarEmpregado(empId, atributo, valor, idSindicato, taxaSindical, null);
    }

    public void alteraEmpregado(String empId, String atributo, String valor1, String banco, String agencia, String contaCorrente) {
        BackupManager.salvarEstado();
        empregadosService.alterarEmpregado(empId, atributo, valor1, banco, agencia, contaCorrente);
    }

    public void alteraEmpregado(String empId, String atributo, String valor, String comissao) {
        BackupManager.salvarEstado();
        empregadosService.alterarEmpregado(empId, atributo, valor, comissao, null, null);
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws Exception {
        BackupManager.salvarEstado();
        taxaService.lancarTaxaServico(membro, data, Double.parseDouble(valor.replace(',', '.')));
    }

    public String getTaxasServico(String emp, String dataInicial, String dataFinal) throws Exception {
        return taxaService.getTotalTaxas(emp, dataInicial, dataFinal);
    }

    // ---------- Folha de pagamento ----------
    public void rodaFolha(String data, String saida) throws Exception {
        BackupManager.salvarEstado();
        FolhaDePagamentoService folha = new FolhaDePagamentoService(
                empregadosService.getEmpregadosMap(),
                registroService,
                vendaService,
                taxaService
        );
        folha.rodaFolha(data, saida);
    }

    public String totalFolha(String data) {
        try {
            FolhaDePagamentoService folha = new FolhaDePagamentoService(
                    empregadosService.getEmpregadosMap(),
                    registroService,
                    vendaService,
                    taxaService
            );
            double total = folha.totalFolha(data);
            return String.format("%.2f", total).replace('.', ',');
        } catch (Exception e) {
            e.printStackTrace();
            return "0,00";
        }
    }

    // ---------- Número de empregados ----------
    public int getNumeroDeEmpregados() {
        return empregadosService.getEmpregadosMap().size();
    }

    // ---------- undo e redo ----------

    public void undo() {
        if (sistemaEncerrado) {
            throw new RuntimeException("Nao pode dar comandos depois de encerrarSistema.");
        }

        BackupManager.undo();
        recarregarServicos();
    }

    public void redo() {
        if (sistemaEncerrado) {
            throw new RuntimeException("Nao pode dar comandos depois de encerrarSistema.");
        }

        BackupManager.redo();
        recarregarServicos();
    }

    private void recarregarServicos() {

        empregadosService.carregarEmpregados();
        registroService.carregarRegistros();
        vendaService.carregarVendas();
        taxaService.carregarTaxas();
    }


    // ---------- Utilitário ----------
    public void imprimirEmpregados() {
        empregadosService.getEmpregadosMap().forEach((id, e) -> {
            System.out.println("ID: " + id
                    + " | Nome: " + e.getNome()
                    + " | Sindicalizado: " + e.getSindicalizado()
                    + " | ID Sindicato: " + e.getIdSindicato()
                    + " | TaxaSindical: " + e.getTaxaSindical()
                    + " | Tipo: " + e.getTipo()
                    + " | Metodo de pagamento: " + e.getMetodoPagamento()
            );
        });
    }
}
