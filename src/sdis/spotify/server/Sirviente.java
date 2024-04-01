package sdis.spotify.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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

    Sirviente(Socket s, MultiMap<String, String> c, InetAddress client, BlacklistManager conectionsBlacklistManager) throws IOException {
        this.socket = s;
        this.mapa = c;
        this.ns = nInstance.getAndIncrement();
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.ois = new ObjectInputStream(socket.getInputStream());
        this.logins = new BlacklistManager(2);
        this.conections = conectionsBlacklistManager;
        this.client = client;
        this.usrLogged = false;
        this.banned = false;

    }  //se invoca en el Servidor, usualmente

    public void run() {
        try {
            MensajeProtocolo msFirst;
            if (conections.isIPBlocked(this.client.getHostAddress())){
                msFirst = new MensajeProtocolo(Primitiva.NOTAUTH, "Err Max Number of connections reached.");
                this.banned = true;
            } 
            else {
                conections.incrementCount(this.client.getHostAddress());
                msFirst = new MensajeProtocolo(Primitiva.INFO, "Welcome, please type your credentials to LOG in");
            }

            oos.writeObject(msFirst);  //concentra la escritura de mensajes ¿bueno?
            System.out.println("Sirviente: "+ns+": [RESP: "+msFirst+"]");

            while (true && !this.banned) {
                MensajeProtocolo me = (MensajeProtocolo) ois.readObject();
                MensajeProtocolo ms = null;
                //me y ms: mensajes entrante y saliente
                System.out.println("Sirviente: "+ns+": [ME: "+ me+"]");  //depuracion me

                switch (me.getPrimitiva()) {
                case INFO:
                    ms = new MensajeProtocolo(Primitiva.INFO, "Welcome, please type your credentials to LOG in");
                break;
                case XAUTH:
                    String usr = me.getIdCola();
                    String pswd = me.getMensaje();
                    
                    if (logins.isIPBlocked(this.client.getHostAddress())){
                        ms = new MensajeProtocolo(Primitiva.ERROR, "Err Max Number of login attempts reached.");
                    }
                    else {
                        if (validateCredentials(usr,pswd)){
                            this.usrLogged = true;
                            ms = new MensajeProtocolo(Primitiva.XAUTH, "User successfully logged");
                            logins.resetCount(this.client.getHostAddress());
                        }
                        else{
                            logins.incrementCount(this.client.getHostAddress());
                            ms = new MensajeProtocolo(Primitiva.NOTAUTH, "Err 401 ~ Credentials DO NOT MATCH. Try again" );
                        }
                    }

                break;
                case ADD2L:
                    if (this.usrLogged){
                        String key = me.getIdCola();
                        String val = me.getMensaje();
                        mapa.push(key, val);
                        ms = new MensajeProtocolo(Primitiva.ADDED);
                    }
                    else ms = new MensajeProtocolo(Primitiva.NOTAUTH,"User login is required");
                break;
                case READL:
                    if(this.usrLogged){
                        String key = me.getIdCola();
                        String men = null;
                        men = mapa.pop(key);
                        if( null != men ){
                            ms = new MensajeProtocolo(Primitiva.MEDIA, men);
                        }else {
                            ms = new MensajeProtocolo(Primitiva.EMPTY);
                        }
                    }
                    else ms = new MensajeProtocolo(Primitiva.NOTAUTH,"User login is required");
                break;

                case DELETEL:
                    if(this.usrLogged){
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
                    else ms = new MensajeProtocolo(Primitiva.NOTAUTH,"User login is required");
                break;

                default:
                    ms = new MensajeProtocolo(Primitiva.ERROR,"Err Not Understand ");
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
            this.conections.decrementCount(this.client.getHostAddress());
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