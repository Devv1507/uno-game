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
     */
    private final Stack<Card> descartadas = new Stack<>();
    /**
     * Agrega una carta a la pila de descarte.
     *
     * @param carta Carta que se va a descartar.
     */
    public void descartar(Card carta) {
        descartadas.push(carta);
    }
    /**
     * Retorna la carta que esta en la parte superior de la pila de descarte,
     * sin retirarla.
     *
     * @return Carta superior de la pila de descarte.
     */
    public Card cartaSuperior() {
        return descartadas.peek();
    }
    /**
     * Recicla las cartas de la pila de descarte cuando el mazo se queda sin cartas.
     * Retira temporalmente la carta superior, recoge el resto de cartas para reutilizarlas
     * y vuelve a colocar la carta superior en la pila.
     *
     * @return Lista de cartas recicladas listas para volver al mazo.
     */
    public List<Card> reciclarMazo() {
        if (descartadas.size() <= 1) {
            return new ArrayList<>();
        }
        // Guarda la carta superior, y la elimina de la pila para evitar duplicados
        Card ultima = descartadas.pop();

        List<Card> recicladas = new ArrayList<>(descartadas);
        descartadas.clear();  //limpia la pila
        descartadas.push(ultima); // vuelve a colocar la carta superior

        return recicladas;
    }
}
