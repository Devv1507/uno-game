package univalle.tedesoft.uno.exceptions;

import univalle.tedesoft.uno.model.Cards.Card;

/**
 * Excepción lanzada cuando un jugador intenta realizar una jugada que no es válida
 * según las reglas actuales del juego UNO (ej. la carta no coincide en color o valor
 * con la carta superior de la pila de descarte).
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class InvalidPlayException extends Exception {
    /** La carta que el jugador intentó jugar. */
    private final Card attemptedCard;
    /** La carta que se encontraba en la cima de la pila de descarte en el momento del intento. */
    private final Card topDiscardCard;

    /**
     * Constructor para crear una nueva InvalidPlayException.
     * @param message El mensaje detallado que describe la razón de la invalidez.
     * @param attemptedCard La carta que se intentó jugar.
     * @param topDiscardCard La carta en la cima de la pila de descarte contra la cual se validó la jugada.
     */
    public InvalidPlayException(String message, Card attemptedCard, Card topDiscardCard) {
        super(message);
        this.attemptedCard = attemptedCard;
        this.topDiscardCard = topDiscardCard;
    }

    /**
     * Obtiene la carta que el jugador intentó jugar.
     * @return La carta que causó la excepción.
     */
    public Card getAttemptedCard() {
        return this.attemptedCard;
    }

    /**
     * Obtiene la carta que estaba en la cima de la pila de descarte cuando se intentó la jugada.
     * @return La carta superior de la pila de descarte en el momento de la jugada inválida.
     */
    public Card getTopDiscardCard() {
        return this.topDiscardCard;
    }
}
