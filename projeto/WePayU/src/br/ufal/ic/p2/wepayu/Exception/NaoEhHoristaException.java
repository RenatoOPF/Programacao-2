package br.ufal.ic.p2.wepayu.Exception;

public class NaoEhHoristaException extends RuntimeException {
    public NaoEhHoristaException(String message) {
        super("Empregado nao eh horista.");
    }
}
