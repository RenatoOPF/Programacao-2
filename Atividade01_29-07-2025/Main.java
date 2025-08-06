import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Nome do produto: ");
        String nome = sc.nextLine();
        System.out.print("Preço do produto: ");
        double preco = sc.nextDouble();
        System.out.print("Quantidade no estoque: ");
        int quantidade = sc.nextInt();

        Produto produto = new Produto(nome, preco, quantidade);

        System.out.println("\nDados do produto: " + produto);

        System.out.print("\nDigite o número de produtos a adicionar no estoque: ");
        int entrada = sc.nextInt();
        produto.adicionarProdutos(entrada);
        System.out.println("Atualização: " + produto);

        System.out.print("\nDigite o número de produtos a remover do estoque: ");
        int saida = sc.nextInt();
        produto.removerProdutos(saida);
        System.out.println("Atualização final: " + produto);

        sc.close();
    }
}
