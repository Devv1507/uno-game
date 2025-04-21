package univalle.tedesoft.uno.model.Cards;

import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

public class SkipCard extends ActionCard{
    public SkipCard(Color color) {
        super(color, Value.SKIP);
    }

    public void appliedEffect() {
        System.out.println("¡El oponente te deja sin turno!");
    }
}
