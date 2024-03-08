public class socketCliente {
    public static final int PUERTO =4000;
    public static void main(String[] args){
        String linea = null;
        try{
            java.io.BufferedReader tec =  new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

            java.net.Socket miSocket = new java.net.Socket("localhost",PUERTO);

            java.io.BufferedReader inred = new java.io.BufferedReader(new java.io.InputStreamReader(miSocket.getInputStream()));
            java.io.PrintStream outred = new java.io.PrintStream(miSocket.getOutputStream());

            while((linea =tec.readLine()) != null){
                outred.println(linea);
                linea = inred.readLine();
                System.out.println("Recibido "+linea);
            }
        } catch (Exception e){e.printStackTrace();}

    }
}
