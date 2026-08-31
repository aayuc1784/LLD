import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class AmazonLocker {
    public static void main(String[] args){
        Compartment[] compartment = new Compartment[5];
        compartment[0] = new Compartment(CompartmentSize.SMALL);
        compartment[1] = new Compartment(CompartmentSize.SMALL);
        compartment[2] = new Compartment(CompartmentSize.SMALL);
        compartment[3] = new Compartment(CompartmentSize.MEDIUM);
        compartment[4] = new Compartment(CompartmentSize.LARGE);

        Locker locker = new Locker(compartment);

        String token = locker.depositPackage(CompartmentSize.MEDIUM);
        locker.collectPackage(token);
    }
}

class Locker {
    private final Map<String, AccessToken> code;
    private final Compartment [] compartments;

    public Locker(Compartment[] compartments) {
        this.code = new HashMap<>();
        this.compartments = compartments;
    }


    public String depositPackage(CompartmentSize compartmentSize){
        for(Compartment compartment : compartments){
            if(compartment.getCompartmentSize() == compartmentSize && !compartment.isOccupied()){
                String token = generateAccessToken();
                AccessToken accessToken = new AccessToken(token, Instant.now().plus(7, ChronoUnit.DAYS),compartment);
                compartment.reserveLocker();
                code.put(token,accessToken);
                return token;
            }
        }
        throw new IllegalArgumentException("No compartment found");
    }

    public void collectPackage(String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Invalid Access Code");
        }

        AccessToken accessToken = code.get(token);
        if (accessToken == null) {
            throw new IllegalArgumentException("Invalid Access Code");
        }
        if (accessToken.isExpired()) {
            throw new RuntimeException("Expired Access Code");
        }
        accessToken.getCompartment().unreservedLocker();
        code.remove(accessToken.getToken());
        System.out.println("Collected Package");
    }

    private String generateAccessToken(){
        Random random = new Random();
        int number = random.nextInt(1000000);
        return String.format("%06d", number);
    }
}

class Compartment{
    private final CompartmentSize compartmentSize;
    private boolean occupied = false;

    public Compartment(CompartmentSize compartmentSize){
        this.compartmentSize=compartmentSize;
    }

    public CompartmentSize getCompartmentSize() {
        return compartmentSize;
    }

    public void reserveLocker(){
        this.occupied=true;
    }

    public boolean isOccupied(){
        return occupied;
    }

    public void unreservedLocker(){
        this.occupied=false;
    }
}

class AccessToken{
    private final String token;
    private final Instant expirationTime;
    private final Compartment compartment;

    public AccessToken(String token, Instant expirationTime, Compartment compartment){
        this.compartment=compartment;
        this.expirationTime=expirationTime;
        this.token=token;
    }

    public Compartment getCompartment() {
        return compartment;
    }

    public String getToken() {
        return token;
    }

    public boolean isExpired() {
        return !Instant.now().isBefore(expirationTime);
    }
}

enum CompartmentSize {
    SMALL,
    MEDIUM,
    LARGE
}