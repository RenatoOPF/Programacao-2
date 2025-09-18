package br.ufal.ic.p2.wepayu.util;

import br.ufal.ic.p2.wepayu.Exception.*;

public class VerificaEmpregado {
    public static void validar(String nome, String endereco, String tipo, String salarioStr, String comissaoStr) {
        if(nome == null || nome.trim().isEmpty()) throw new NomeNuloException("Nome nao pode ser nulo.");
        if(endereco == null || endereco.trim().isEmpty()) throw new EnderecoNuloException("Endereco nao pode ser nulo.");
        if(!tipo.equalsIgnoreCase("horista") && !tipo.equalsIgnoreCase("assalariado") && !tipo.equalsIgnoreCase("comissionado"))
            throw new TipoInvalidoException("Tipo invalido.");

        try { if(Double.parseDouble(salarioStr.replace(',', '.')) < 0) throw new SalarioNegativoException(""); }
        catch(NumberFormatException e){ throw new SalarioNaoNumericoException(""); }

        if("comissionado".equalsIgnoreCase(tipo)){
            if(comissaoStr == null || comissaoStr.trim().isEmpty()) throw new ComissaoNulaException("");
            try { if(Double.parseDouble(comissaoStr.replace(',', '.')) < 0) throw new ComissaoNegativaException(""); }
            catch(NumberFormatException e){ throw new ComissaoNaoNumericaException(""); }
        }
    }
}
