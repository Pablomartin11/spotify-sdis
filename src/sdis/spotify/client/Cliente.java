package sdis.spotify.client;

import sdis.spotify.common.MalMensajeProtocoloException;
import sdis.spotify.common.MensajeProtocolo;
import sdis.spotify.common.Primitiva;
import sdis.spotify.common.Strings;

public class Cliente {
    final private int PUERTO = 2000;
    static java.io.ObjectInputStream ois = null;
    static java.io.ObjectOutputStream oos = null;
    public static void main(String[] args) throws java.io.IOException {
        String host = "localhost";
        String linea;
        java.net.Socket sock = new java.net.Socket(host, 2000);
        try {
            //Creación de los canales de serialización de objetos
            oos = new java.io.ObjectOutputStream(sock.getOutputStream());
            ois = new java.io.ObjectInputStream(sock.getInputStream());
            
            // Espera la bienvenida del servidor
            MensajeProtocolo me = (MensajeProtocolo) ois.readObject();
            System.out.println("\n< "+ me.getMensaje());

            
            //Sin teclado, probemos las primitivas por programa
            System.out.println(Strings.AVISO_EMPEZAR); System.in.read();
            //INICIO Escenario 1

            pruebaPeticionRespuesta(new MensajeProtocolo(Primitiva.XAUTH,"Hector", "1234"));
            pruebaPeticionRespuesta(new MensajeProtocolo(Primitiva.XAUTH,"hector", "1234"));
            pruebaPeticionRespuesta(new MensajeProtocolo(Primitiva.ADD2L,"Lista1", "Cancion1"));
            pruebaPeticionRespuesta(new MensajeProtocolo(Primitiva.ADD2L,"Lista1", "Cancion2"));

            pruebaPeticionRespuesta(new MensajeProtocolo(Primitiva.READL,"Lista1"));


            //a estas alguras algún cliente externo debería insertar un mensaje en la cola
            //FIN Esceniario 1
        } catch (java.io.EOFException e) {
            System.err.println(Strings.ERRORC_FINCONEXION);
        } catch (java.io.IOException e) {
            System.err.println(Strings.ERRORC_APERTURA_ES +e);
        } catch (MalMensajeProtocoloException e) {
            System.err.println(Strings.ERRORC_MALMENSAJEPROTOCOLO +e);
        } catch (Exception e) {
            System.err.println(Strings.ERRORC_EXCEPCION +e);
        } finally {
        ois.close();
        oos.close();
        sock.close();
        }
    }
 
    //Prueba una interacción de escritura y lectura con el servidor
    static void pruebaPeticionRespuesta(MensajeProtocolo mp) throws java.io.IOException, MalMensajeProtocoloException, ClassNotFoundException {
        System.out.println("> "+mp);
        oos.writeObject(mp);
        System.out.println("< "+(MensajeProtocolo) ois.readObject());
        System.out.println("\nPulsa <Enter>"); System.in.read();
    }
}
