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
        return blackListMap.containsKey(ip) && blackListMap.get(ip) >= max;
    }

    /**
     * Increments the specific count in one unit.
     * @param ip we want to increment de count.
     */
    public synchronized void incrementCount(String ip) {
        blackListMap.put(ip, blackListMap.getOrDefault(ip, 0) + 1);
        System.out.println("Contador incrementado en 1 para: "+ip);
    }

    /**
     * "Resets the count to zero" or removes the specific IP from the blackList
     * @param ip we want to remove.
     */
    public synchronized void resetCount(String ip) {
        blackListMap.remove(ip);
    }

    public int getCount(String ip){
        return blackListMap.get(ip);
    }

    public synchronized void decrementCount(String hostAddress) {
        if (blackListMap.containsKey(hostAddress)){
            blackListMap.put(hostAddress, blackListMap.get(hostAddress)-1);
        }
        else blackListMap.put(hostAddress,0);
    }
}

