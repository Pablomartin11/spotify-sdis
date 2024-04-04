package sdis.spotify.client.unit;

import sdis.spotify.common.MalMensajeProtocoloException;
import sdis.spotify.common.MensajeProtocolo;
import sdis.spotify.common.Primitiva;
import sdis.spotify.common.Strings;

public class Readl {
    final private int PUERTO = 2000;
    static java.io.ObjectInputStream ois = null;
    static java.io.ObjectOutputStream oos = null;

    public static void main(String[] args) throws java.io.IOException {
        if (args.length != 2) {
            System.out.println("Use:\njava protocol.cliente.READL host clave valor");
            System.exit(-1);
        }
        String host = args[0];  //localhost o ip|dn del servidor
        String clave = args[1];  //clave del deposito
        java.net.Socket sock = new java.net.Socket(host, 2000);
        try {
            oos = new java.io.ObjectOutputStream(sock.getOutputStream());
            ois = new java.io.ObjectInputStream(sock.getInputStream());

            System.out.println("<" + (MensajeProtocolo) ois.readObject());
            pruebaPeticionRespuesta(new MensajeProtocolo(Primitiva.XAUTH, "hector", "1234"));

            pruebaPeticionRespuesta(new MensajeProtocolo(Primitiva.READL, clave));
        } catch (java.io.EOFException e) {
            System.err.println(Strings.ERROR_FINCONEXION);
        } catch (java.io.IOException e) {
            System.err.println(Strings.ERROR_APERTURA_ES+e);
        } catch (MalMensajeProtocoloException e) {
            System.err.println(Strings.ERROR_MALMENSAJEPROTOCOLO +e);
        } catch (Exception e) {
            System.err.println(Strings.ERROR_EXCEPCION+e);
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

