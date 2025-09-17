package br.ufal.ic.p2.wepayu.Exception;

public class ComissaoNaoNumericaException extends RuntimeException {
    public ComissaoNaoNumericaException(String message) {
        super("Comissao deve ser numerica.");
    }
}
