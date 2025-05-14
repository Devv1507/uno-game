package univalle.tedesoft.uno.model.Decks;

import univalle.tedesoft.uno.model.Cards.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Representa la pila de descarte en el juego de UNO.
 * Almacena las cartas que han sido jugadas, manteniendo
 * la ultima carta jugada en la parte superior.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class DiscardPile {
    /**
     * Pila de cartas descartadas. La parte superior contiene la ultima carta jugada.
     * Se inicializa sin un constructor, se usa el que brinda java por defecto.
     */
    private final Stack<Card> discarded = new Stack<>();
    /**
     * Agrega una carta a la pila de descarte.
     *
     * @param card Carta que se va a descartar.
     */
    public void discard(Card card) {
        if (card != null) {
            this.discarded.push(card);
        }
    }
    /**
     * Retorna la carta que esta en la parte superior de la pila de descarte,
     * sin retirarla.
     *
     * @return Carta superior de la pila de descarte.
     */
    public Card SuperiorCard() {
        if (this.discarded.isEmpty()) {
            return null;
        }
        return discarded.peek();
    }
    /**
     * Recicla las cartas de la pila de descarte cuando el mazo se queda sin cartas.
     * Retira temporalmente la carta superior, recoge el resto de cartas para reutilizarlas
     * y vuelve a colocar la carta superior en la pila.
     *
     * @return Lista de cartas recicladas listas para volver al mazo.
     */
    public List<Card> recycleDeck() {
        if (discarded.size() <= 1) {
            return new ArrayList<>();
        }
        // Guarda la carta superior, y la elimina de la pila para evitar duplicados
        Card endCard = discarded.pop();

        List<Card> recyclesCards = new ArrayList<>(discarded);
        discarded.clear();  //limpia la pila
        discarded.push(endCard); // vuelve a colocar la carta superior

        return recyclesCards;
    }
}
