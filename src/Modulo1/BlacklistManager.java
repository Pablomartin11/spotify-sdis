package Modulo1;

import java.util.concurrent.ConcurrentHashMap;
public class BlacklistManager {

    private static final int conexionesMaximas = 3;
    private static final int intentosConexion = 2;

    private final ConcurrentHashMap<String, Integer> conexionesMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> loginFallidoMap = new ConcurrentHashMap<>();

    //Comprueba si contiene la ip y hay menos de las conexiones requeridas
    public boolean isIPBlockedForConnections(String ip) {
        return conexionesMap.containsKey(ip) && conexionesMap.get(ip) >= conexionesMaximas;
    }

    //Comprueba si contiene la ip y si ha fallado el login
    public boolean isIPBlockedForLogin(String ip) {
        return loginFallidoMap.containsKey(ip) && loginFallidoMap.get(ip) >= intentosConexion;
    }

    //Incrementa la conexion en 1
    public void incrementConnectionCount(String ip) {
        conexionesMap.put(ip, conexionesMap.getOrDefault(ip, 0) + 1);
        System.out.println("Conexion incrementada en 1 para: "+ip);
    }

    //Incrementa el login fallido en 1
    public void incrementLoginFailCount(String ip) {
        loginFallidoMap.put(ip, loginFallidoMap.getOrDefault(ip, 0) + 1);
        System.out.println("Login fallido incrementado en 1 para: "+ip);
    }

    // Resetea el login fallido para una ip
    public void resetLoginFailCount(String ip) {
        loginFallidoMap.remove(ip);
    }

    // Quita las conexiones para una ip
    public void clearConnectionCount(String ip) {
        conexionesMap.remove(ip);
    }

}

