package sdis.spotify.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import Modulo1.BlacklistManager;
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
    private static final ConcurrentHashMap<String, String> credenciales = new ConcurrentHashMap<>();
    private boolean usrLogged;

    static {
        credenciales.put("hector", "1234");
        credenciales.put("sdis","asdf");
    }

    Sirviente(Socket s, MultiMap<String, String> c, InetAddress client) throws IOException {
        this.socket = s;
        this.mapa = c;
        this.ns = nInstance.getAndIncrement();
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.ois = new ObjectInputStream(socket.getInputStream());
        this.logins = new BlacklistManager(2);
        this.client = client;
        this.usrLogged = false;
    }  //se invoca en el Servidor, usualmente

    public void run() {
        try {
            MensajeProtocolo msBienvenida = new MensajeProtocolo(Primitiva.INFO, "Welcome, please type your credentials to LOG in");
            oos.writeObject(msBienvenida);  //concentra la escritura de mensajes ¿bueno?
            System.out.println("Sirviente: "+ns+": [RESP: "+msBienvenida+"]");

            while (true) {
                String mensaje;  //String multipurpose
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
                    
                    if (logins.isIPBlocked(client.getHostAddress())){
                        ms = new MensajeProtocolo(Primitiva.ERROR, "Err Max Number of login attempts reached.");
                    }

                    if (validateCredentials(usr,pswd)){
                        this.usrLogged = true;
                        ms = new MensajeProtocolo(Primitiva.XAUTH, "User successfully logged");
                    }
                    else{
                        logins.incrementCount(client.getHostAddress());
                        ms = new MensajeProtocolo(Primitiva.NOTAUTH, "Err 401 ~ Credentials DO NOT MATCH. Try again" );
                    }

                break;
                case ADD2L:
                    if (this.usrLogged){
                        String key = me.getIdCola();
                        String val = me.getMensaje();
                        synchronized (mapa) {
                            mapa.push(key, val);
                        }
                        ms = new MensajeProtocolo(Primitiva.ADDED);
                    }
                    else ms = new MensajeProtocolo(Primitiva.NOTAUTH,"User login is required");
                break;
                case READL:
                    if(this.usrLogged){
                        String key = me.getIdCola();
                        String men = null;
                        synchronized (mapa) {
                            men = mapa.pop(key);
                        }
                        if( null != men ){
                            ms = new MensajeProtocolo(Primitiva.MEDIA, men);
                        }else {
                            ms = new MensajeProtocolo(Primitiva.EMPTY);
                        }
                    }
                    else ms = new MensajeProtocolo(Primitiva.XAUTH, "["+ns+":"+socket+"]");
                break;

                case DELETEL:
                    if(this.usrLogged){
                        String key = me.getIdCola();
                        boolean valor = false;
                        //Se comprueba que la clave existe
                        if(mapa.containsKey(key)){
                            synchronized (mapa) {
                                valor = mapa.remove(key);
                                ms = new MensajeProtocolo(Primitiva.DELETED);
                            }
                        }else {
                            ms = new MensajeProtocolo(Primitiva.EMPTY);
                        }
                        if(!valor){ // Sino se consigue eliminar
                            ms = new MensajeProtocolo(Primitiva.EMPTY);
                        }

                    } else ms = new MensajeProtocolo(Primitiva.XAUTH, "["+ns+":"+socket+"]");
                break;

                default:
                    //TODO no se que error poner aqui
                    ms = new MensajeProtocolo(Primitiva.ERROR,"Err ");
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