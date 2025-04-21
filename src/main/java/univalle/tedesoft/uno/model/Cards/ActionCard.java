package univalle.tedesoft.uno.model.Cards;

import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;
import univalle.tedesoft.uno.model.Players.Player;

public abstract class ActionCard extends Card {
    public ActionCard(Color color, Value value) {
        super(color, value);
    }

    public abstract void appliedEffect();
}