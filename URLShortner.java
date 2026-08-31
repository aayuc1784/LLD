import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
//import com.github.f4b6a3.uuid.UuidCreator;

public class URLShortner {
    public static void main(String[] args){
        String url = "www.google.com/";
        URLShortnerService urlShortnerService = URLShortnerService.getInstance();
        System.out.println(urlShortnerService.generateShortURL(url, URLStrategy.COUNTER));
        System.out.println(urlShortnerService.generateShortURL(url));
        System.out.println(urlShortnerService.generateShortURL(url, URLStrategy.COUNTER));
    }
}

class URLShortnerService {
    private final static URLShortnerService instance = new URLShortnerService();
    private final String BASE_URL = "www.bit.ly/";

    private URLShortnerService(){

    }

    public static URLShortnerService getInstance() {
        return instance;
    }

    public String generateShortURL(String url, URLStrategy urlStrategy){
        return BASE_URL + URLStrategyFactory.getInstance().getStrategy(urlStrategy).generate(url);
    }

    public String generateShortURL(String url){
        return BASE_URL + URLStrategyFactory.getInstance().getStrategy().generate(url);
    }

    // redirection logic || validation || track to get previous (storing shortURL -> longURL to redirect)
}

class URLStrategyFactory {
    private final Map<URLStrategy, GenerateURLStrategy> urlStrategyFactory;
    private static final URLStrategyFactory instance = new URLStrategyFactory();

    private URLStrategyFactory(){
        urlStrategyFactory = new HashMap<>();
        urlStrategyFactory.put(URLStrategy.COUNTER, new CounterStrategy());
        urlStrategyFactory.put(URLStrategy.HASH, new HashStrategy());
        urlStrategyFactory.put(URLStrategy.RANDOM, new RandomStrategy(7));
//        urlStrategyFactory.put(URLStrategy.UUIDv7, new UUIDv7Strategy());
        urlStrategyFactory.put(URLStrategy.SNOWFLAKE, new SnowflakeStrategy());
    }

    public static URLStrategyFactory getInstance(){
        return instance;
    }

    public GenerateURLStrategy getStrategy(URLStrategy urlStrategy){
        GenerateURLStrategy generateURLStrategy = urlStrategyFactory.get(urlStrategy);
        if(generateURLStrategy == null){
            throw new IllegalArgumentException("Unsupported Strategy");
        }
        return generateURLStrategy;
    }
    public GenerateURLStrategy getStrategy(){
        return urlStrategyFactory.get(URLStrategy.SNOWFLAKE);
    }

}

enum URLStrategy {
    COUNTER, RANDOM, UUIDv7, HASH, SNOWFLAKE
}

interface GenerateURLStrategy {
    String generate(String url);
}

class CounterStrategy implements GenerateURLStrategy {
    private final AtomicLong counter = new AtomicLong(0);
    private final Base62Encoder base62Encoder = new Base62Encoder();

    @Override
    public String generate(String url) {
        return base62Encoder.encodeId(counter.incrementAndGet());
    }

}

class RandomStrategy implements GenerateURLStrategy {
    private static final String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom secureRandom = new SecureRandom();

    private final int urlLength;

    public RandomStrategy (int urlLength){
        this.urlLength = urlLength;
    }

    @Override
    public String generate(String url) {
        StringBuilder result = new StringBuilder();
        for(int id=0;id<urlLength;id++){
            result.append(characters.charAt(secureRandom.nextInt(characters.length())));
        }
        return result.toString();
    }
}

//class UUIDv7Strategy implements GenerateURLStrategy {
//    private final Base62Encoder base62Encoder = new Base62Encoder();
//
//    @Override
//    public String generate(String url) {
//        UUID uuid = UuidCreator.getTimeOrderedEpoch();
//        return base62Encoder.encodeId(Bytes(uuid));
//    }
//
//    private byte[] Bytes(UUID uuid){
//        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
//        byteBuffer.putLong(uuid.getMostSignificantBits());
//        byteBuffer.putLong(uuid.getLeastSignificantBits());
//        return byteBuffer.array();
//    }
//}

class HashStrategy implements GenerateURLStrategy {

    @Override
    public String generate(String url) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes(url));
        // .encodeToString(Arrays.copyOf(hash,8))
    }

    private byte[] bytes(String url){
        try {
            return MessageDigest.getInstance("SHA-256").digest(url.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

class SnowflakeStrategy implements GenerateURLStrategy {
    private final Base62Encoder base62Encoder = new Base62Encoder();
    @Override
    public String generate(String url) {
        return base62Encoder.encodeId(SnowflakeIdGenerator.getInstance().generateId());
    }
}

interface Encoder {
    String encodeId(long id);
    String encodeId(byte[] bytes);
}

class Base62Encoder implements Encoder {
    private static final String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final BigInteger base = BigInteger.valueOf(62);
    public Base62Encoder(){

    }

    @Override
    public String encodeId(long id) {
        if(id==0){
            return String.valueOf(characters.charAt(0));
        }
        StringBuilder result = new StringBuilder();
        while(id > 0){
            result.append(characters.charAt((int) (id % 62)));
            id/=62;
        }

        return result.reverse().toString();
    }

    @Override
    public String encodeId(byte [] bytes){
        BigInteger id = new BigInteger(1,bytes);
        if(id.signum() == 0){
            return String.valueOf(characters.charAt(0));
        }
        StringBuilder result = new StringBuilder();
        while(id.signum() > 0){
            BigInteger[] divide = id.divideAndRemainder(base);
            result.append(characters.charAt(divide[1].intValue()));
            id=divide[0];
        }
        return result.reverse().toString();
    }

}

class SnowflakeIdGenerator{
    private final static SnowflakeIdGenerator instance = new SnowflakeIdGenerator();
    private long serverId;
    private static final long EPOCH = 1577836800000L;
    private long lastTimeStamp=-1L;
    private long sequence = 0;
    private final static long maxSequence = (1L<<12)-1;

    private SnowflakeIdGenerator(){
        // return one time serverId
        generateServerId();
    }

    private void generateServerId(){
        // get serverId
        // for now using 10 bit value
        SecureRandom secureRandom = new SecureRandom();
        this.serverId = secureRandom.nextInt(1024);
    }


    public static SnowflakeIdGenerator getInstance(){
        return instance;
    }

    public synchronized long generateId(){
        long currentTime = System.currentTimeMillis();

        if(currentTime < lastTimeStamp){
            throw new IllegalArgumentException("Clock Backward");
        }

        if(currentTime == lastTimeStamp){
            sequence = (sequence+1)&maxSequence;
            if(sequence == 0){
                currentTime = nextPossibleTime(lastTimeStamp);
            }
        } else {
            sequence = 0;
        }
        lastTimeStamp = currentTime;
        return ((currentTime - EPOCH) << 22) | (serverId << 12) | sequence;
    }

    private long nextPossibleTime(long lastTimeStamp){
        long timestamp = System.currentTimeMillis();
        while(timestamp <=  lastTimeStamp){
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
