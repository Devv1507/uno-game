package univalle.tedesoft.uno.model.Players;

import java.util.ArrayList;
import java.util.List;
import univalle.tedesoft.uno.model.Cards.Card;

/**
 * Clase abstracta que representa un jugador en el juego UNO.
 * Contiene la lista de cartas del jugador y metodos basicos para
 * agregar, remover y contar cartas en su mano.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public abstract class Player {

    /** Lista de cartas que tiene el jugador en su mano */
    public List<Card> cards = new ArrayList<>();

    /**
     * Constructor por defecto del jugador.
     */
    public Player(){}

    /**
     * Agrega una carta a la mano del jugador.
     *
     * @param card Carta que se agregara a la mano
     */
    public void addCard(Card card){
        cards.add(card);
    }

    /**
     * Remueve una carta de la mano del jugador.
     *
     * @param card Carta que se eliminara de la mano
     */
    public void playCard(Card card){
        cards.remove(card);
    }
    /**
     * Devuelve la cantidad actual de cartas en la mano del jugador.
     *
     * @return Numero de cartas en la mano
     */
    public int getNumeroCartas() {
        return cards.size();
    }
}
