package univalle.tedesoft.uno.model.State;

import univalle.tedesoft.uno.model.Cards.Card;
import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Players.Player;

/**
 * TODO: esta debe ser la clase principal que representa el estado del juego
 * Debe orquestar los turnos, el mazo, los jugadores y la pila de descarte.
 */
public class GameState implements IGameState {

    public void onGameStart(GameState game) {
    }

    public void onTurnChanged(Player currentPlayer) {
    }

    public void onCardPlayed(Player pLayer, Card card) {
    }

    public void onPlayerDrewCard(Player player, Card drawnCard){}

    public void onHandChanged(Player player) {}

    public void onForceDraw(Player player, int numberOfCards) {}

    public void onPlayerSkipped(Player player) {}

    public void onMustChooseColor(Player player) {}

    public void onColorChosen(Color color) {}

    public void onUnoStateChanged(Player player, boolean hasOneCard) {}

    public void onUnoDeclared(Player player, boolean success){}

    public void onGameOver(Player player) {}

    public void onDeckShuffled() {}
}
