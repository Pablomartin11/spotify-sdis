package sdis.spotify.common;

public class MensajeProtocolo implements java.io.Serializable {
    private final Primitiva primitiva;
    private final String mensaje;
    private final String idCola;

    /**
     * Constructor para XAUTH y ADD2L
     * @param p, tipo de primitiva
     * @param mensaje, mensaje que contendrá usuario y contraseña.
     */
    public MensajeProtocolo(Primitiva p,String idCola, String mensaje ) throws MalMensajeProtocoloException {
            if (p == Primitiva.XAUTH || p== Primitiva.ADD2L){
                this.primitiva = p;
                this.mensaje = mensaje;
                this.idCola = idCola;
            }
            else throw new MalMensajeProtocoloException();
    }

    /**
     * Constructor para ADDED, EMPTY, DELETED
     * @param p
     * @throws MalMensajeProtocoloException
     */
    public MensajeProtocolo(Primitiva p) throws MalMensajeProtocoloException {
        if (p == Primitiva.ADDED || p == Primitiva.EMPTY || p == Primitiva.DELETED ) {
            this.primitiva = p;
            this.mensaje = this.idCola = null;
        }
        else throw new MalMensajeProtocoloException();
    }

    /**
     * Constructor para INFO, READL, MEDIA, DELETEL, NOTAUTH ,ERROR y XAUTH confirmación
     * @param p
     * @param mensaje
     * @throws MalMensajeProtocoloException
     */
    public MensajeProtocolo(Primitiva p, String mensaje) throws MalMensajeProtocoloException{
        if (p== Primitiva.INFO|| p == Primitiva.XAUTH || p == Primitiva.MEDIA || p == Primitiva.NOTAUTH || p == Primitiva.ERROR ){
            this.primitiva = p;
            this.mensaje = mensaje;
            this.idCola = null;
        } else if(p == Primitiva.READL || p == Primitiva.DELETEL){
            this.primitiva = p;
            this.mensaje = null;
            this.idCola = mensaje;
        }
        else throw new MalMensajeProtocoloException();
    }



    public Primitiva getPrimitiva() { return this.primitiva; }

    public String getMensaje() { return this.mensaje; }

    public String getIdCola() { return this.idCola; }

    public String toString() {  //prettyPrinter de la clase
        switch (this.primitiva) {
            case XAUTH:
            case INFO:
            case MEDIA:
            case NOTAUTH:
            case ERROR:
                return this.primitiva+":"+this.mensaje ;
            case ADD2L:
                return this.primitiva+":"+this.idCola+":"+this.mensaje;
            case READL:
            case DELETEL:
                return this.primitiva+":"+this.idCola;
            default :
        return this.primitiva.toString() ;
    }
    }
}
