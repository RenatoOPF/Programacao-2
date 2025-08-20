public class Individual extends TaxPayer {
    private Double gastosSaude;

    public Individual(String nome, Double rendaAnual, Double gastosSaude) {
        super(nome, rendaAnual);
        this.gastosSaude = gastosSaude;
    }

    @Override
    public Double imposto() {
        double impostoBase;
        if (getRendaAnual() < 20000.0) {
            impostoBase = getRendaAnual() * 0.15;
        } else {
            impostoBase = getRendaAnual() * 0.25;
        }
        impostoBase -= gastosSaude * 0.5;
        if (impostoBase < 0.0) {
            impostoBase = 0.0;
        }
        return impostoBase;
    }
}
