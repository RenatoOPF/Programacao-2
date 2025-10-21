package br.ufal.ic.p2.wepayu.service;

import br.ufal.ic.p2.wepayu.Exception.DataInvalidaException;
import br.ufal.ic.p2.wepayu.models.Empregado;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Serviço responsável por calcular e rodar a folha de pagamento.
 * Todos os métodos externos utilizam datas em formato String (d/M/uuuu).
 */
public class FolhaDePagamentoService {

    private Map<String, Empregado> empregados;
    private RegistroDeHorasService registroService;
    private VendasService vendasService;
    private TaxaServicoService taxaService;

    public FolhaDePagamentoService(Map<String, Empregado> empregados,
                                   RegistroDeHorasService registroService,
                                   VendasService vendasService,
                                   TaxaServicoService taxaService) {
        this.empregados = empregados;
        this.registroService = registroService;
        this.vendasService = vendasService;
        this.taxaService = taxaService;
    }

    /** Retorna o total que seria pago na data informada (data em formato "d/M/uuuu"). */
    public double totalFolha(String dataStr) throws Exception {
        LocalDate data = parseData(dataStr, "de cálculo");
        double totalFolhaBruto = 0.0;

        for (Empregado e : empregados.values()) {
            // Define a data de início do contrato
            LocalDate inicioContrato;
            if ("horista".equalsIgnoreCase(e.getTipo())) {
                if (e.getDataAdmissao() == null) continue; // segurança
                inicioContrato = e.getDataAdmissao();
            } else {
                inicioContrato = LocalDate.of(2005, 1, 1);
            }

            // Verifica se o empregado deve receber nesse dia
            if (!deveReceberNoDia(e, data)) continue;

            double bruto = 0.0;
            String inicioStr = "";
            String fimStr = formatData(data);

            // ===== HORISTA =====
            if ("horista".equalsIgnoreCase(e.getTipo())) {
                inicioStr = getInicioPeriodoStr(e, data);

                double horasNormais = 0.0;
                double horasExtras = 0.0;
                try {
                    horasNormais = Double.parseDouble(registroService.getHorasNormais(e.getId(), inicioStr, fimStr).replace(',', '.'));
                    horasExtras = Double.parseDouble(registroService.getHorasExtras(e.getId(), inicioStr, fimStr).replace(',', '.'));
                } catch (Exception ignored) {}

                bruto = horasNormais * e.getSalario() + horasExtras * e.getSalario() * 1.5;
            }

            // ===== ASSALARIADO =====
            else if ("assalariado".equalsIgnoreCase(e.getTipo())) {
                // Salário mensal completo
                bruto = e.getSalario();
            }

            // ===== COMISSIONADO =====
            else if ("comissionado".equalsIgnoreCase(e.getTipo())) {
                LocalDate fimPeriodo = data;
                LocalDate inicioPeriodo = fimPeriodo.minusDays(13);
                if (inicioPeriodo.isBefore(inicioContrato)) {
                    inicioPeriodo = inicioContrato;
                }

                inicioStr = formatData(inicioPeriodo);
                fimStr = formatData(fimPeriodo);

                // Salário fixo quinzenal
                double fixo = (e.getSalario() * 12) / 26;
                fixo = Math.floor(fixo * 100) / 100;

                // Vendas e comissão
                double vendas = 0.0;
                try {
                    String vendasRetorno = vendasService.getTotalVendas(e.getId(), inicioStr, fimStr);
                    if (vendasRetorno == null || vendasRetorno.isBlank()) vendasRetorno = "0";
                    vendas = Double.parseDouble(vendasRetorno.replace(',', '.'));
                } catch (Exception ignored) {}

                double comissao = vendas * e.getComissao();
                comissao = Math.floor(comissao * 100) / 100;
                bruto = fixo + comissao;
            }

            // Acumula no total da folha
            totalFolhaBruto += bruto;
        }

        return totalFolhaBruto;
    }

    private String alinharDireita(double valor, int largura) {
        String s = String.format("%.2f", valor).replace('.', ',');
        return String.format("%" + largura + "s", s);
    }

    private String alinharEsquerda(String texto, int largura) {
        return String.format("%-" + largura + "s", texto);
    }

