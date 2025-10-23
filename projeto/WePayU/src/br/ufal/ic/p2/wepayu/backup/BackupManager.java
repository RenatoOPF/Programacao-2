package br.ufal.ic.p2.wepayu.backup;

import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.RegistroDeHoras;
import br.ufal.ic.p2.wepayu.models.Venda;
import br.ufal.ic.p2.wepayu.models.TaxaServico;
import br.ufal.ic.p2.wepayu.service.EmpregadosService;
import br.ufal.ic.p2.wepayu.service.RegistroDeHorasService;
import br.ufal.ic.p2.wepayu.service.VendasService;
import br.ufal.ic.p2.wepayu.service.TaxaServicoService;

import java.util.*;

public class BackupManager {

    private static final Stack<Snapshot> undoStack = new Stack<>();
    private static final Stack<Snapshot> redoStack = new Stack<>();

    private static EmpregadosService empregadosService;
    private static RegistroDeHorasService registroService;
    private static VendasService vendaService;
    private static TaxaServicoService taxaService;

    public static void inicializar(EmpregadosService e, RegistroDeHorasService r,
                                   VendasService v, TaxaServicoService t) {
        limparHistorico();
        empregadosService = e;
        registroService = r;
        vendaService = v;
        taxaService = t;
    }

    public static void salvarEstado() {

        Snapshot snapshot = criarSnapshot();
        undoStack.push(snapshot);
        redoStack.clear();
    }

    public static void undo() {
        if (undoStack.isEmpty()) {
            throw new RuntimeException("Nao ha comando a desfazer.");
        }

        Snapshot snapshotAtual = criarSnapshot(); // salva o estado atual para redo
        redoStack.push(snapshotAtual);

        Snapshot snapshotParaRestaurar = undoStack.pop();
        restaurar(snapshotParaRestaurar);
    }

    public static void redo() {
        if (redoStack.isEmpty()) {
            throw new RuntimeException("Nao ha comando a refazer.");
        }

        Snapshot snapshotAtual = criarSnapshot();
        undoStack.push(snapshotAtual);

        Snapshot snapshotParaRestaurar = redoStack.pop();
        restaurar(snapshotParaRestaurar);
    }

    // ---------- Métodos auxiliares ----------

    private static Snapshot criarSnapshot() {
        Map<String, Empregado> empregadosClone = deepCloneEmpregados();
        Map<String, List<RegistroDeHoras>> registrosClone = deepCloneRegistros();
        Map<String, List<Venda>> vendasClone = deepCloneVendas();
        Map<String, List<TaxaServico>> taxasClone = deepCloneTaxas();

        return new Snapshot(empregadosClone, registrosClone, vendasClone, taxasClone);
    }

    private static void restaurar(Snapshot snapshot) {
        empregadosService.restaurarEmpregados(snapshot.empregadosMap);
        registroService.restaurarRegistros(snapshot.registros);
        vendaService.restaurarVendas(snapshot.vendas);
        taxaService.restaurarTaxas(snapshot.taxas);
    }

    // ---------- Deep clone ----------

    private static Map<String, Empregado> deepCloneEmpregados() {
        Map<String, Empregado> clone = new HashMap<>();
        for (Map.Entry<String, Empregado> entry : empregadosService.getEmpregadosMap().entrySet()) {
            clone.put(entry.getKey(), entry.getValue().copiar());
        }
        return clone;
    }

    private static Map<String, List<RegistroDeHoras>> deepCloneRegistros() {
        Map<String, List<RegistroDeHoras>> clone = new HashMap<>();
        for (Empregado e : empregadosService.getEmpregadosMap().values()) {
            List<RegistroDeHoras> registrosClone = new ArrayList<>();
            for (RegistroDeHoras r : e.getRegistrosDeHoras()) {
                registrosClone.add(r.copiar());
            }
            clone.put(e.getId(), registrosClone);
        }
        return clone;
    }

    private static Map<String, List<Venda>> deepCloneVendas() {
        Map<String, List<Venda>> clone = new HashMap<>();
        for (Empregado e : empregadosService.getEmpregadosMap().values()) {
            List<Venda> vendasClone = new ArrayList<>();
            for (Venda v : e.getVendas()) {
                vendasClone.add(v.copiar());
            }
            clone.put(e.getId(), vendasClone);
        }
        return clone;
    }

    private static Map<String, List<TaxaServico>> deepCloneTaxas() {
        Map<String, List<TaxaServico>> clone = new HashMap<>();
        for (Empregado e : empregadosService.getEmpregadosMap().values()) {
            List<TaxaServico> taxasClone = new ArrayList<>();
            for (TaxaServico t : e.getTaxasServico()) {
                taxasClone.add(t.copiar());
            }
            clone.put(e.getId(), taxasClone);
        }
        return clone;
    }

    // ---------- Snapshot interno ----------
    private static class Snapshot {
        private final Map<String, Empregado> empregadosMap;
        private final Map<String, List<RegistroDeHoras>> registros;
        private final Map<String, List<Venda>> vendas;
        private final Map<String, List<TaxaServico>> taxas;

        private Snapshot(Map<String, Empregado> e, Map<String, List<RegistroDeHoras>> r,
                         Map<String, List<Venda>> v, Map<String, List<TaxaServico>> t) {
            this.empregadosMap = e;
            this.registros = r;
            this.vendas = v;
            this.taxas = t;
        }
    }

    public static void limparHistorico() {
        undoStack.clear();
        redoStack.clear();
    }
}
