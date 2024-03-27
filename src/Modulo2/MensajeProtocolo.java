package Modulo2;

public class MensajeProtocolo implements java.io.Serializable {
    private final Primitiva primitiva;
    private final String mensaje;
    private final String idCola;

    /**
     * Constructor para XAUTH y ADD2L
     * @param p, tipo de primitiva
     * @param mensaje, mensaje que contendrá usuario y contraseña.
     */
    public MensajeProtocolo(Primitiva p, String mensaje, String idCola) throws MalMensajeProtocoloException {
            if (p == Primitiva.XAUTH){
                this.primitiva = p;
                this.mensaje = mensaje;
                this.idCola = idCola;
            }
            else throw new MalMensajeProtocoloException();
    }

    /**
     * Constructor para ADDED e INFO
     * @param p
     * @throws MalMensajeProtocoloException
     */
    public MensajeProtocolo(Primitiva p) throws MalMensajeProtocoloException {
        if (p  == Primitiva.ERROR || p == Primitiva.ADDED) {
        this.primitiva = p;
        this.mensaje = this.idCola = null;
        }
        else if (p == Primitiva.INFO){
            this.primitiva = p;
            this.mensaje = "Welcome, please type your credentials to LOG in";
            this.idCola = null;
        }
        else throw new MalMensajeProtocoloException();
    }

    /**
     * Constructor para NOTAUTH ,ERROR y XAUTH confirmación
     * @param p
     * @param mensaje
     * @throws MalMensajeProtocoloException
     */
    public MensajeProtocolo(Primitiva p, String mensaje) throws MalMensajeProtocoloException{
        if (p == Primitiva.NOTAUTH || p == Primitiva.ERROR || p == Primitiva.XAUTH){
            this.primitiva = p;
            this.mensaje = mensaje;
            this.idCola = null;
        } 
        else throw new MalMensajeProtocoloException();
    }


    public Primitiva getPrimitiva() { return this.primitiva; }

    public String getMensaje() { return this.mensaje; }

    public String getIdCola() { return this.idCola; }

    public String toString() {  //prettyPrinter de la clase
        switch (this.primitiva) {
            case XAUTH:
                return this.primitiva+":"+this.mensaje ;
            case ADD2L:
                return this.primitiva+":"+this.idCola+":"+this.mensaje;
            default :
        return this.primitiva.toString() ;
    }
    }
}
