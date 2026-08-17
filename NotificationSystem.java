import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationSystem {
    public static void main (String [] args){
        NotificationService notificationService = NotificationService.getInstance();
        String content = "This is Notification service LLD";

        NotificationEngine EmailService = new NotificationEngine(new EmailStrategy("test@gmail.com"));
        NotificationEngine SmsService = new NotificationEngine(new SMSStrategy("12345678"));

        notificationService.addObserver(EmailService);
        notificationService.addObserver(SmsService);

        Notification notification = new simpleNotification(content);

        notificationService.sendNotification(notification);

        notification = new TimeStampDecorator(notification);

        notificationService.sendNotification(notification);

    }
}

interface Notification{
    String getContent();
}

class simpleNotification implements Notification{
    private final String message;
    public simpleNotification(String message){
        this.message=message;
    }

    @Override
    public String getContent() {
        return message;
    }
}

// decorator pattern
abstract class NotificationDecorator implements Notification{
    protected Notification notification;

    public NotificationDecorator(Notification notification){
        this.notification = notification;
    }
}

class TimeStampDecorator extends NotificationDecorator {

    public TimeStampDecorator(Notification notification) {
        super(notification);
    }

    @Override
    public String getContent() {
        return "[" + LocalDateTime.now() + "]" + notification.getContent();
    }
}

class SignatureDecorator extends NotificationDecorator {
    private final String signature;

    public SignatureDecorator(Notification notification, String sig) {
        super(notification);
        this.signature = sig;
    }

    @Override
    public String getContent() {
        return notification.getContent() + "\n-- " + signature + "\n\n";
    }
}

// observer pattern
interface NotificationObserver{
    void update(Notification notification);
}

interface NotificationObservable {
    void addObserver(NotificationObserver observer);
    void removeObserver(NotificationObserver observer);
    void notifyObservers(Notification notification);
}

class NotificationPublisher implements NotificationObservable{
    private final List<NotificationObserver>observers = new ArrayList<>();

    @Override
    public void addObserver(NotificationObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Notification notification) {
        for(NotificationObserver observer : observers){
            observer.update(notification);
        }
    }
}

interface NotificationStrategy{
    void sendNotification(String message);
}

class EmailStrategy implements NotificationStrategy{
    private final String emailId;

    public EmailStrategy(String emailId){
        this.emailId=emailId;
    }
    @Override
    public void sendNotification(String message) {
        System.out.println("Sending email Notification to: " + emailId + "\n" + message);
    }
}

class SMSStrategy implements NotificationStrategy {
    private final String mobileNumber;

    public SMSStrategy(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void sendNotification(String content) {
        System.out.println("Sending SMS Notification to: " + mobileNumber + "\n" + content);
    }
}

class PopUpStrategy implements NotificationStrategy {

    public void sendNotification(String content) {
        System.out.println("Sending Popup Notification: \n" + content);
    }
}

class NotificationEngine implements NotificationObserver{

    private final NotificationStrategy strategy;

    public NotificationEngine(NotificationStrategy strategy){
        this.strategy=strategy;
    }

    @Override
    public void update(Notification notification) {
        strategy.sendNotification(notification.getContent());
    }
}

class NotificationService{
    private final NotificationPublisher notificationPublisher;
    private static NotificationService instance;

    private NotificationService(){
        notificationPublisher = new NotificationPublisher();
    }

    public static synchronized NotificationService getInstance(){
        if(instance==null){
            instance = new NotificationService();
        }
        return instance;
    }

    public void addObserver(NotificationObserver notificationObserver){
        notificationPublisher.addObserver(notificationObserver);
    }
    public void removeObserver(NotificationObserver notificationObserver){
        notificationPublisher.removeObserver(notificationObserver);
    }
    public void sendNotification(Notification notification){
        notificationPublisher.notifyObservers(notification);
    }

}