    public void rodaFolha(String dataStr, String arquivoSaida) throws Exception {
        LocalDate data = parseData(dataStr, "de pagamento");

        try (PrintWriter out = new PrintWriter(new FileWriter(arquivoSaida))) {
            // Cabeçalho geral
            out.println("FOLHA DE PAGAMENTO DO DIA " + data.format(DateTimeFormatter.ISO_LOCAL_DATE));
            out.println("====================================\n");

            // ================= HORISTAS =================
            out.println("===============================================================================================================================");
            out.println("===================== HORISTAS ================================================================================================");
            out.println("===============================================================================================================================");
            out.println("Nome                                 Horas Extra Salario Bruto Descontos Salario Liquido Metodo");
            out.println("==================================== ===== ===== ============= ========= =============== ======================================");

            double totalHorasNormais = 0;
            double totalHorasExtras = 0;
            double totalHoristasBruto = 0;
            double totalHoristasDescontos = 0;
            double totalHoristasLiquido = 0;

            List<Empregado> horistas = empregados.values().stream()
                    .filter(e -> "horista".equalsIgnoreCase(e.getTipo()) && deveReceberNoDia(e, data))
                    .sorted(Comparator.comparing(Empregado::getNome))
                    .toList();

            for (Empregado e : horistas) {
                // DEBUG: início do processamento do empregado
//                System.out.println("DEBUG START: Empregado=" + e.getNome() + " id=" + e.getId());
//                System.out.println("  getDataAdmissao() = " + e.getDataAdmissao());
//                System.out.println("  getDataUltimoPagamento() = " + e.getDataUltimoPagamento());
//                System.out.println("  getDebitoSindicalAcumulado() = " + e.getDebitoSindicalAcumulado());
//                System.out.println("  getTaxaSindical() = " + e.getTaxaSindical());

                // Se não tem admissão → não recebe
                if (e.getDataAdmissao() == null) {
                    out.printf("%-36s %5s %5s %13s %9s %15s %s%n", e.getNome(),
                            "0", // horas normais
                            "0", // horas extras
                            alinharDireita(0, 12), // bruto
                            alinharDireita(0, 8), // descontos
                            alinharDireita(0, 14), // líquido
                            e.getMetodoPagamentoFormatado() ); continue; // próximo empregado
                }

                // Mesmo período para tudo
                String inicioStr = getInicioPeriodoStr(e, data);
                String fimStr = formatData(data);

//                System.out.println("DEBUG PERIOD STRINGS: inicioStr=" + inicioStr + " fimStr=" + fimStr);

                LocalDate inicioPeriodo = LocalDate.parse(inicioStr, DateTimeFormatter.ofPattern("d/M/uuuu"));
                long diasPeriodo = ChronoUnit.DAYS.between(inicioPeriodo, data) + 1;

                double horasNormais = 0.0;
                double horasExtras = 0.0;

                try {
                    horasNormais = Double.parseDouble(registroService.getHorasNormais(e.getId(), inicioStr, fimStr).replace(',', '.'));
                    horasExtras = Double.parseDouble(registroService.getHorasExtras(e.getId(), inicioStr, fimStr).replace(',', '.'));
                } catch (Exception ignored) {}

                double bruto = horasNormais * e.getSalario() + horasExtras * e.getSalario() * 1.5;

                // Descontos externos
                double descontosExternos = 0.0;
                try {
                    descontosExternos = Double.parseDouble(taxaService.getTotalTaxas(e.getId(), inicioStr, fimStr).replace(',', '.'));
                } catch (Exception ignored) {}

                // Taxa sindical: MESMO PERÍODO das horas e taxas
                double taxaSindicalTotal = e.getDebitoSindicalAcumulado() + e.getTaxaSindical() * diasPeriodo;

                double liquido = bruto - descontosExternos - taxaSindicalTotal;

                if (liquido < 0) {
                    liquido = 0;
                    // acumula débito
                    e.setDebitoSindicalAcumulado(taxaSindicalTotal);
                    descontosExternos = 0;
                    taxaSindicalTotal = 0;
                } else {
                    e.setDebitoSindicalAcumulado(0);
                }

                // Atualiza totais
                totalHorasNormais += horasNormais;
                totalHorasExtras += horasExtras;
                totalHoristasBruto += bruto;
                totalHoristasDescontos += descontosExternos + taxaSindicalTotal;
                totalHoristasLiquido += liquido;

                // Impressão
                out.printf("%-36s %5.0f %5.0f %13s %9s %15s %s%n",
                        e.getNome(),
                        horasNormais,
                        horasExtras,
                        alinharDireita(bruto, 12),
                        alinharDireita(descontosExternos + taxaSindicalTotal, 8),
                        alinharDireita(liquido, 14),
                        e.getMetodoPagamentoFormatado()
                );

                e.setDataUltimoPagamento(formatData(data));
            }

            // Totais horistas
            out.printf("%nTOTAL HORISTAS %27s %5s %13s %9s %15s %n%n",
                    String.format("%.0f", totalHorasNormais).replace('.', ','),
                    String.format("%.0f", totalHorasExtras).replace('.', ','),
                    String.format("%.2f", totalHoristasBruto).replace('.', ','),
                    String.format("%.2f", totalHoristasDescontos).replace('.', ','),
                    String.format("%.2f", totalHoristasLiquido).replace('.', ',')
            );

            // ================= ASSALARIADOS =================
            // ================= ASSALARIADOS =================
            out.println("===============================================================================================================================");
            out.println("===================== ASSALARIADOS ============================================================================================");
            out.println("===============================================================================================================================");
            out.println("Nome                                             Salario Bruto Descontos Salario Liquido Metodo");
            out.println("================================================ ============= ========= =============== ======================================");

            double totalAssalariadosBruto = 0;
            double totalAssalariadosDesconto = 0;
            double totalAssalariadosLiquido = 0;

            List<Empregado> assalariados = empregados.values().stream()
                    .filter(e -> "assalariado".equalsIgnoreCase(e.getTipo()) && deveReceberNoDia(e, data))
                    .sorted(Comparator.comparing(Empregado::getNome))
                    .toList();

            for (Empregado e : assalariados) {
                // Define o período do mês atual
                LocalDate inicioPeriodo = data.with(TemporalAdjusters.firstDayOfMonth());
                String inicioStr = formatData(inicioPeriodo);
                String fimStr = formatData(data);

                double bruto = e.getSalario();

                // Descontos externos
                double descontosExternos = 0.0;
                try {
                    descontosExternos = Double.parseDouble(
                            taxaService.getTotalTaxas(e.getId(), inicioStr, fimStr).replace(',', '.')
                    );
                } catch (Exception ignored) {}

                // Taxa sindical proporcional aos dias do mês
                long diasTrabalhados = java.time.temporal.ChronoUnit.DAYS.between(inicioPeriodo, data) + 1;
                double taxaSindicalTotal = e.getDebitoSindicalAcumulado() + e.getTaxaSindical() * diasTrabalhados;

                double liquido = bruto - descontosExternos - taxaSindicalTotal;

                if (liquido < 0) {
                    liquido = 0;
                    e.setDebitoSindicalAcumulado(taxaSindicalTotal);
                } else {
                    e.setDebitoSindicalAcumulado(0);
                }

                totalAssalariadosBruto += bruto;
                totalAssalariadosDesconto += descontosExternos + taxaSindicalTotal;
                totalAssalariadosLiquido += liquido;

                out.printf("%s %12s %8s %14s %s%n",
                        alinharEsquerda(e.getNome(), 45),
                        alinharDireita(bruto, 16),
                        alinharDireita(descontosExternos + taxaSindicalTotal, 9),
                        alinharDireita(liquido, 15),
                        e.getMetodoPagamentoFormatado()
                );

                e.setDataUltimoPagamento(formatData(data));
            }

            out.printf("%nTOTAL ASSALARIADOS %43s %9s %15s%n%n",
                    alinharDireita(totalAssalariadosBruto, 43),
                    alinharDireita(totalAssalariadosDesconto, 9),
                    alinharDireita(totalAssalariadosLiquido, 15)
            );

            // ================= COMISSIONADOS =================
            out.println("===============================================================================================================================");
            out.println("===================== COMISSIONADOS ===========================================================================================");
            out.println("===============================================================================================================================");
            out.println("Nome                  Fixo     Vendas   Comissao Salario Bruto Descontos Salario Liquido Metodo");
            out.println("===================== ======== ======== ======== ============= ========= =============== ======================================");

            double totalComissionadosFixo = 0.0;
            double totalComissionadosVendas = 0.0;
            double totalComissionadosComissao = 0.0;
            double totalComissionadosBruto = 0.0;
            double totalComissionadosDescontos = 0.0;
            double totalComissionadosLiquido = 0.0;

            List<Empregado> comissionados = empregados.values().stream()
                    .filter(e -> "comissionado".equalsIgnoreCase(e.getTipo()) && deveReceberNoDia(e, data))
                    .sorted(Comparator.comparing(Empregado::getNome))
                    .toList();

            for (Empregado e : comissionados) {
                // Período quinzenal
                LocalDate fimPeriodo = data;
                LocalDate inicioPeriodo;
                if (e.getDataUltimoPagamento() != null) {
                    inicioPeriodo = LocalDate.parse(e.getDataUltimoPagamento(), DateTimeFormatter.ofPattern("d/M/uuuu")).plusDays(1);
                } else {
                    // Primeiro pagamento: início do contrato
                    inicioPeriodo = LocalDate.of(2005, 1, 1);
                }
                // Ajusta para ter 14 dias
                if (ChronoUnit.DAYS.between(inicioPeriodo, fimPeriodo) < 13) {
                    inicioPeriodo = fimPeriodo.minusDays(13);
                }

                String inicioStr = formatData(inicioPeriodo);
                String fimStr = formatData(fimPeriodo);

                // Salário fixo quinzenal
                double fixo = (e.getSalario() * 12) / 26;
                fixo = Math.floor(fixo * 100) / 100;

                // Vendas
                double vendas = 0.0;
                try {
                    String vendasRetorno = vendasService.getTotalVendas(e.getId(), inicioStr, fimStr);
                    if (vendasRetorno == null || vendasRetorno.isBlank()) vendasRetorno = "0";
                    vendas = Double.parseDouble(vendasRetorno.replace(',', '.'));
                } catch (Exception ignored) {}

                // Comissão
                double comissao = vendas * e.getComissao();
                comissao = Math.floor(comissao * 100) / 100;

                // Salário bruto
                double bruto = fixo + comissao;

                // Descontos externos
                double descontosExternos = 0.0;
                try {
                    String taxas = taxaService.getTotalTaxas(e.getId(), inicioStr, fimStr);
                    if (taxas == null || taxas.isBlank()) taxas = "0";
                    descontosExternos = Double.parseDouble(taxas.replace(',', '.'));
                } catch (Exception ignored) {}

                // Taxa sindical quinzenal
                double taxaSindicalTotal = e.getDebitoSindicalAcumulado() + e.getTaxaSindical() * 14;

                // Salário líquido
                double liquido = bruto - descontosExternos - taxaSindicalTotal;

                // Horista/comissionado não pode ter contracheque negativo
                if (liquido < 0) {
                    liquido = 0;
                    e.setDebitoSindicalAcumulado(taxaSindicalTotal);
                    descontosExternos = 0;
                    taxaSindicalTotal = 0;
                } else {
                    e.setDebitoSindicalAcumulado(0);
                }

                // Atualiza totais
                totalComissionadosFixo += fixo;
                totalComissionadosVendas += vendas;
                totalComissionadosComissao += comissao;
                totalComissionadosBruto += bruto;
                totalComissionadosDescontos += descontosExternos + taxaSindicalTotal;
                totalComissionadosLiquido += liquido;

                // Impressão
                out.printf("%-20s %9s %8s %8s %13s %9s %15s %s%n",
                        e.getNome(),
                        String.format("%.2f", fixo).replace('.', ','),
                        String.format("%.2f", vendas).replace('.', ','),
                        String.format("%.2f", comissao).replace('.', ','),
                        String.format("%.2f", bruto).replace('.', ','),
                        String.format("%.2f", descontosExternos + taxaSindicalTotal).replace('.', ','),
                        String.format("%.2f", liquido).replace('.', ','),
                        e.getMetodoPagamentoFormatado()
                );

                e.setDataUltimoPagamento(formatData(fimPeriodo));
            }

// Totais
            out.printf("%nTOTAL COMISSIONADOS %10s %8s %8s %13s %9s %15s %n%n",
                    String.format("%.2f", totalComissionadosFixo).replace('.', ','),
                    String.format("%.2f", totalComissionadosVendas).replace('.', ','),
                    String.format("%.2f", totalComissionadosComissao).replace('.', ','),
                    String.format("%.2f", totalComissionadosBruto).replace('.', ','),
                    String.format("%.2f", totalComissionadosDescontos).replace('.', ','),
                    String.format("%.2f", totalComissionadosLiquido).replace('.', ',')
            );

            double totalFolha = totalHoristasBruto + totalAssalariadosBruto + totalComissionadosBruto;
            out.printf("TOTAL FOLHA: %s%n", String.format("%.2f", totalFolha).replace('.', ','));
        }

    }

