package br.ufal.ic.p2.wepayu.Exception;

public class SalarioNegativoException extends RuntimeException {
    public SalarioNegativoException(String message) {
        super("Salario deve ser nao-negativo.");
    }
}
