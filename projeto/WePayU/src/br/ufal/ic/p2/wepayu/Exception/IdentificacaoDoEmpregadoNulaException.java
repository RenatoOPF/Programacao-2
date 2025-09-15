package br.ufal.ic.p2.wepayu.Exception;

public class IdentificacaoDoEmpregadoNulaException extends RuntimeException {
    public IdentificacaoDoEmpregadoNulaException(String message) { super("Identificacao do empregado nao pode ser nula."); }
}
