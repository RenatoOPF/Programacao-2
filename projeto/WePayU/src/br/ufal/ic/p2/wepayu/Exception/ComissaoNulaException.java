package br.ufal.ic.p2.wepayu.Exception;

public class ComissaoNulaException extends RuntimeException {
    public ComissaoNulaException(String message) {
        super("Comissao nao pode ser nula.");
    }
}
