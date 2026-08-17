import java.util.Random;

public class PaymentGatewayApplication {
    public static void main(String [] args){
        PaymentRequest paymentRequest = new PaymentRequest("Alice", "Bob", 1000.0, "INR");
        boolean status = PaymentController.getPaymentController().handlePayment(paymentRequest, GatewayType.PAYTM);
        System.out.println("Payment " + status);

        status = PaymentController.getPaymentController().handlePayment(paymentRequest, GatewayType.RAZORPAY);
        System.out.println("Payment " + status);
    }
}

class PaymentRequest {
    public String sender;
    public String receiver;
    public double amount;
    public String currency;

    public PaymentRequest(String sender, String receiver, double amount, String currency){
        this.sender = sender;
        this.currency=currency;
        this.receiver=receiver;
        this.amount=amount;
    }
}

class PaymentController{
    private static final PaymentController paymentController = new PaymentController();

    private PaymentController(){}

    public static PaymentController getPaymentController(){
        return paymentController;
    }

    public boolean handlePayment(PaymentRequest paymentRequest, GatewayType gateway){
        PaymentGateway paymentGateway = GatewayFactory.getGatewayFactory().getGateway(gateway);
        PaymentService.getPaymentService().setGateway(paymentGateway);
        return PaymentService.getPaymentService().processPayment(paymentRequest);
    }
}

class GatewayFactory{
    private static final GatewayFactory gatewayFactory = new GatewayFactory();

    private GatewayFactory(){}

    public static GatewayFactory getGatewayFactory(){
        return gatewayFactory;
    }

    public PaymentGateway getGateway(GatewayType gatewayType){
        if(gatewayType == GatewayType.PAYTM){
            PaymentGateway paymentGateway = new PaytmGateway();
            return new PaymentGatewayProxy(paymentGateway,3);
        } else{
            PaymentGateway paymentGateway = new RazorpayGateway();
            return new PaymentGatewayProxy(paymentGateway,2);
        }
    }
}

class PaymentGatewayProxy extends PaymentGateway{
    private final PaymentGateway paymentGateway;
    private final int retry;

    public PaymentGatewayProxy(PaymentGateway paymentGateway, int retry){
        this.paymentGateway = paymentGateway;
        this.retry = retry;
    }

    @Override
    public boolean processPayment(PaymentRequest paymentRequest){
        boolean result = false;
        for (int attempt = 0; attempt < retry; ++attempt) {
            if (attempt > 0) {
                System.out.println("[Proxy] Retrying payment (attempt " + (attempt+1)
                        + ") for " + paymentRequest.sender + ".");
            }
            result = paymentGateway.processPayment(paymentRequest);
            if (result) break;
        }
        if (!result) {
            System.out.println("[Proxy] Payment failed after " + retry
                    + " attempts for " + paymentRequest.sender + ".");
        }
        return result;
    }

    @Override
    protected boolean validatePayment(PaymentRequest paymentRequest) {
        return paymentGateway.validatePayment(paymentRequest);
    }

    @Override
    protected boolean initiatePayment(PaymentRequest paymentRequest) {
        return paymentGateway.initiatePayment(paymentRequest);
    }

    @Override
    protected boolean confirmPayment(PaymentRequest paymentRequest) {
        return paymentGateway.confirmPayment(paymentRequest);
    }
}

interface Bank{
    boolean processPayment(double amount);
}

class PaytmBank implements Bank {
    public PaytmBank(){}
    private final Random rand = new Random();

    @Override
    public boolean processPayment(double amount) {
        int random = rand.nextInt(100);
        return random < 80;
    }
}
class RazorpayBank implements Bank{
    public RazorpayBank(){}
    private final Random rand = new Random();

    @Override
    public boolean processPayment(double amount) {
        int random = rand.nextInt(100);
        return random < 90;
    }
}
class PaytmGateway extends PaymentGateway{
    public PaytmGateway(){
        this.bank = new PaytmBank();
    }
    @Override
    protected boolean validatePayment(PaymentRequest paymentRequest) {
        System.out.println("[Paytm] Validating payment for " + paymentRequest.sender + ".");
        return !(paymentRequest.amount <= 0);
    }

    @Override
    protected boolean initiatePayment(PaymentRequest paymentRequest) {
        System.out.println("[Paytm] Initiating payment of " + paymentRequest.amount
                + " " + paymentRequest.currency + " for " + paymentRequest.sender + ".");
        return bank.processPayment(paymentRequest.amount);
    }

    @Override
    protected boolean confirmPayment(PaymentRequest paymentRequest) {
        System.out.println("[Paytm] Confirming payment for " + paymentRequest.sender + ".");
        return true;
    }
}

class RazorpayGateway extends PaymentGateway{
    public RazorpayGateway(){
        this.bank = new RazorpayBank();
    }
    @Override
    protected boolean validatePayment(PaymentRequest paymentRequest) {
        System.out.println("[Razorpay] Validating payment for " + paymentRequest.sender + ".");
        return !(paymentRequest.amount <= 0);
    }

    @Override
    protected boolean initiatePayment(PaymentRequest paymentRequest) {
        System.out.println("[Razorpay] Initiating payment of " + paymentRequest.amount
                + " " + paymentRequest.currency + " for " + paymentRequest.sender + ".");
        return bank.processPayment(paymentRequest.amount);
    }

    @Override
    protected boolean confirmPayment(PaymentRequest paymentRequest) {
        System.out.println("[Razorpay] Confirming payment for " + paymentRequest.sender + ".");
        return true;
    }
}

class PaymentService{
    private static final PaymentService paymentService = new PaymentService();
    private PaymentGateway gateway;

    private PaymentService(){
        this.gateway=null;
    }

    public static PaymentService getPaymentService(){
        return paymentService;
    }

    public void setGateway(PaymentGateway gateway){
        this.gateway = gateway;
    }

    public boolean processPayment(PaymentRequest paymentRequest) {
        if(gateway == null){
            System.out.println("Gateway can not be null");
            return false;
        }
        return gateway.processPayment(paymentRequest);
    }
}

abstract class PaymentGateway{
    protected Bank bank;
    public PaymentGateway (){
        this.bank = null;
    }
    boolean processPayment(PaymentRequest paymentRequest){
        if(!validatePayment(paymentRequest)){
            System.out.println("[PaymentGateway] Validation failed for " + paymentRequest.sender + ".");
            return false;
        }

        if(!initiatePayment(paymentRequest)){
            System.out.println("[PaymentGateway] Initiation failed for " + paymentRequest.sender + ".");
            return false;
        }

        if(!confirmPayment(paymentRequest)){
            System.out.println("[PaymentGateway] Confirmation failed for " + paymentRequest.sender + ".");
            return false;
        }

        return true;
    }

    protected abstract boolean validatePayment(PaymentRequest paymentRequest);
    protected abstract boolean initiatePayment(PaymentRequest paymentRequest);
    protected abstract boolean confirmPayment(PaymentRequest paymentRequest);
}


enum GatewayType {
    PAYTM,
    RAZORPAY
}
