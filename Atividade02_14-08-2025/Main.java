import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        Scanner sc = new Scanner(System.in);

        List<TaxPayer> list = new ArrayList<>();

        System.out.print("Enter the number of tax payers: ");
        int n = sc.nextInt();

        for (int i = 1; i <= n; i++) {
            System.out.println("Tax payer #" + i + " data:");
            System.out.print("Individual or company (i/c)? ");
            char tipo = sc.next().charAt(0);
            System.out.print("Name: ");
            sc.nextLine();
            String name = sc.nextLine();
            System.out.print("Anual income: ");
            double renda = sc.nextDouble();

            if (tipo == 'i') {
                System.out.print("Health expenditures: ");
                double saude = sc.nextDouble();
                list.add(new Individual(name, renda, saude));
            } else {
                System.out.print("Number of employees: ");
                int funcionarios = sc.nextInt();
                list.add(new Company(name, renda, funcionarios));
            }
        }

        System.out.println();
        System.out.println("TAXES PAID:");
        double soma = 0.0;
        for (TaxPayer c : list) {
            double imposto = c.imposto();
            System.out.println(c.getName() + ": $ " + String.format("%.2f", imposto));
            soma += imposto;
        }

        System.out.println();
        System.out.println("TOTAL TAXES: $ " + String.format("%.2f", soma));

        sc.close();
    }
}
