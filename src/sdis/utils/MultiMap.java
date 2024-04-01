package sdis.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentLinkedQueue;
/**
* Multimap concurrente para la práctica de Distribuidos de 2019.
*/
public class MultiMap<K, T> {
    private final ConcurrentMap<K, ConcurrentLinkedQueue<T>>  map = new ConcurrentHashMap<K, ConcurrentLinkedQueue<T>> ();

    public synchronized void push(K clave, T valor) {
        java.util.Queue<T> cola = map.get(clave);
        if (null == cola) {
        //putIfAbsent es atómica pero requiere "nueva", y es costoso
        ConcurrentLinkedQueue<T> nueva = new ConcurrentLinkedQueue<T>();
        ConcurrentLinkedQueue<T> previa = map.putIfAbsent(clave, nueva);
        cola = (null == previa) ? nueva : previa ;
        }
        cola.add(valor);
    }

    public T pop(K clave) {
        ConcurrentLinkedQueue<T> cola = map.get(clave);
        return (null != cola) ? cola.poll() : null ;
    }

    // Método para verificar si una clave existe en el mapa
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    /*
        Metodo para eliminar una clave y sus valores asociados

        Devuelve true si consigue borrar y false sino consigue borrar
     */

    public synchronized boolean remove(K key) {
        boolean valor = false;
        ConcurrentLinkedQueue<T> m = map.remove(key);
        if (m != null){
            valor = true;
        }
        return valor;
    }
}
