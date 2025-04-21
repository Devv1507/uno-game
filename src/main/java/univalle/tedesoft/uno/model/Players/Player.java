package univalle.tedesoft.uno.model.Players;

import univalle.tedesoft.uno.model.Cards.Card;
import java.util.ArrayList;
import java.util.List;

public abstract class Player {
    public List<Card> cards = new ArrayList<>();

    public Player(){}

    public void addCard(Card card){
        cards.add(card);
    }

    public void removeCard(Card card){
        cards.remove(card);
    }

    public int getNumeroCartas() {
        return cards.size();
    }
}
