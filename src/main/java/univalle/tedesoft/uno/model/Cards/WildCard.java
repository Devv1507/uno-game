package univalle.tedesoft.uno.model.Cards;

import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

public class WildCard extends ActionCard {
    public WildCard(Color color) {
        super(color, Value.WILD);
    }

    public void appliedEffect() {
        System.out.println("¡Han cambiado el color del  juego!");
    }
}
