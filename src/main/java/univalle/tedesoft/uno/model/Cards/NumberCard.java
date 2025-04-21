package univalle.tedesoft.uno.model.Cards;

import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

public class NumberCard extends Card {
    public NumberCard(Color color, Value value) {
        super(color, value);  // value será por ejemplo Value.FIVE
    }
}