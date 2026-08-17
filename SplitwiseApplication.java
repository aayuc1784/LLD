import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplitwiseApplication {
    public static void main(String[] args){

    }
}

class SplitWise{

}

class User implements Observer{
    private static int nextUser = 0;
    public String userId;
    public String name;
    public String email;
    public Map<String, Double>balance;

    public User(String name, String email) {
        this.email = email;
        this.name = name;
        this.balance = new HashMap<>();
        this.userId = "user" + (++nextUser);
    }
    @Override
    public void update() {
        System.out.println("Notification send for User " + userId + " " + message);
    }

    public void updateBalance(String otherUserId, double amount){
        balance.put(otherUserId,balance.getOrDefault(otherUserId,0.0)+amount);

        if(Math.abs(balance.get(otherUserId)) < 0.01){
            balance.remove(otherUserId);
        }
    }

    public double owedAmount() {
        double total = 0;
        for (Map.Entry<String, Double> balance : balance.entrySet()) {
            if (balance.getValue() < 0) {
                total += Math.abs(balance.getValue());
            }
        }
        return total;
    }

    public double oweingAmount(){
        double total = 0;
        for (Map.Entry<String, Double> balance : balance.entrySet()) {
            if (balance.getValue() > 0) {
                total += Math.abs(balance.getValue());
            }
        }
        return total;
    }
}

class Group{
    private static int nextGroupId=0;
    public String groupId;
    public String groupName;
    public List<User>users;
    public Map<String, Expense>expenses;
    public Map<String,Map<String,Double>>balance;

    public Group(String groupName){
        this.groupId="group" + (++nextGroupId);
        this.groupName=groupName;
        this.users = new ArrayList<>();
        this.expenses = new HashMap<>();
        this.balance = new HashMap<>();
    }

    private User findUserByUserId(String userId){
        for(User user : users){
            if(user.userId.equals(userId))return user;
        }
        return null;
    }

    void addMember(User user){
        if(isMember(user.userId)){
            System.out.println("User is already a member of group");
            return;
        }
        users.add(user);
        balance.put(user.userId,new HashMap<>());
        System.out.println(user.name + " added to group " + groupName);
    }

    boolean removeMember(User user){
        if(!isRemovable(user)){
            System.out.println("User can not be removed");
            return false;
        }

        users.removeIf(user1 -> user.userId.equals(user1.userId));
        balance.remove(user.userId);
        for(Map.Entry<String,Map<String,Double>> bal: balance.entrySet()){
            bal.getValue().remove(user.userId);
        }

        return true;
    }

    private boolean isRemovable(User user){
        if(!isMember(user.userId)){
            System.out.println("User is not a part of group");
            return false;
        }
        for(Map.Entry<String, Double> bal : balance.get(user.userId).entrySet()){
            if(Math.abs(bal.getValue()) > 0.01)return false;
        }
        return true;
    }

    private boolean isMember(String userId){
        return balance.containsKey(userId);
    }

    public void notifyUser(String message){
        for(Observer user : users){
            user.update();
        }
    }

    public void simplyFyPayments(){};

    public void settlePayment(){};

    public void addExpense(){};

    private void updateBalance(){};

}

class Expense{
    private static int nextExpenseId=0;
    public String expenseId;
    public String desc;
    List<Split>splitList;
    public String groupId;
    public String userId;
    public double amount;

    // group expense
    public Expense(String desc,String userId, double amount, String groupId, List<Split>splits){
        this.desc=desc;
        this.userId=userId;
        this.groupId=groupId;
        this.splitList=splits;
        this.amount=amount;
        this.expenseId="expense" + (++nextExpenseId);
    }

    // individual expense
    public Expense(String desc, String userId, double amount, List<Split>splits){
        this(desc,userId,amount,"",splits);
    }

}

interface Observer{
    void update();
}

enum SplitType{
    EXACT,
    PERCENTAGE,
    EQUAL
}

class Split{
    public String userId;
    public double amount;

    public Split(String userId, double amount) {
        this.amount=amount;
        this.userId=userId;
    }
}

interface splitStrategy{
    List<Split> calculateSplit(double totalAmount, List<String>users, List<Double>valueAmount);
}

class ExactSplitStrategy implements splitStrategy{
    @Override
    public List<Split> calculateSplit(double totalAmount, List<String>users, List<Double> valueAmount) {
        List<Split>result = new ArrayList<>();
        for(int i=0;i<users.size();i++){
            Split split = new Split(users.get(i), valueAmount.get(i));
            result.add(split);
        }
        return result;
    }
}

class EqualSplitStrategy implements splitStrategy{
    @Override
    public List<Split> calculateSplit(double totalAmount, List<String> users, List<Double> valueAmount) {
        List<Split>result = new ArrayList<>();
        double equalAmount = totalAmount/users.size();
        for (String user : users) {
            Split split = new Split(user, equalAmount);
            result.add(split);
        }
        return result;
    }
}

class PercentageSplitStrategy implements splitStrategy{
    @Override
    public List<Split> calculateSplit(double totalAmount, List<String> users, List<Double> valueAmount) {
        List<Split> result = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            double amount = (totalAmount * valueAmount.get(i)) / 100.0;
            result.add(new Split(users.get(i), amount));
        }
        return result;
    }
}

class SplitFactory{
    public static splitStrategy getStrategy(SplitType splitType){
        return switch (splitType) {
            case EXACT -> new ExactSplitStrategy();
            case PERCENTAGE -> new PercentageSplitStrategy();
            default -> new EqualSplitStrategy();
        };
    }
}


