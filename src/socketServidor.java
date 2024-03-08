public class socketServidor {
    public static final int PUERTO = 4000;

    public static void main(String[] args) {
        try (java.net.ServerSocket servidor = new java.net.ServerSocket(PUERTO)) {
            while (true) {
                try {
                    System.out.println("----Servidor esperando al cliente ----");
                    java.net.Socket sock = servidor.accept(); // ojito!! sin try-with-rc
                    java.io.BufferedReader inred =
                            new java.io.BufferedReader(

                                    new java.io.InputStreamReader(sock.getInputStream()));
                    java.io.PrintStream
                            outred =
                            new java.io.PrintStream(sock.getOutputStream());



                    Runnable sirviente = () -> {
                        try {
                            String linea;
                            while ((linea = inred.readLine()) != null) {
                                System.out.println("Echoing: "+linea);



                                outred.println(linea);
                            }
                        } catch (java.io.IOException ioe) {

                            System.err.println("Cerrando socket de cliente");

                            ioe.printStackTrace(System.err);

                        }
                    } ;
                    Thread t = new Thread(sirviente, "Sirviente echo");
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
}