    /** Define se o empregado deve ser pago nessa data. */
    /** Define se o empregado deve ser pago nessa data. */
    private boolean deveReceberNoDia(Empregado e, LocalDate data) {
        String tipo = e.getTipo().toLowerCase();

        switch (tipo) {
            case "horista":
                // pago toda sexta-feira
                return data.getDayOfWeek() == DayOfWeek.FRIDAY;

            case "assalariado":
                // pago no último dia do mês
                LocalDate ultimoDia = data.with(TemporalAdjusters.lastDayOfMonth());
                return data.equals(ultimoDia);

            case "comissionado":
                // Contrato: 1/1/2005
                LocalDate contrato = LocalDate.of(2005, 1, 1);
                // Primeira sexta-feira quinzenal (data base): 14/1/2005
                LocalDate primeiraSexta = LocalDate.of(2005, 1, 14);

                // 1. Deve ser uma sexta-feira
                if (data.getDayOfWeek() != DayOfWeek.FRIDAY) return false;

                // 2. Deve ser na data ou após a primeira sexta quinzenal
                if (data.isBefore(primeiraSexta)) return false;

                // 3. A diferença em dias deve ser um múltiplo de 14
                long diasDesdePrimeiraSexta = ChronoUnit.DAYS.between(primeiraSexta, data);
                // Se a diferença em dias for divisível por 14, é uma quinzena.
                return diasDesdePrimeiraSexta % 14 == 0;
        }
        return false;
    }

