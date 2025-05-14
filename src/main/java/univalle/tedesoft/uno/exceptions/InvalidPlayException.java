package univalle.tedesoft.uno.exceptions;

import univalle.tedesoft.uno.model.Cards.Card;

public class InvalidPlayException extends Exception {
    private final Card attemptedCard;
    private final Card topDiscardCard;

    public InvalidPlayException(String message, Card attemptedCard, Card topDiscardCard) {
        super(message);
        this.attemptedCard = attemptedCard;
        this.topDiscardCard = topDiscardCard;
    }

    public Card getAttemptedCard() {
        return this.attemptedCard;
    }

    public Card getTopDiscardCard() {
        return this.topDiscardCard;
    }
}
