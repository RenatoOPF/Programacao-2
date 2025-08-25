import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Reservation {
    private int roomNumber;
    private LocalDate checkIn;
    private LocalDate checkOut;

    public LocalDate now = LocalDate.now();

    public Reservation(int roomNumber, LocalDate checkIn, LocalDate checkOut)
    {
        if (checkIn.isBefore(now) || checkOut.isBefore(now)) 
        {
            throw new IllegalArgumentException("Error in reservation: Reservation dates must be future dates");
        }

        if(checkOut.isBefore(checkIn)) 
        {
            throw new IllegalArgumentException("Error in reservation: Check-out date must be after check-in date");
        }

        this.roomNumber = roomNumber;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public int duration() 
    {
        return (int) (checkOut.toEpochDay() - checkIn.toEpochDay());
    }

    public int getRoomNumber()
    {
        return roomNumber;
    }

    public LocalDate getCheckIn()
    {
        return checkIn;
    }

    public LocalDate getCheckOut()
    {
        return checkOut;
    }

    public void updateDates(LocalDate checkIn, LocalDate checkOut) 
    {
        if (checkIn.isBefore(now) || checkOut.isBefore(now)) 
        {
            throw new IllegalArgumentException("Error in reservation: Reservation dates for update must be future dates");
        }

        if(checkOut.isBefore(checkIn)) 
        {
            throw new IllegalArgumentException("Error in reservation: Check-out date must be after check-in date");
        }

        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public String toString ()
    {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return "Room " + roomNumber + ", check-in: " + checkIn.format(formato) + ", check-out: " + checkOut.format(formato) + ", " + duration() + " nights";
    }

}
