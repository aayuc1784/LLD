public class VendingMachineApplication {
    public static void main(String[] args){
        VendingMachine vendingMachine = new VendingMachine();
        vendingMachine.insertCoin(10);
        vendingMachine.selectItem("Chips");
        vendingMachine.insertCoin(10);
        vendingMachine.dispenseItem();
    }
}

class VendingMachine{
    private VendingState vendingState;
    private double amount;
    private String selectedItem;

    public VendingMachine(){
        vendingState = new IdleState();
    }

    public void setVendingState(VendingState vendingState){
        this.vendingState=vendingState;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
    public double getAmount(){
        return amount;
    }

    public void setSelectedItem(String selectedItem){
        this.selectedItem=selectedItem;
    }
    public String getSelectedItem(){
        return selectedItem;
    }

    public void insertCoin(double amount){
        vendingState.insertCoin(this, amount);
    }
    public void selectItem(String selectedItem){
        vendingState.selectItem(this, selectedItem);
    }
    public void dispenseItem(){
        vendingState.dispenseItem(this);
    }
    public void reset(){
        amount=0;
        vendingState=new IdleState();
        selectedItem="";
    }
}

interface VendingState{
    void insertCoin(VendingMachine vendingMachine, double amount);
    void selectItem(VendingMachine vendingMachine, String selectedItem);
    void dispenseItem(VendingMachine vendingMachine);
}

class IdleState implements VendingState{

    @Override
    public void insertCoin(VendingMachine vendingMachine, double amount) {
        System.out.println("Please select an item before inserting coins.");
    }

    @Override
    public void selectItem(VendingMachine vendingMachine, String selectedItem) {
        System.out.println("Item selected: " + selectedItem);
        vendingMachine.setSelectedItem(selectedItem);
        vendingMachine.setVendingState(new ItemState());
    }

    @Override
    public void dispenseItem(VendingMachine vendingMachine) {
        System.out.println("No item selected. Nothing to dispense.");
    }
}

class ItemState implements VendingState{

    @Override
    public void insertCoin(VendingMachine vendingMachine, double amount) {
        System.out.println("Inserted $" + amount + " for item: " + vendingMachine.getSelectedItem());
        vendingMachine.setAmount(amount);
        vendingMachine.setVendingState(new CoinState());
    }

    @Override
    public void selectItem(VendingMachine vendingMachine, String selectedItem) {
        System.out.println("Item is already selected " + vendingMachine.getSelectedItem());
    }

    @Override
    public void dispenseItem(VendingMachine vendingMachine) {
        System.out.println("No item selected. Nothing to dispense.");
    }
}

class CoinState implements VendingState{

    @Override
    public void insertCoin(VendingMachine vendingMachine, double amount) {
        System.out.println("Machine has already coin" + vendingMachine.getAmount());
    }

    @Override
    public void selectItem(VendingMachine vendingMachine, String selectedItem) {
        System.out.println("Item is already selected " + vendingMachine.getSelectedItem());
    }

    @Override
    public void dispenseItem(VendingMachine vendingMachine) {
        System.out.println("Dispensing item: " + vendingMachine.getSelectedItem());
        vendingMachine.setVendingState(new DispenseState());
        System.out.println("Item dispensed successfully.");
        vendingMachine.reset();
    }
}

class DispenseState implements VendingState{

    @Override
    public void insertCoin(VendingMachine vendingMachine, double amount) {
        System.out.println("Dispensing items " + vendingMachine.getSelectedItem());
    }

    @Override
    public void selectItem(VendingMachine vendingMachine, String selectedItem) {
        System.out.println("Dispensing items " + vendingMachine.getSelectedItem());
    }

    @Override
    public void dispenseItem(VendingMachine vendingMachine) {
        System.out.println("Dispensing items " + vendingMachine.getSelectedItem());
    }
}

class SoldOutState implements VendingState{

    @Override
    public void insertCoin(VendingMachine vendingMachine, double amount) {
        System.out.println("Nothing to Buy");
    }

    @Override
    public void selectItem(VendingMachine vendingMachine, String selectedItem) {
        System.out.println("Nothing to Buy");
    }

    @Override
    public void dispenseItem(VendingMachine vendingMachine) {
        System.out.println("Nothing to Buy");
    }
}