    private int calcularDiasTrabalhados(Empregado e) {
        if (e.getTipo().toLowerCase().equals("horista")) {
            return 7;
        }
        if (e.getTipo().toLowerCase().equals("assalariado")) {
            return 14;
        }
        if (e.getTipo().toLowerCase().equals("comissionado")) {
            return 30;
        }
        return 0;
    }

    /** Define o início do período de pagamento, retornando como String. */
    private String getInicioPeriodoStr(Empregado e, LocalDate dataPagamento) {
        LocalDate admissao = e.getDataAdmissao();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d/M/uuuu");

//        System.out.println("\n=== DEBUG getInicioPeriodoStr ===");
//        System.out.println("Empregado: " + e.getNome());
//        System.out.println("Data de Pagamento: " + dataPagamento);
//        System.out.println("Data de Admissão: " + admissao);
//        System.out.println("Último Pagamento: " + e.getDataUltimoPagamento());

        LocalDate inicio;

        if (e.getDataUltimoPagamento() != null) {
            inicio = LocalDate.parse(e.getDataUltimoPagamento(), fmt).plusDays(1);
//            System.out.println("Base: último pagamento + 1 dia");
        } else {
            inicio = admissao;
//            System.out.println("Base: admissão (primeiro pagamento)");
        }

//        System.out.println("Início calculado do período: " + inicio);
//        System.out.println("=================================\n");

        return formatData(inicio);
    }

    /** Converte LocalDate para String no formato d/M/uuuu. */
    private String formatData(LocalDate data) {
        return data.format(DateTimeFormatter.ofPattern("d/M/uuuu"));
    }

    /** Faz o parse de String para LocalDate com validação. */
    private LocalDate parseData(String dataStr, String tipo) throws DataInvalidaException {
        try {
            return LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("d/M/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT));
        } catch (DateTimeParseException ex) {
            throw new DataInvalidaException("Data " + tipo + " invalida.");
        }
    }

    /** Parse silencioso (para uso interno em cálculos opcionais). */
    private LocalDate parseDataSilencioso(String dataStr) {
        try {
            return LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("d/M/uuuu"));
        } catch (Exception ex) {
            return null;
        }
    }
}
