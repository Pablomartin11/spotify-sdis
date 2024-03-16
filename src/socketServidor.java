import java.io.PrintStream;
import java.util.concurrent.ConcurrentHashMap;

public class socketServidor {
    public static final int PUERTO = 10000;

    private static final BlacklistManager connectionBlacklistManager = new BlacklistManager();
    private static final BlacklistManager loginBlacklistManager = new BlacklistManager();

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

                    //TODO CAMBIAR STRING POR INETADRESS
                    String clientIP = sock.getInetAddress().getHostAddress();


                    if (connectionBlacklistManager.isIPBlockedForConnections(clientIP)) {
                        PrintStream outred = new PrintStream(sock.getOutputStream());
                        outred.println("Err Max Number of connections reached.");
                        continue;
                    }

                    java.io.BufferedReader inred = new java.io.BufferedReader( new java.io.InputStreamReader(sock.getInputStream()));
                    java.io.PrintStream outred = new java.io.PrintStream(sock.getOutputStream());

                    connectionBlacklistManager.incrementConnectionCount(clientIP);


                    outred.println("Welcome, please type your credentials to LOG in");

                    String username = inred.readLine();
                    outred.println("OK: password?");

                    String password = inred.readLine();

                    if (validateCredentials(username, password)) {
                        connectionBlacklistManager.clearConnectionCount(clientIP); // Clear connection count for successful login
                        outred.println("User successfully logged in");
                    } else {
                        loginBlacklistManager.incrementLoginFailCount(clientIP);
                        if (loginBlacklistManager.isIPBlockedForLogin(clientIP)) {
                            connectionBlacklistManager.clearConnectionCount(clientIP); // Clear connection count for IP blocked due to login failures
                            outred.println("Err Max Number of login attempts reached.");
                            continue;
                        }
                        outred.println("Credentials does not match our records: Enter username again:");
                        continue; // Retry the interaction sequence
                    }


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
