package br.ufal.ic.p2.wepayu.Exception;

public class ComissaoNegativaException extends RuntimeException {
    public ComissaoNegativaException(String message) {
        super("Comissao deve ser nao-negativa.");
    }
}
