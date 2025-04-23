package univalle.tedesoft.uno.model.Decks;

import univalle.tedesoft.uno.model.Cards.Card;
import java.util.Stack;

public class DiscardPile {
    private final Stack<Card> descartadas = new Stack<>();

    public void descartar(Card carta) {
        descartadas.push(carta);
    }
    public Card cartaSuperior() {
        return descartadas.peek();
    }
    /*
    import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class DiscardPile {
    private final Stack<Card> descartadas = new Stack<>();

    public void descartar(Card carta) {
        descartadas.push(carta);
    }

    public Card cartaSuperior() {
        return descartadas.peek();
    }

    public List<Card> reciclarMazo() {
        if (descartadas.size() <= 1) return new ArrayList<>();

        // Guarda la carta superior
        Card ultima = descartadas.pop();

        List<Card> recicladas = new ArrayList<>(descartadas);
        descartadas.clear();
        descartadas.push(ultima); // vuelve a colocar la carta superior

        return recicladas;
    }
}

     */
}
