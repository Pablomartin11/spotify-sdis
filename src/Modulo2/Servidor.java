package Modulo2;

public class Servidor {
    public static void main(String args[]) {
    int PUERTO = 2000;    //puerto de servicio
    int NThreads = 5;  //hilos del ThreadPool
    java.util.concurrent.ExecutorService exec = java.util.concurrent.Executors.newFixedThreadPool(NThreads);

    MultiMap<String, String> mapa = new MultiMap();

    try {
        java.net.ServerSocket sock = new java.net.ServerSocket(PUERTO);

        System.err.println("Servidor: WHILE [INICIANDO]");

        Thread mainServer = new Thread(() -> {
            try {
                //WHILE
                while (true) {
                    java.net.Socket socket = sock.accept();
                    try {
                        Sirviente serv = new Sirviente(socket, mapa);
                        exec.execute(serv);
                    } catch (java.io.IOException ioe) {
                        System.out.println("Servidor: WHILE [ERR ObjectStreams]");
                    }
                }  //fin-while
            } catch (java.io.IOException ioe) {
                System.err.println("Servidor: WHILE [Error.E/S]");
            } catch (Exception e) {
            System.err.println("Servidor: WHILE [Error.execute]");
            }
            }, "RUN(WHILE)");  //fin-newThread separado para servidor
            mainServer.start();
            System.out.println("Servidor: [CORRIENDO]");
        
    } catch (java.io.IOException ioe) {
        System.out.println("Servidor: [ERR SSOCKET]");
        }
    }
}
