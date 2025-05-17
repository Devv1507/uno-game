package univalle.tedesoft.uno.model.Decks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import univalle.tedesoft.uno.model.Cards.Card;
import univalle.tedesoft.uno.model.Cards.NumberCard;
import univalle.tedesoft.uno.model.Cards.WildCard;
import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

import java.util.List;
//import java.util.ArrayList;
import java.util.Stack;

/**
 * Pruebas unitarias para la clase DiscardPile.
 * Estas pruebas verifican la funcionalidad de descartar cartas,
 * obtener la carta superior y reciclar el mazo.
 */
class DiscardPileTest {
    /** Pila de descarte utilizada para las pruebas. */
    private DiscardPile discardPile;
    /** Carta de ejemplo: Número 5 Rojo. */
    private Card cardRed5;
    /** Carta de ejemplo: Número 3 Azul. */
    private Card cardBlue3;
    /** Carta de ejemplo: Salto Verde. */
    private Card cardGreenSkip;
    /** Carta de ejemplo: Comodín. */
    private Card cardWild;

    /**
     * Metodo de configuracion que se ejecuta antes de cada prueba.
     * Inicializa una nueva instancia de {@link DiscardPile} y algunas
     * cartas de ejemplo.
     */
    @BeforeEach
    void setUp() {
        discardPile = new DiscardPile();
        cardRed5 = new NumberCard(Color.RED, Value.FIVE);
        cardBlue3 = new NumberCard(Color.BLUE, Value.THREE);
        cardGreenSkip = new univalle.tedesoft.uno.model.Cards.SkipCard(Color.GREEN);
        cardWild = new WildCard();
    }

    /**
     * Verifica que la pila de descarte este vacia al ser creada.
     */
    @Test
    void constructor_initializesEmptyPile() {
        assertTrue(discardPile.isEmpty(), "La pila de descarte deberia estar vacia inicialmente.");
        assertEquals(0, discardPile.size(), "El tamano de la pila de descarte deberia ser 0 inicialmente.");
    }

    /**
     * Verifica que {@link DiscardPile#discard(Card)} agrega correctamente
     * una carta a la pila de descarte y que {@link DiscardPile#SuperiorCard()}
     * devuelve esa carta. Tambien verifica los estados de isEmpty y size.
     */
    @Test
    void discard_addsCard_updatesStatus_and_SuperiorCard_returnsIt() {
        assertTrue(discardPile.isEmpty(), "La pila de descarte deberia estar vacia inicialmente.");

        discardPile.discard(cardRed5);
        assertFalse(discardPile.isEmpty(), "La pila no deberia estar vacia despues de descartar.");
        assertEquals(1, discardPile.size(), "El tamano deberia ser 1 despues de descartar una carta.");
        assertEquals(cardRed5, discardPile.SuperiorCard(), "SuperiorCard deberia devolver la carta recien descartada.");

        discardPile.discard(cardBlue3);
        assertEquals(2, discardPile.size(), "El tamano deberia ser 2 despues de descartar otra carta.");
        assertEquals(cardBlue3, discardPile.SuperiorCard(), "SuperiorCard deberia devolver la ultima carta descartada.");
    }

    /**
     * Prueba que {@link DiscardPile#SuperiorCard()} devuelve la ultima carta
     * descartada cuando hay varias cartas en la pila.
     */
    @Test
    void SuperiorCard_returnsLastDiscardedCard_whenMultipleCards() {
        discardPile.discard(cardRed5);
        discardPile.discard(cardBlue3);
        discardPile.discard(cardGreenSkip);

        assertEquals(cardGreenSkip, discardPile.SuperiorCard(), "SuperiorCard deberia ser la ultima carta anadida.");
    }

    /**
     * Prueba que {@link DiscardPile#SuperiorCard()} retorna null
     * cuando la pila de descarte está vacía.
     */
    @Test
    void SuperiorCard_returnsNull_whenPileIsEmpty() {
        assertTrue(discardPile.isEmpty(), "La pila de descarte debería estar vacía.");
        assertNull(discardPile.SuperiorCard(), "SuperiorCard debería retornar null si la pila está vacía.");
    }

