package sdis.spotify.server;

import java.net.InetAddress;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import sdis.spotify.common.Strings;
import sdis.utils.BlacklistManager;
import sdis.utils.MultiMap;
import sdis.spotify.common.MalMensajeProtocoloException;
import sdis.spotify.common.MensajeProtocolo;
import sdis.spotify.common.Primitiva;

class Sirviente implements Runnable {
    private final Socket socket;
    private final MultiMap<String, String> mapa;
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;
    private final int ns;
    private static AtomicInteger nInstance = new AtomicInteger();

    private final InetAddress client;
    private final BlacklistManager logins;
    private final BlacklistManager conections;
    private static final ConcurrentHashMap<String, String> credenciales = new ConcurrentHashMap<>();
    private boolean usrLogged;
    private boolean banned;

    static {
        credenciales.put("hector", "1234");
        credenciales.put("sdis","asdf");
    }

    Sirviente(Socket s, MultiMap<String, String> c, InetAddress client, BlacklistManager conectionsBlacklistManager,BlacklistManager loginsBlackListManager) throws IOException {
        this.socket = s;
        this.mapa = c;
        this.ns = nInstance.getAndIncrement();
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.ois = new ObjectInputStream(socket.getInputStream());
        this.logins = loginsBlackListManager;
        this.conections = conectionsBlacklistManager;
        this.client = client;
        this.usrLogged = false;
        this.banned = false;

    }  //se invoca en el Servidor, usualmente

    public void run() {
        try {
            MensajeProtocolo msFirst;
            if (conections.isIPBlocked(client)){
                msFirst = new MensajeProtocolo(Primitiva.NOTAUTH, Strings.ERROR_MAX_CONNECIONS);
                this.banned = true;
            }
            else {
                conections.incrementCount(client);
                msFirst = new MensajeProtocolo(Primitiva.INFO, Strings.MENSAJE_INICIO);
            }

            oos.writeObject(msFirst);  //concentra la escritura de mensajes ¿bueno?
            System.out.println("Sirviente: "+ns+": [RESP: "+msFirst+"]");

            while (!this.banned) {
                MensajeProtocolo me = (MensajeProtocolo) ois.readObject();
                MensajeProtocolo ms = null;
                //me y ms: mensajes entrante y saliente
                System.out.println("Sirviente: "+ns+": [ME: "+ me+"]");  //depuracion me

                switch (me.getPrimitiva()) {
                case INFO:
                    ms = new MensajeProtocolo(Primitiva.INFO, Strings.MENSAJE_INICIO);
                break;
                case XAUTH:
                    String usr = me.getIdCola();
                    String pswd = me.getMensaje();
                    
                    if (logins.isIPBlocked(client)){
                        ms = new MensajeProtocolo(Primitiva.ERROR, Strings.ERROR_MAX_FAIL_LOGGINS);
                    }
                    else {
                        if (validateCredentials(usr,pswd)){
                            this.usrLogged = true;
                            ms = new MensajeProtocolo(Primitiva.XAUTH, Strings.MENSAJE_LOGGED);
                            logins.resetCount(client);
                        }
                        else{
                            logins.incrementCount(client);
                            ms = new MensajeProtocolo(Primitiva.NOTAUTH, Strings.ERROR_401_CREDENTIALS);
                        }
                    }

                break;
                case ADD2L:
                    if (this.usrLogged && !this.banned){
                        String key = me.getIdCola();
                        String val = me.getMensaje();
                        mapa.push(key, val);
                        ms = new MensajeProtocolo(Primitiva.ADDED);
                    }
                    else if(!this.usrLogged) ms = new MensajeProtocolo(Primitiva.NOTAUTH,Strings.ERROR_NOT_LOGIN);
                    else if(this.banned) ms = new MensajeProtocolo(Primitiva.ERROR,Strings.ERROR_MAX_CONNECIONS);
                break;
                case READL:
                    if(this.usrLogged && !this.banned){
                        String key = me.getIdCola();
                        String men = null;
                        men = mapa.pop(key);
                        if( null != men ){
                            ms = new MensajeProtocolo(Primitiva.MEDIA, men);
                        }else {
                            ms = new MensajeProtocolo(Primitiva.EMPTY);
                        }
                    }
                    else if(!this.usrLogged) ms = new MensajeProtocolo(Primitiva.NOTAUTH,Strings.ERROR_NOT_LOGIN);
                    else if(this.banned) ms = new MensajeProtocolo(Primitiva.ERROR,Strings.ERROR_MAX_CONNECIONS);
                break;

                case DELETEL:
                    if(this.usrLogged && !this.banned){
                        String key = me.getIdCola();
                        boolean valor = false;
                        //Se comprueba que la clave existe
                        if(mapa.containsKey(key)){
                            valor = mapa.remove(key);
                            ms = new MensajeProtocolo(Primitiva.DELETED);
                        }else {
                            ms = new MensajeProtocolo(Primitiva.EMPTY);
                        }
                        if(!valor){ // Sino se consigue eliminar
                            ms = new MensajeProtocolo(Primitiva.EMPTY);
                        }

                    } 
                    else if(!this.usrLogged) ms = new MensajeProtocolo(Primitiva.NOTAUTH,Strings.ERROR_NOT_LOGIN);
                    else if(this.banned) ms = new MensajeProtocolo(Primitiva.ERROR,Strings.ERROR_MAX_CONNECIONS);
                break;

                default:
                    ms = new MensajeProtocolo(Primitiva.ERROR,Strings.ERROR_NOT_UNDERSTAND);
                }  //fin del selector segun el mensaje entrante
            
                oos.writeObject(ms);  //concentra la escritura de mensajes ¿bueno?
                //depuracion de mensaje saliente
                System.out.println("Sirviente: "+ns+": [RESP: "+ms+"]");
            }
        } catch (IOException e) {
            System.err.println("Sirviente: "+ns+": [FIN]");
        } catch (ClassNotFoundException ex) {
            System.err.println("Sirviente: "+ns+": [ERR Class not found]");
        } //catch (InterruptedException ex) {
            //System.err.println("Sirviente: "+ns+": [Interrumpido wait()]");
        //}
         catch (MalMensajeProtocoloException ex) {
            System.err.println("Sirviente: "+ns+": [ERR MalMensajeProtocolo ]");
        } finally {
        //seguimos deshaciéndonos de los sockets y canales abiertos.

        // Quitamos una conexion del contador si no está baneado, si lo estuviera, solo por el mero hecho de intentar la conexión, aunque le eche, seguiria restando uno y habria un fallo de seguridad
        if (!this.banned){
            this.conections.decrementCount(client);
        }
        
        try {
            ois.close();
            oos.close();
            socket.close();
            } catch (Exception x) {
                System.err.println("Sirviente: "+ns+": [ERR Cerrando sockets]");
            }
        }
    }

    private static boolean validateCredentials(String username, String password) {
        // Verifica si las credenciales proporcionadas coinciden con las almacenadas
        String storedPassword = credenciales.get(username);
        return storedPassword != null && storedPassword.equals(password);
    }

}