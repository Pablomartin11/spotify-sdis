package sdis.utils;

import java.util.concurrent.ConcurrentHashMap;
public class BlacklistManager {

    private final int max;
    private final ConcurrentHashMap<String, Integer> blackListMap;

    /**
     * Blacklist Manager Constructor.
     */
    public BlacklistManager(int max){
        this.blackListMap = new ConcurrentHashMap<>();
        this.max = max;
    }

    /**
     * Checks if an specific IP has reached the maximum.
     * It could be conecctions, logins,etc.
     * @param ip of the client we want to check.
     * @return boolean
     */
    public boolean isIPBlocked(String ip) {
        return blackListMap.containsKey(ip) && blackListMap.get(ip) > max;
    }

    /**
     * Increments the specific count in one unit.
     * @param ip we want to increment de count.
     */
    public void incrementCount(String ip) {
        blackListMap.put(ip, blackListMap.getOrDefault(ip, 0) + 1);
        System.out.println("Contador incrementado en 1 para: "+ip);
    }

    /**
     * "Resets the count to zero" or removes the specific IP from the blackList
     * @param ip we want to remove.
     */
    public void resetCount(String ip) {
        blackListMap.remove(ip);
    }
}

