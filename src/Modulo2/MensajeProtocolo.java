package Modulo2;

public class MensajeProtocolo implements java.io.Serializable {
    private final Primitiva primitiva;
    private final String mensaje;
    private final String idCola;

    /**
     * Constructor para XAUTH
     * @param p, tipo de primitiva
     * @param mensaje, mensaje que contendrá usuario y contraseña.
     */
    public MensajeProtocolo(Primitiva p, String mensaje) throws MalMensajeProtocoloException {
            if (p == Primitiva.XAUTH){
                this.primitiva = p;
                this.mensaje = mensaje;
                this.idCola = null;
            }
        else throw new MalMensajeProtocoloException();
    }

    public MensajeProtocolo(Primitiva p) throws MalMensajeProtocoloException {
        if (p  == Primitiva.ERROR) {
        this.primitiva = p;
        this.mensaje = this.idCola = null;
        } else
        throw new MalMensajeProtocoloException();
    }

    public Primitiva getPrimitiva() { return this.primitiva; }

    public String getMensaje() { return this.mensaje; }

    public String getIdCola() { return this.idCola; }

    public String toString() {  //prettyPrinter de la clase
        switch (this.primitiva) {
            case XAUTH:
                return this.primitiva+":"+this.mensaje ;
            default :
        return this.primitiva.toString() ;
    }
    }
}
