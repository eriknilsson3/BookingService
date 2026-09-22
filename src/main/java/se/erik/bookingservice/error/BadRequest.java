package se.erik.bookingservice.error;

public class BadRequest extends RuntimeException{
    public BadRequest(String message){
        super(message);
    }
}
