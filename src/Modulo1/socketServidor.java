package Modulo1;

import java.io.PrintStream;
import java.util.concurrent.ConcurrentHashMap;
import java.net.InetAddress;

public class socketServidor {
    public static final int PUERTO = 10000;

    private static final BlacklistManager connectionBlacklistManager = new BlacklistManager(3);
    private static final BlacklistManager loginBlacklistManager = new BlacklistManager(2);

    // Almacena las credenciales en un ConcurrentHashMap
    private static final ConcurrentHashMap<String, String> credenciales = new ConcurrentHashMap<>();


    static {
        credenciales.put("hector", "1234");
        credenciales.put("sdis","asdf");
    }

    public static void main(String[] args) {
        try (java.net.ServerSocket servidor = new java.net.ServerSocket(PUERTO)) {
            while (true) {
                try {
                    System.out.println("----Servidor esperando al cliente ----");
                    java.net.Socket sock = servidor.accept(); // ojito!! sin try-with-rc

                    InetAddress client = sock.getInetAddress();
                    String clientIP = client.getHostAddress();


                    if (connectionBlacklistManager.isIPBlocked(clientIP)) {
                        PrintStream outred = new PrintStream(sock.getOutputStream());
                        outred.println("Err Max Number of connections reached.");
                        continue;
                    }

                    java.io.BufferedReader inred = new java.io.BufferedReader( new java.io.InputStreamReader(sock.getInputStream()));
                    java.io.PrintStream outred = new java.io.PrintStream(sock.getOutputStream());

                    connectionBlacklistManager.incrementCount(clientIP);
                    
                    // Definimos el hilo.
                    Runnable servant = () -> {
                        try {
                            outred.println("Welcome, please type your credentials to LOG in");

                            String username = inred.readLine();
                            outred.println("OK: password?");

                            String password = inred.readLine();

                            if (validateCredentials(username, password)) {
                                connectionBlacklistManager.resetCount(clientIP); // Clear connection count for successful login
                                outred.println("User successfully logged in");
                            } else {
                                loginBlacklistManager.incrementCount(clientIP);
                                if (loginBlacklistManager.isIPBlocked(clientIP)) {
                                    connectionBlacklistManager.resetCount(clientIP); // Clear connection count for IP blocked due to login failures
                                    outred.println("Err Max Number of login attempts reached.");
                                }
                                outred.println("Credentials does not match our records: Enter username again:");
                            }
                        } catch (java.io.IOException ioe){
                            System.err.println("Cerrando socket de cliente");
                            ioe.printStackTrace(System.err);
                        }
                };

                // Corremos el hilo.
                Thread t = new Thread(servant, "Sirviente Peticiones");
                t.start();

                } catch (java.io.IOException e) {
                    System.err.println("Cerrando socket de cliente");
                    e.printStackTrace(System.err);
                }
            } // fin del while()

        } catch (java.io.IOException e) {

            System.err.println("Cerrando socket de servicio");

            e.printStackTrace(System.err);

        }
    }
    private static boolean validateCredentials(String username, String password) {
        // Verifica si las credenciales proporcionadas coinciden con las almacenadas
        String storedPassword = credenciales.get(username);
        return storedPassword != null && storedPassword.equals(password);
    }
}
