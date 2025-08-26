import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try {
            System.out.println("Enter account data");

            System.out.print("Number: ");
            int number = sc.nextInt();
            sc.nextLine();

            System.out.print("Holder: ");
            String holder = sc.nextLine();

            System.out.print("Initial balance: ");
            Double balance = sc.nextDouble();

            System.out.print("Withdraw limit: ");
            Double withdrawLimit = sc.nextDouble();

            Account Account = new Account(number, holder, balance, withdrawLimit);

            System.out.print("Enter amount for withdraw: ");
            Double withdrawAmount = sc.nextDouble();

            Account.withdraw(withdrawAmount);   

            System.out.println("New balance: " + Account.getBalance());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        sc.close();
    }
}
