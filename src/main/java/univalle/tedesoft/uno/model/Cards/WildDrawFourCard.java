package univalle.tedesoft.uno.model.Cards;

import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

public class WildDrawFourCard extends ActionCard {
    public WildDrawFourCard(Color color) {
        super(color, Value.SKIP);
    }

    public void appliedEffect() {
        System.out.println("¡El oponente te regala 4 cartas más!");
    }
}
