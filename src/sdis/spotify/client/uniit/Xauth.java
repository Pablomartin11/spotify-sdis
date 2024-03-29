package sdis.spotify.client.uniit;

import sdis.spotify.common.MalMensajeProtocoloException;
import sdis.spotify.common.MensajeProtocolo;
import sdis.spotify.common.Primitiva;

public class Xauth {
    final private int PUERTO = 2000;
    static java.io.ObjectInputStream ois = null;
    static java.io.ObjectOutputStream oos = null;

    public static void main(String[] args) throws java.io.IOException {
        if (args.length != 3) {
            System.out.println("Use:\njava protocol.cliente.XAUTH host user password");
            System.exit(-1);
        }
        String host = args[0];  // localhost o ip|dn del servidor
        String usr = args[1];  // usuario
        String pswd = args[2];  // contraseña
        java.net.Socket sock = new java.net.Socket(host, 2000);
        try {
            oos = new java.io.ObjectOutputStream(sock.getOutputStream());
            ois = new java.io.ObjectInputStream(sock.getInputStream());
            System.out.println("<" + (MensajeProtocolo) ois.readObject());
            pruebaPeticionRespuesta(new MensajeProtocolo(Primitiva.XAUTH, usr, pswd));
        } catch (java.io.EOFException e) {
            System.err.println("Cliente: Fin de conexión.");
        } catch (java.io.IOException e) {
            System.err.println("Cliente: Error de apertura o E/S sobre objetos:"+e);
        } catch (MalMensajeProtocoloException e) {
            System.err.println("Cliente: Error mensaje Protocolo: "+e);
        } catch (Exception e) {
            System.err.println("Cliente: Excepción. Cerrando Sockets: "+e);
        } finally {
            ois.close();
            oos.close();
            sock.close();
        }
    }

    static void pruebaPeticionRespuesta(MensajeProtocolo mp) throws java.io.IOException, MalMensajeProtocoloException, ClassNotFoundException {
        System.out.println("> "+mp);
        oos.writeObject(mp);
        System.out.println("< "+(MensajeProtocolo) ois.readObject());
    }
}

