package univalle.tedesoft.uno.model.Players;

import java.util.ArrayList;
import java.util.List;
import univalle.tedesoft.uno.model.Cards.Card;

/**
 * Clase que representa un jugador en el juego UNO.
 * Contiene la lista de cartas del jugador y métodos básicos para
 * agregar, remover y contar cartas en su mano.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class Player {

    /** Lista de cartas que tiene el jugador en su mano */
    public List<Card> cards = new ArrayList<>();
    /** Nombre del jugador. */
    public String name;
    /** Indicador para un jugador que tiene la ventana de oportunidad para declarar "UNO". */
    private boolean isUnoCandidate = false;
    /** Indicar para un jugador que efectivamente declaró "UNO" durante su ventana de oportunidad. */
    private boolean hasDeclaredUnoThisTurn = false;

    /**
     * Constructor por defecto del jugador.
     */
    public Player(){}

    /**
     * Agrega una carta a la mano del jugador.
     * @param card Carta que se agregara a la mano
     */
    public void addCard(Card card){
        cards.add(card);
    }

    /**
     * Remueve una carta de la mano del jugador.
     * @param card Carta que se eliminara de la mano
     */
    public void removeCardOfCards(Card card){
        cards.remove(card);
    }
    /**
     * Devuelve la cantidad actual de cartas en la mano del jugador.
     * @return Numero de cartas en la mano
     */
    public int getNumeroCartas() {
        return cards.size();
    }

    /**
     * Devuelve el arreglo de cartas almacenadas en el player.
     * @return arreglo de cartas.
     */
    public List<Card> getCards() {
        return cards;
    }

    /**
     * Devuelve el nombre del jugador.
     * @return el nombre del jugador
     */
    public String getName() { return this.name; }

    /**
     * Establece el nombre del jugador.
     * @param name El nuevo nombre del jugador
     */
    public void setName(String name) { this.name = name; }

    /**
     * Elimina todas las cartas de la mano del jugador.
     */
    public void clearHand() {
        this.cards.clear();
    }

    /**
     * Verifica si el jugador es actualmente un candidato para declarar "UNO".
     * (Tiene 1 carta y está en la ventana de oportunidad).
     * @return true si es candidato a UNO, false en caso contrario.
     */
    public boolean isUnoCandidate() {
        return this.isUnoCandidate;
    }

    /**
     * Establece si el jugador es un candidato para declarar "UNO".
     * @param unoCandidate true si es candidato, false en caso contrario.
     */
    public void setUnoCandidate(boolean unoCandidate) {
        this.isUnoCandidate = unoCandidate;
    }

    /**
     * Verifica si el jugador ha declarado "UNO" en la oportunidad actual.
     * @return true si ya declaró UNO, false en caso contrario.
     */
    public boolean hasDeclaredUnoThisTurn() {
        return this.hasDeclaredUnoThisTurn;
    }

    /**
     * Establece si el jugador ha declarado "UNO" en la oportunidad actual.
     * @param hasDeclaredUnoThisTurn true si declaró UNO, false en caso contrario.
     */
    public void setHasDeclaredUnoThisTurn(boolean hasDeclaredUnoThisTurn) {
        this.hasDeclaredUnoThisTurn = hasDeclaredUnoThisTurn;
    }

    /**
     * Resetea los estados relacionados con la declaración de "UNO" del jugador.
     */
    public void resetUnoStatus() {
        this.isUnoCandidate = false;
        this.hasDeclaredUnoThisTurn = false;
    }
}