    /**
     * Verifica que {@link DiscardPile#recycleDeck()} devuelve una lista vacia
     * si la pila de descarte tiene una o ninguna carta.
     */
    @Test
    void recycleDeck_returnsEmptyList_whenOneOrZeroCards() {
        // Caso 0 cartas
        assertTrue(discardPile.isEmpty());
        List<Card> recycled1 = discardPile.recycleDeck();
        assertNotNull(recycled1, "La lista reciclada no deberia ser nula.");
        assertTrue(recycled1.isEmpty(), "La lista reciclada deberia estar vacia si la pila original esta vacia.");
        assertTrue(discardPile.isEmpty(), "La pila de descarte deberia permanecer vacia.");
        assertEquals(0, discardPile.size(), "El tamano deberia ser 0.");


        // Caso 1 carta
        discardPile.discard(cardRed5);
        assertEquals(1, discardPile.size());
        List<Card> recycled2 = discardPile.recycleDeck();
        assertNotNull(recycled2, "La lista reciclada no deberia ser nula.");
        assertTrue(recycled2.isEmpty(), "La lista reciclada deberia estar vacia si la pila original tenia 1 carta.");
        assertEquals(cardRed5, discardPile.SuperiorCard(), "La carta original deberia permanecer en la pila de descarte.");
        assertEquals(1, discardPile.size(), "El tamano deberia ser 1 despues de intentar reciclar con una carta.");
    }

    /**
     * Prueba la funcionalidad basica de {@link DiscardPile#recycleDeck()}.
     * Verifica que la carta superior permanece en la pila de descarte y
     * las demas cartas son devueltas para ser recicladas.
     */
    @Test
    void recycleDeck_returnsCardsExceptTop_and_keepsTopCard() {
        discardPile.discard(cardRed5);      // Sera la de abajo
        discardPile.discard(cardBlue3);     // Sera la del medio
        discardPile.discard(cardGreenSkip); // Sera la tercera (se recicla)
        discardPile.discard(cardWild);      // Nueva superior, esta se quedara

        assertEquals(4, discardPile.size(), "Tamano inicial antes de reciclar deberia ser 4.");
        List<Card> recycledCards = discardPile.recycleDeck();

        assertNotNull(recycledCards, "La lista de cartas recicladas no deberia ser nula.");
        assertEquals(3, recycledCards.size(), "Deberia haber 3 cartas en la lista reciclada.");
        assertTrue(recycledCards.contains(cardRed5), "La lista reciclada deberia contener cardRed5.");
        assertTrue(recycledCards.contains(cardBlue3), "La lista reciclada deberia contener cardBlue3.");
        assertTrue(recycledCards.contains(cardGreenSkip), "La lista reciclada deberia contener cardGreenSkip.");
        assertFalse(recycledCards.contains(cardWild), "La lista reciclada no deberia contener la nueva carta superior (cardWild).");

        // Verificar que la pila de descarte ahora solo contiene la carta superior original
        assertEquals(cardWild, discardPile.SuperiorCard(), "La pila de descarte deberia contener solo la carta superior original (cardWild).");
        assertEquals(1, discardPile.size(), "La pila deberia tener solo 1 carta despues de reciclar.");
        assertFalse(discardPile.isEmpty(), "La pila no deberia estar vacia despues de reciclar (debe quedar la superior).");
    }

    /**
     * Prueba el comportamiento de {@link DiscardPile#recycleDeck()} cuando
     * solo hay dos cartas en la pila.
     */
    @Test
    void recycleDeck_withTwoCards() {
        discardPile.discard(cardRed5); // Se reciclara
        discardPile.discard(cardBlue3); // Se quedara
        assertEquals(2, discardPile.size());

        List<Card> recycledCards = discardPile.recycleDeck();

        assertEquals(1, recycledCards.size(), "Deberia haber 1 carta reciclada.");
        assertTrue(recycledCards.contains(cardRed5), "La carta reciclada deberia ser cardRed5.");
        assertEquals(cardBlue3, discardPile.SuperiorCard(), "La carta superior deberia ser cardBlue3.");
        assertEquals(1, discardPile.size(), "El tamano deberia ser 1 despues de reciclar con dos cartas.");
    }
}