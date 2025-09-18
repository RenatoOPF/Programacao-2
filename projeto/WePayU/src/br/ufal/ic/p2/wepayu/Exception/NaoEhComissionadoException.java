package br.ufal.ic.p2.wepayu.Exception;

public class NaoEhComissionadoException extends RuntimeException {
    public NaoEhComissionadoException(String message) {
        super("Empregado nao eh comissionado.");
    }
}
