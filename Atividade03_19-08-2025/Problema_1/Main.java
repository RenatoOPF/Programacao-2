import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            System.out.print("Room number: ");
            int romm = sc.nextInt();
            sc.nextLine();
    
            System.out.print("Check-in date (dd/MM/yyyy): ");
            LocalDate checkIn = LocalDate.parse(sc.nextLine(), formato);
            System.out.print("Check-out date (dd/MM/yyyy): ");
            LocalDate checkOut = LocalDate.parse(sc.nextLine(), formato);

            Reservation reservation = new Reservation(romm, checkIn, checkOut);
            System.out.println("Reservation: " + reservation);
    
            System.out.println("Enter data to update the reservation:");
            System.out.print("Check-in date (dd/MM/yyyy): ");
            LocalDate newCheckIn = LocalDate.parse(sc.nextLine(), formato);
            System.out.print("Check-out date (dd/MM/yyyy): ");
            LocalDate newCheckOut = LocalDate.parse(sc.nextLine(), formato);

            reservation.updateDates(newCheckIn, newCheckOut);
            System.out.println("Reservation: " + reservation);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        sc.close();
    }
}

