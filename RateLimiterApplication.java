import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class RateLimiterApplication {
    public static void main (String[] args){
        RateLimiterService rateLimiterService = new RateLimiterService();
        User user = new User("test", UserType.FREE);

        boolean allowRequest = rateLimiterService.allowRequest(user);
        System.out.println("Request for user " + user.getUserId() + (allowRequest ? " Allowed" : " Blocked"));
    }
}

class RateLimiterService{
    private final Map<UserType, RateLimiter> rateLimiterMap = new HashMap<>();

    public RateLimiterService(){
        rateLimiterMap.put(UserType.FREE, RateLimiterFactory.createRateLimiter(new RateLimitConfig(10,1), RateLimiterType.TOKEN_BUCKET));
        rateLimiterMap.put(UserType.PREMIUM, RateLimiterFactory.createRateLimiter(new RateLimitConfig(20,1), RateLimiterType.FIXED_WINDOW));
    };

    public boolean allowRequest(User user){
        RateLimiter limiter = rateLimiterMap.get(user.getUserType());
        if(limiter==null){
            throw new IllegalArgumentException("No Limiter Configured for UserType");
        }
        return limiter.allowRequest(user.getUserId());
    }

}

enum UserType{
    FREE,
    PREMIUM
}

enum RateLimiterType{
    TOKEN_BUCKET,
    LEAKY_BUCKET,
    FIXED_WINDOW,
    SLIDING_WINDOW_COUNTER,
    SLIDING_WINDOW_LOG
}

class User {
    private final String userId;
    private final UserType userType;

    public User(String userId, UserType userType) {
        this.userType=userType;
        this.userId=userId;
    }

    public String getUserId(){
        return userId;
    }
    public UserType getUserType(){
        return userType;
    }
}

class RateLimitConfig{
    private final int maxRequest;
    private final int windowSize;
    public RateLimitConfig(int maxRequest, int windowSize) {
        this.maxRequest=maxRequest;
        this.windowSize=windowSize;
        if(maxRequest <= 0){
            throw new IllegalArgumentException(
                    "maxRequests must be greater than 0"
            );
        }
        if (windowSize <= 0) {
            throw new IllegalArgumentException(
                    "windowSize must be greater than 0"
            );
        }
    }

    public int getMaxRequest(){
        return maxRequest;
    }

    public int getWindowSize(){
        return windowSize;
    }
}

class RateLimiterFactory {
    public static RateLimiter createRateLimiter(RateLimitConfig rateLimitConfig, RateLimiterType rateLimiterType) {
        return switch (rateLimiterType){
            case FIXED_WINDOW -> new FixedWindowRateLimiter(rateLimitConfig);
            case TOKEN_BUCKET -> new TokenBucketRateLimiter(rateLimitConfig);
            case LEAKY_BUCKET -> new LeakyBucketRateLimiter(rateLimitConfig);
            case SLIDING_WINDOW_COUNTER -> new SlidinWindowCounterRateLimiter(rateLimitConfig);
            case SLIDING_WINDOW_LOG -> new SlidingWindowLogRateLimiter(rateLimitConfig);

            default -> throw new IllegalArgumentException("No algorithm matched" + rateLimiterType);
        };
    }
}



abstract class RateLimiter {
    protected final RateLimitConfig rateLimitConfig;
    protected final RateLimiterType rateLimiterType;

    protected RateLimiter(RateLimitConfig rateLimitConfig, RateLimiterType rateLimiterType){
        this.rateLimitConfig=rateLimitConfig;
        this.rateLimiterType = rateLimiterType;
    };

    public abstract boolean allowRequest(String userId);
}

class TokenBucketRateLimiter extends RateLimiter{

    protected TokenBucketRateLimiter(RateLimitConfig rateLimitConfig) {
        super(rateLimitConfig, RateLimiterType.TOKEN_BUCKET);
    }

    @Override
    public boolean allowRequest(String userId) {
        return false;
    }
}
class LeakyBucketRateLimiter extends RateLimiter{

    protected LeakyBucketRateLimiter(RateLimitConfig rateLimitConfig) {
        super(rateLimitConfig,RateLimiterType.LEAKY_BUCKET);
    }

    @Override
    public boolean allowRequest(String userId) {
        return false;
    }
}

class FixedWindowRateLimiter extends RateLimiter{
    private final Map<String, Integer> userRequests = new ConcurrentHashMap<>();
    private final Map<String, Long> userWindow = new HashMap<>();

    protected FixedWindowRateLimiter(RateLimitConfig rateLimitConfig) {
        super(rateLimitConfig, RateLimiterType.FIXED_WINDOW);
    }

    @Override
    public boolean allowRequest(String userId) {
        AtomicBoolean allowed = new AtomicBoolean(false);

        Long currentWindow = System.currentTimeMillis();
        Long existingWindow = userWindow.getOrDefault(userId,0L);
        Long windowSize = rateLimitConfig.getWindowSize()*60*1000L;


        return allowed.get();
    }
}

class SlidinWindowCounterRateLimiter extends RateLimiter{

    protected SlidinWindowCounterRateLimiter(RateLimitConfig rateLimitConfig) {
        super(rateLimitConfig,RateLimiterType.SLIDING_WINDOW_COUNTER);
    }

    @Override
    public boolean allowRequest(String userId) {
        return false;
    }
}

class SlidingWindowLogRateLimiter extends RateLimiter{

    protected SlidingWindowLogRateLimiter(RateLimitConfig rateLimitConfig) {
        super(rateLimitConfig,RateLimiterType.SLIDING_WINDOW_LOG);
    }

    @Override
    public boolean allowRequest(String userId) {
        return false;
    }
}

