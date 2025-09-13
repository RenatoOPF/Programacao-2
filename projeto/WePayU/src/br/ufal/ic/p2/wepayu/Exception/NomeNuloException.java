package br.ufal.ic.p2.wepayu.Exception;

public class NomeNuloException extends RuntimeException {
    public NomeNuloException(String message) {
        super("Nome nao pode ser nulo.");
    }
}
