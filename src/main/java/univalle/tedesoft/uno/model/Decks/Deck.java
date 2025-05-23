package univalle.tedesoft.uno.model.Decks;

import java.util.Collections;
import java.util.LinkedList;

import univalle.tedesoft.uno.exceptions.EmptyDeckException;
import univalle.tedesoft.uno.model.Cards.*;
import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

/**
 * Clase que representa el mazo principal del juego UNO.
 * Contiene todas las cartas iniciales necesarias para jugar, incluyendo cartas numéricas,
 * de acción (+2, reverse, skip) y comodines (wild, +4).
 * El mazo se inicializa en el constructor, se baraja automáticamente y permite tomar cartas.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class Deck {
    /** Lista que contiene las cartas del mazo */
    private final LinkedList<Card> cards;
    /**
     * Constructor del mazo. Inicializa el mazo con las cartas estandar de UNO
     * y luego lo baraja aleatoriamente.
     */
    public Deck(){
        cards = new LinkedList<>();
        intializeDeck();
        shuffle();
    }
    /**
     * Metodo privado que genera todas las cartas necesarias para el mazo.
     * Se agregan cartas numericas del 0 al 9 y cartas de acción (+2, reverse, skip)
     * para cada color excepto WILD. Luego se agregan los comodines.
     * Tambien imprime todas las cartas generadas para fines de prueba.
     */
    private void intializeDeck() {
        for (int c = 0; c < Color.values().length; c++) {
            if (c == Color.WILD.ordinal()){
                continue;
            }
            cards.add(new DrawTwoCard(Color.values()[c]));
            cards.add(new DrawTwoCard(Color.values()[c]));
            cards.add(new SkipCard(Color.values()[c]));
            // Cartas numéricas 0-9, +2, reverse y skip, 14 cartas por cada color
            for(int v = 0; v < Value.values().length; v++){
                if(v <= Value.NINE.ordinal()){
                    cards.add(new NumberCard(Color.values()[c], Value.values()[v]));
                }
            }
        }
        // Comodines (wild +4 y wild color change)
        for (int i = 0; i < 4; i++) {
            cards.add(new WildCard());
            cards.add(new WildDrawFourCard());
        }
    }
    /**
     * Retorna la lista de cartas actuales del mazo.
     *
     * @return LinkedList con las cartas del mazo.
     */
    public LinkedList<Card> getCards() {
        return cards;
    }
    /**
     * Baraja aleatoriamente las cartas del mazo.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }
    /**
     * Toma la primera carta del mazo (parte superior).
     *
     * @return La carta retirada del mazo o null si está vacío.
     * @throws EmptyDeckException si el mazo está vacío.
     */
    public Card takeCard() throws EmptyDeckException {
        if (this.cards.isEmpty()) {
            throw new EmptyDeckException("No se pueden tomar más cartas, el mazo principal está vacío.");
        }
        return cards.poll();
    }

    /**
     * Trae el numero de cartas dentro del arreglo.
     * @return numero de cartas en el deck
     */
    public int getNumeroCartas() {
        return cards.size();
    }
}
