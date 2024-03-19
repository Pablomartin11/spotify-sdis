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

            String loginCheck = "1";
            do {
                System.out.println(inred.readLine());
                linea = tec.readLine();
                outred.println(linea); // envia el usuario

                System.out.println(inred.readLine());

                linea = tec.readLine();
                outred.println(linea); // envia la contraseña

                loginCheck = inred.readLine();
                System.out.println(loginCheck); //Respuesta

            } while(!loginCheck.equals("User successfully logged in"));

        } catch (Exception e){e.printStackTrace();}

    }
}
