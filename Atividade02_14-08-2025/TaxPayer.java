public abstract class TaxPayer {
    private String nome;
    private Double rendaAnual;

    public TaxPayer(String nome, Double rendaAnual) {
        this.nome = nome;
        this.rendaAnual = rendaAnual;
    }

    public String getName() {
        return nome;
    }

    public Double getRendaAnual() {
        return rendaAnual;
    }

    public abstract Double imposto();
}
