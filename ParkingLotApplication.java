import java.util.*;

public class ParkingLotApplication {
    public static void main(String[] args){
        ParkingLot parkingLot = ParkingLot.getInstance();
        ParkingSpot spot1 = new ParkingSpot("1", ParkingSpotSize.SMALL);
        ParkingSpot spot2 = new ParkingSpot("2", ParkingSpotSize.LARGE);
        ParkingSpot spot3 = new ParkingSpot("3", ParkingSpotSize.MEDIUM);
        ParkingSpot spot4 = new ParkingSpot("4", ParkingSpotSize.SMALL);

        parkingLot.addSpot(spot1);
        parkingLot.addSpot(spot2);
        parkingLot.addSpot(spot3);
        parkingLot.addSpot(spot4);

        Vehicle vehicle1 =  new Vehicle("ID1", VehicleSize.BIKE);
        Vehicle vehicle2 =  new Vehicle("ID2", VehicleSize.CAR);
        Vehicle vehicle3 =  new Vehicle("ID3", VehicleSize.TRUCK);

        Optional<Ticket> ticket1 = parkingLot.enter(vehicle1);
        Optional<Ticket> ticket2 = parkingLot.enter(vehicle2);
        Optional<Ticket> ticket3 = parkingLot.enter(vehicle3);
        Optional<Double>fee1 = parkingLot.exit(vehicle1);
        Optional<Double>fee2 = parkingLot.exit(vehicle3);

        System.out.println(ticket1.get().getTicketId());
        System.out.println(ticket2.get().getTicketId());
        System.out.println(ticket3.get().getTicketId());
        System.out.println(fee1.get());


    }
}

class ParkingLot {
    private final static ParkingLot instance = new ParkingLot();
    private final List<ParkingSpot> spots = new ArrayList<>();
    private final Map<String, Ticket> tickets = new HashMap<>();
    private FeeStrategy feeStrategy;
    private SpotStrategy spotStrategy;

    private ParkingLot(){
        this.feeStrategy = new FlatFeeStrategy();
        this.spotStrategy = new NearestStrategy();
    }

    public static ParkingLot getInstance() {
        return instance;
    }

    public void addSpot(ParkingSpot parkingSpot){
        spots.add(parkingSpot);
    }

    public Optional<Ticket> enter (Vehicle vehicle){
        Optional <ParkingSpot> spot = spotStrategy.findSpot(spots, vehicle);
        if(spot.isPresent()){
            ParkingSpot parkingSpot = spot.get();
            parkingSpot.parkVehicle();
            Ticket ticket = new Ticket(vehicle,parkingSpot);
            tickets.put(vehicle.getVehicleId(),ticket);
            System.out.printf("%s parked at %s. Ticket: %s\n", vehicle.getVehicleId(), parkingSpot.getSpotId(), ticket.getTicketId());
            return Optional.of(ticket);
        }

        return Optional.empty();
    }

    public Optional<Double> exit (Vehicle vehicle){
        Ticket ticket = tickets.get(vehicle.getVehicleId());
        if(ticket == null){
            System.out.println("Ticket not found");
            return Optional.empty();
        }
        tickets.remove(vehicle.getVehicleId());
        ticket.markExit();
        Double amount = feeStrategy.calculate(ticket);
        return Optional.of(amount);
    }


    public void setFeeStrategy(FeeStrategy feeStrategy){
        this.feeStrategy = feeStrategy;
    }

    public void setSpotStrategy(SpotStrategy spotStrategy) {
        this.spotStrategy = spotStrategy;
    }
}

enum VehicleSize {
    CAR, BIKE, TRUCK
}

enum ParkingSpotSize {
    SMALL, MEDIUM, LARGE
}

class Vehicle {

    private final String vehicleId;
    private final VehicleSize vehicleSize;

    public Vehicle(String vehicleId, VehicleSize vehicleSize){
        this.vehicleId = vehicleId;
        this.vehicleSize = vehicleSize;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public VehicleSize getVehicleSize() {
        return vehicleSize;
    }
}

class ParkingSpot {

    private final String spotId;
    private final ParkingSpotSize parkingSpotSize;
    private boolean isOccupied;

    public ParkingSpot(String spotId, ParkingSpotSize parkingSpotSize) {
        this.spotId = spotId;
        this.parkingSpotSize = parkingSpotSize;
        isOccupied = false;
    }

    public synchronized boolean isOccupied (){
        return isOccupied;
    }

    public synchronized void parkVehicle() {
        isOccupied = true;
    }

    public void unparkVehicle() {
        isOccupied = false;
    }

    public ParkingSpotSize getParkingSpotSize() {
        return parkingSpotSize;
    }

    public String getSpotId() {
        return spotId;
    }
}

class Ticket {
    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSpot parkingSpot;
    private final Long entryAt;
    private Long exitAt;
    private Random random = new Random();

    public Ticket (Vehicle vehicle, ParkingSpot parkingSpot) {
        this.ticketId = String.valueOf(10000 + random.nextInt(90000));
        this.vehicle = vehicle;
        this.parkingSpot = parkingSpot;
        this.entryAt = System.currentTimeMillis();
    }

    public void markExit() {
        this.exitAt = System.currentTimeMillis();
        parkingSpot.unparkVehicle();
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Long getEntryAt() {
        return entryAt;
    }

    public Long getExitAt() {
        return exitAt;
    }

    public String getTicketId() {
        return ticketId;
    }
}

interface SpotStrategy {
    Optional<ParkingSpot> findSpot(List<ParkingSpot> spots, Vehicle vehicle);
}

class NearestStrategy implements SpotStrategy{

    private static final Map<ParkingSpotSize, VehicleSize> spotSizeVehicleSizeMap = Map.of(
            ParkingSpotSize.LARGE, VehicleSize.TRUCK,
            ParkingSpotSize.SMALL, VehicleSize.BIKE,
            ParkingSpotSize.MEDIUM, VehicleSize.CAR
    );

    @Override
    public Optional<ParkingSpot> findSpot(List<ParkingSpot> spots, Vehicle vehicle) {
        for(ParkingSpot spot : spots){
            if(!spot.isOccupied() && spotSizeVehicleSizeMap.get(spot.getParkingSpotSize()) == vehicle.getVehicleSize()){
                return Optional.of(spot);
            }
        }
        System.out.println("No spot found to park: " + vehicle.getVehicleId());
        return Optional.empty();
    }
}


interface FeeStrategy {
    double calculate(Ticket ticket);
}

class FlatFeeStrategy implements FeeStrategy {

    @Override
    public double calculate(Ticket ticket) {
        return (ticket.getExitAt() - ticket.getEntryAt()) * 0.001;
    }
}

class HourlyRateStrategy implements FeeStrategy {
    private static final Map<VehicleSize, Double> HOURLY_RATES = Map.of(
            VehicleSize.BIKE, 10.0,
            VehicleSize.CAR, 20.0,
            VehicleSize.TRUCK, 30.0
    );

    @Override
    public double calculate(Ticket ticket) {
        return (ticket.getExitAt() - ticket.getEntryAt()) * HOURLY_RATES.get(ticket.getVehicle().getVehicleSize());
    }
}
