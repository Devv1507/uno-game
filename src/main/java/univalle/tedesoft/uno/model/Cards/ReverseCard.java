package univalle.tedesoft.uno.model.Cards;

import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

public class ReverseCard extends ActionCard{
    public ReverseCard(Color color) {
        super(color, Value.REVERSE);
    }

    public void appliedEffect() {
        System.out.println("¡El oponente revierte el sentido del juego!");
    }
}
