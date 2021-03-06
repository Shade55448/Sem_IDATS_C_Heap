package kolekce;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tomáš Vondra
 * @param <E>
 */
public class AbstrHeap<E> implements AbstrHeapInterface<E> {

    private static final int DEFAULTNI_VELIKOST = 15;

    private final Comparator<E> comparator;
    private E[] prvky;
    private int velikost;
    private int pocetPrvku;

    public AbstrHeap(Comparator<E> comparator) {
        this(comparator, DEFAULTNI_VELIKOST);
    }

    public AbstrHeap(Comparator<E> comparator, int velikost) {
        this.comparator = comparator;
        prvky = (E[]) new Object[velikost + DEFAULTNI_VELIKOST];
        this.velikost = velikost + DEFAULTNI_VELIKOST;
        pocetPrvku = 0;
         //Načítáme o Defaultní velikost víc, aby jsme měli vždy dostatečně velké pole když voláme pomocné metody(poziceLevehoDitete..)
    }

    @Override
    public void vybuduj(E[] items) {
        prvky = (E[]) new Object[items.length + DEFAULTNI_VELIKOST];

        System.arraycopy(items, 0, prvky, 0, items.length);
        velikost = items.length + DEFAULTNI_VELIKOST;
        pocetPrvku = items.length;

        prebuduj();
    }

    @Override
    public void prebuduj() {
        for (int pozice = (pocetPrvku / 2); pozice >= 0; pozice--) {
            prebuduj(pozice);
        }
    }

    @Override
    public void zrus() {
        prvky = (E[]) new Object[DEFAULTNI_VELIKOST];
        velikost = DEFAULTNI_VELIKOST;
        pocetPrvku = 0;
    }

    @Override
    public boolean jePrazdny() {
        return pocetPrvku == 0;
    }

    @Override
    public void vloz(E item) {
        if (pocetPrvku >= velikost) {
            zvetsiPole();
        } //Pokud je pole moc malé, zdvojnásobíme ho

        int aktualni = pocetPrvku;
        prvky[pocetPrvku++] = item;

        if (pocetPrvku > 1) { //Pro jeden prvek nemá cenu řešit
            while (comparator.compare(prvky[aktualni], prvky[poziceRodice(aktualni)]) < 0) {
                prohod(aktualni, poziceRodice(aktualni));
                aktualni = poziceRodice(aktualni);
            } //Dokud je vzdálenost menší než rodič, prohazuj
        } 

    }

    @Override
    public E odeberMax() throws KolekceException {
        if (pocetPrvku == 0) {
            throw new KolekceException("Prázdná halda");
        }

        E prvek = prvky[0];
        prvky[0] = prvky[pocetPrvku - 1];
        prvky[pocetPrvku - 1] = null;
        pocetPrvku--;
        prebuduj(0); //Odeber max a znova přestav pole
        return prvek;
    }

    @Override
    public E zpristupniMax() throws KolekceException {
        if (pocetPrvku == 0) {
            throw new KolekceException("Prázdná halda");
        }

        return prvky[0];
    }

    @Override
    public Iterator<E> iterator() {
        return new BreathIterator();
    }

    private void zvetsiPole() {
        velikost = velikost * 2;
        E[] temp = (E[]) new Object[velikost];
        System.arraycopy(prvky, 0, temp, 0, pocetPrvku);
        prvky = temp;
    }

    private void prohod(int pozice1, int pozice2) {
        E temp;
        temp = prvky[pozice1];
        prvky[pozice1] = prvky[pozice2];
        prvky[pozice2] = temp;
    }

    private int poziceRodice(int pozice) {
        return ((pozice - 1) / 2); //Pozice rodiče od dané pozice
    }

    private int poziceLevehoDitete(int pozice) {
        return (pozice * 2) + 1; //Pozice levehoDitete od dané pozice
    }

    private int pozicePravehoDitete(int pozice) {
        return poziceLevehoDitete(pozice) + 1; //Pozice pravehoDitete od dané pozice
    }

    private boolean jeList(int pozice) {
        return pozice >= (pocetPrvku / 2) && pozice <= pocetPrvku; //Zjistí, jestli se jedná o list stromu
    }

    private void prebuduj(int pozice) {
        int poziceNejmensihoPrvku = pozice;
        if (!jeList(pozice)) {
            if (poziceLevehoDitete(pozice) < pocetPrvku
                    && comparator.compare(prvky[pozice], prvky[poziceLevehoDitete(pozice)]) > 0) {
                poziceNejmensihoPrvku = poziceLevehoDitete(pozice);
            } //Pokud je poziceLevehoDitete v poli a pozice prvku je vzdálenější než levé dítě
            if (pozicePravehoDitete(pozice) < pocetPrvku
                    && comparator.compare(prvky[pozice], prvky[pozicePravehoDitete(pozice)]) > 0) {
                poziceNejmensihoPrvku = pozicePravehoDitete(pozice);
            } //Pokud je pozicePravehoDitete v poli a pizice prvku je vzdálenější než pravé dítě

            if (poziceNejmensihoPrvku != pozice) {
                prohod(pozice, poziceNejmensihoPrvku);
                prebuduj(poziceNejmensihoPrvku);
            } //Prohoď
        }
    }

    private class BreathIterator implements Iterator<E> {

        //Inspirováno sem B. In-Order do hloubky
        private AbstrFIFOInterface<E> fronta = null;
        private int currentPosition;

        public BreathIterator() {

            try {
                fronta = new AbstrFIFO<>();
                if (prvky[0] != null) {
                    fronta.vloz(prvky[0]);
                    currentPosition = 0;
                } //Pokud není prázdný, vlož kořen
            } catch (KolekceException ex) {
                Logger.getLogger(AbstrHeap.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public boolean hasNext() {
            return !fronta.jePrazdny();
        }

        @Override
        public E next() {
            if (hasNext()) {
                try {
                    E out = fronta.odeber();
                    if (prvky[poziceLevehoDitete(currentPosition)] != null) {
                        fronta.vloz(prvky[poziceLevehoDitete(currentPosition)]);
                    } //Pokud pozice leveho ditete není prázdná
                    if (prvky[pozicePravehoDitete(currentPosition)] != null) {
                        fronta.vloz(prvky[pozicePravehoDitete(currentPosition)]);
                    } //Pokud pozice pravého dítěte není prázdná
                    
                    if(!jeList(currentPosition)){
                        currentPosition++;
                    } //Pokud se nejedná o list, poskoč

                    return out;
                } catch (KolekceException ex) {
                    Logger.getLogger(AbstrHeap.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
            throw new NoSuchElementException();
        }
    }
}
