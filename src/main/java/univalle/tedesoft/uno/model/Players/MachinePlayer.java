package univalle.tedesoft.uno.model.Players;

import univalle.tedesoft.uno.model.Cards.Card;
import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.State.IGameState;

import java.util.Random;

/**
 * Clase que representa al jugador maquina.
 *
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class MachinePlayer extends Player {
    private final Random random;

    public MachinePlayer() {
        this.name = "Machine";
        this.random = new Random();
    }

    /**
     * Simula que la maquina elija aleatoriamente un color.
     * @return El color elegido aleatoriamente.
     */
    public Color chooseColor() {
        Color[] possibleColors = {Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE};
        int index = this.random.nextInt(possibleColors.length);
        return possibleColors[index];
    }

    /**
     * Con un bucle for-each, busca una carta en la mano que sea valida.
     * @param gameState estado actual del juego (decks, players, mano disponible, etc)
     * @return card viable a jugar.
     */
    public Card chooseCardToPlay(IGameState gameState) {
        // encontrar la primera carta jugable
        for (Card card : this.cards) {
            if (gameState.isValidPlay(card)) {
                return card;
            }
        }
        return null;
    }
}
