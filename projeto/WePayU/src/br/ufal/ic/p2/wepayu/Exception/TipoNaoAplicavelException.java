package br.ufal.ic.p2.wepayu.Exception;

public class TipoNaoAplicavelException extends RuntimeException {
    public TipoNaoAplicavelException(String message) { super("Tipo nao aplicavel."); }
}
