package br.ufal.ic.p2.wepayu.Exception;

public class TipoInvalidoException extends RuntimeException {
    public TipoInvalidoException(String message) {
        super("Tipo invalido.");
    }
}
