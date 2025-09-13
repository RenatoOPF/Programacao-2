package br.ufal.ic.p2.wepayu.Exception;

public class SalarioNuloException extends RuntimeException {
    public SalarioNuloException(String message) {
        super("Salario nao pode ser nulo.");
    }
}
