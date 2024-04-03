package sdis.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.net.InetAddress;
public class BlacklistManager {

    private final int max;
    private final ConcurrentHashMap<InetAddress, Integer> blackListMap;

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
    public boolean isIPBlocked(InetAddress ip) {
        return blackListMap.containsKey(ip) && blackListMap.get(ip) >= max;
    }

    /**
     * Increments the specific count in one unit.
     * @param ip we want to increment de count.
     */
    public synchronized void incrementCount(InetAddress ip) {
        blackListMap.put(ip, blackListMap.getOrDefault(ip, 0) + 1);
        System.out.println("Contador incrementado en 1 para: "+ip.getHostAddress());
    }

    /**
     * "Resets the count to zero" or removes the specific IP from the blackList
     * @param ip we want to remove.
     */
    public synchronized void resetCount(InetAddress ip) {
        blackListMap.remove(ip);
    }

    public int getCount(InetAddress ip){
        return blackListMap.get(ip);
    }

    public synchronized void decrementCount(InetAddress hostAddress) {
        if (blackListMap.containsKey(hostAddress)){
            blackListMap.put(hostAddress, blackListMap.get(hostAddress)-1);
        }
        else blackListMap.put(hostAddress,0);
    }
}

