package Modulo1;

public class socketCliente {
    public static final int PUERTO =10000;
    public static void main(String[] args){
        String linea = null;
        try{
            java.io.BufferedReader tec =  new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

            java.net.Socket miSocket = new java.net.Socket("localhost",PUERTO);

            java.io.BufferedReader inred = new java.io.BufferedReader(new java.io.InputStreamReader(miSocket.getInputStream()));
            java.io.PrintStream outred = new java.io.PrintStream(miSocket.getOutputStream());

            System.out.println(inred.readLine());
            linea = tec.readLine();
            outred.println(linea); // envia el usuario

            System.out.println(inred.readLine());

            linea = tec.readLine();
            outred.println(linea); // envia la contraseña

            System.out.println(inred.readLine()); //Respuesta

        } catch (Exception e){e.printStackTrace();}

    }
}
