package Modulo2;

class Sirviente implements Runnable {
    private final java.net.Socket socket;
    private final MultiMap<String, String> mapa;
    private final java.io.ObjectOutputStream oos;
    private final java.io.ObjectInputStream ois;
    private final int ns;
    private static java.util.concurrent.atomic.AtomicInteger nInstance = new java.util.concurrent.atomic.AtomicInteger();

    Sirviente(java.net.Socket s, MultiMap<String, String> c) throws java.io.IOException {
        this.socket = s;
        this.mapa = c;
        this.ns = nInstance.getAndIncrement();
        this.oos = new java.io.ObjectOutputStream(socket.getOutputStream());
        this.ois = new java.io.ObjectInputStream(socket.getInputStream());
    }  //se invoca en el Servidor, usualmente

    public void run() {
        try {
            while (true) {
                String mensaje;  //String multipurpose
                MensajeProtocolo me = (MensajeProtocolo) ois.readObject();
                MensajeProtocolo ms;
                //me y ms: mensajes entrante y saliente
                System.out.println("Sirviente: "+ns+": [ME: "+ me+"]");  //depuracion me

                switch (me.getPrimitiva()) {
                case INFO:
                    ms = new MensajeProtocolo(Primitiva.XAUTH, "["+ns+":"+socket+"]");
                break;
                case XAUTH:
                    ms = new MensajeProtocolo(Primitiva.XAUTH, "["+ns+":"+socket+"]");
                break;
                case ADD2L:
                    mapa.push(me.getIdCola(), me.getMensaje());
                    synchronized (mapa) {
                        mapa.notify();
                    }  // despierta un sirviente esperando en un bloqueo de "mapa"
                    ms = new MensajeProtocolo(Primitiva.ADD2L);
                break;
                case READL:
                    ms = new MensajeProtocolo(Primitiva.XAUTH, "["+ns+":"+socket+"]");
                break;
                case MEDIA:
                    ms = new MensajeProtocolo(Primitiva.XAUTH, "["+ns+":"+socket+"]");
                break;
                case EMPTY:
                    ms = new MensajeProtocolo(Primitiva.XAUTH, "["+ns+":"+socket+"]");
                break;
                case DELETEL:
                    ms = new MensajeProtocolo(Primitiva.XAUTH, "["+ns+":"+socket+"]");
                break;
                case DELETED:
                    ms = new MensajeProtocolo(Primitiva.XAUTH, "["+ns+":"+socket+"]");
                break;
                case NOTAUTH:
                    ms = new MensajeProtocolo(Primitiva.XAUTH, "["+ns+":"+socket+"]");
                break;
                case ERROR:
                    ms = new MensajeProtocolo(Primitiva.XAUTH, "["+ns+":"+socket+"]");
                break;
                default:
                    ms = new MensajeProtocolo(Primitiva.ERROR);
                }  //fin del selector segun el mensaje entrante
                oos.writeObject(ms);  //concentra la escritura de mensajes ¿bueno?
                //depuracion de mensaje saliente
                System.out.println("Sirviente: "+ns+": [RESP: "+ms+"]");
            }
        } catch (java.io.IOException e) {
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
}