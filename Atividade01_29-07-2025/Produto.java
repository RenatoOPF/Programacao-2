public class Produto {
    private String nome;
    private double preco;
    private int quantidade;

    public Produto(String nome, double preco, int quantidade) {
        this.nome = nome;
        this.preco = preco;
        this.quantidade = quantidade;
    }

    public double valorTotalEstoque() {
        return preco * quantidade;
    }

    public void adicionarProdutos(int quantidade) {
        this.quantidade += quantidade;
    }

    public void removerProdutos(int quantidade) {
        if (quantidade > this.quantidade) {
            System.out.println("Erro: não é possível remover mais do que há em estoque.");
            System.exit(1);
        }
        this.quantidade -= quantidade;
    }

    public String toString() {
        return String.format("%s, Preço: R$ %.2f, Quantidade: %d, Total em estoque: R$ %.2f", nome, preco, quantidade, valorTotalEstoque());
    }
}
