package kolekce;

/**
 *
 * @author user
 * @param <E>
 */
public interface AbstrHeapInterface<E> extends Iterable<E> {

    public void vybuduj(E[] items);

    public void prebuduj();

    public void zrus();

    public boolean jePrazdny();

    public void vloz(E item);

    public E odeberMax() throws KolekceException;

    public E zpristupniMax() throws KolekceException;

}
