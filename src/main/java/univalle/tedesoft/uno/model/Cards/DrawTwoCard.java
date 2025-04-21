package univalle.tedesoft.uno.model.Cards;

import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;
import univalle.tedesoft.uno.model.Players.Player;

public class DrawTwoCard extends ActionCard {

    public DrawTwoCard(Color color) {
        super(color, Value.DRAW_TWO);
    }

    public void appliedEffect() {
        System.out.println("¡El oponente roba 2 cartas!");
    }
}