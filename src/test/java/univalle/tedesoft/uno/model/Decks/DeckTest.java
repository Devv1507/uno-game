package univalle.tedesoft.uno.model.Decks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import univalle.tedesoft.uno.exceptions.EmptyDeckException;
import univalle.tedesoft.uno.model.Cards.*;
import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Pruebas unitarias para la clase Deck.
 * Estas pruebas verifican la correcta inicializacion del mazo,
 * la distribucion de cartas, la funcionalidad de barajar y tomar cartas.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
class DeckTest {
    private Deck deck;
    // Segun la implementacion actual de Deck.intializeDeck():
    // 4 colores * (10 NumberCards (0-9) + 2 DrawTwo + 1 Skip) = 4 * 13 = 52
    // 4 Wild Cards
    // 4 WildDrawFour Cards
    // Total = 52 + 4 + 4 = 60 cartas
    private static final int EXPECTED_DECK_SIZE = 60;

    /**
     * Metodo de configuracion que se ejecuta antes de cada prueba.
     * Inicializa una nueva instancia de {@link Deck}.
     */
    @BeforeEach
    void setUp() {
        deck = new Deck();
    }

    /**
     * Verifica que el constructor de {@link Deck} crea un mazo
     * con el numero esperado de cartas.
     */
    @Test
    void constructor_initializesDeckWithCorrectNumberOfCards() {
        assertNotNull(deck.getCards(), "La lista de cartas no deberia ser nula.");
        assertEquals(EXPECTED_DECK_SIZE, deck.getNumeroCartas(), "El mazo deberia tener " + EXPECTED_DECK_SIZE + " cartas inicialmente.");
    }

    /**
     * Verifica que el mazo se inicializa con la distribucion correcta de
     * tipos y colores de cartas, segun la logica en initializeDeck().
     */
    @Test
    void constructor_initializesDeckWithCorrectCardDistribution() {
        List<Card> cards = deck.getCards();
        long numberCardsCount = cards.stream().filter(c -> c instanceof NumberCard).count();
        long drawTwoCount = cards.stream().filter(c -> c instanceof DrawTwoCard).count();
        long skipCount = cards.stream().filter(c -> c instanceof SkipCard).count();
        long wildCount = cards.stream().filter(c -> c instanceof WildCard).count();
        long wildDrawFourCount = cards.stream().filter(c -> c instanceof WildDrawFourCard).count();

        // Verificaciones basadas en la implementacion actual de Deck.java
        // 4 colores * 10 cartas numericas (0-9) = 40
        assertEquals(40, numberCardsCount, "Deberia haber 40 cartas numericas.");
        // 4 colores * 2 cartas +2 = 8
        assertEquals(8, drawTwoCount, "Deberia haber 8 cartas +2.");
        // 4 colores * 1 carta Skip = 4
        assertEquals(4, skipCount, "Deberia haber 4 cartas Skip.");
        // 4 cartas Wild
        assertEquals(4, wildCount, "Deberia haber 4 cartas Wild.");
        // 4 cartas Wild +4
        assertEquals(4, wildDrawFourCount, "Deberia haber 4 cartas Wild +4.");

        // Verificacion adicional de colores para cartas numeradas, +2 y Skip
        for (Color color : Color.values()) {
            if (color == Color.WILD) continue;

            long numCardsThisColor = cards.stream()
                    .filter(c -> c instanceof NumberCard && c.getColor() == color)
                    .count();
            assertEquals(10, numCardsThisColor, "Deberia haber 10 cartas numericas de color " + color);

            long drawTwoThisColor = cards.stream()
                    .filter(c -> c instanceof DrawTwoCard && c.getColor() == color)
                    .count();
            assertEquals(2, drawTwoThisColor, "Deberia haber 2 cartas +2 de color " + color);

            long skipThisColor = cards.stream()
                    .filter(c -> c instanceof SkipCard && c.getColor() == color)
                    .count();
            assertEquals(1, skipThisColor, "Deberia haber 1 carta Skip de color " + color);
        }
    }


    /**
     * Prueba que el metodo Deck.shuffle() cambia el orden de las cartas.
     * Compara el orden antes y despues de barajar. Hay una probabilidad
     * extremadamente pequena de que el orden sea el mismo despues de barajar,
     * pero para fines practicos, esta prueba es valida.
     * El constructor ya baraja, asi que tomamos esa lista, la copiamos,
     * y volvemos a barajar para asegurar que el metodo shuffle funciona.
     */
    @Test
    void shuffle_changesCardOrder() {
        List<Card> cardsBeforeShuffle = new ArrayList<>(deck.getCards());
        deck.shuffle(); // Barajar de nuevo
        List<Card> cardsAfterShuffle = deck.getCards();

        assertEquals(EXPECTED_DECK_SIZE, cardsAfterShuffle.size(), "Barajar no deberia cambiar el numero de cartas.");
        // Es altamente improbable que la lista sea igual despues de barajar,
        // a menos que el mazo sea muy pequeno o el algoritmo de shuffle sea defectuoso.
        assertNotEquals(cardsBeforeShuffle, cardsAfterShuffle, "El orden de las cartas deberia ser diferente despues de barajar (probabilidad baja de fallo si son iguales).");

        // Adicionalmente, verificar que todas las cartas originales aun estan presentes
        assertTrue(cardsAfterShuffle.containsAll(cardsBeforeShuffle) && cardsBeforeShuffle.containsAll(cardsAfterShuffle),
                "Barajar no deberia perder ni agregar cartas, solo cambiar el orden.");
    }

    /**
     * Prueba que Deck.takeCard() devuelve la carta superior del mazo
     * y la elimina, reduciendo el tamano del mazo.
     */
    @Test
    void takeCard_returnsTopCardAndRemovesIt_whenDeckIsNotEmpty() throws EmptyDeckException { // Añadir throws
        // Asegurarse de que el mazo no esta vacio para esta prueba
        if (this.deck.getNumeroCartas() == 0) {
            // Esto no debería ocurrir con la inicialización estándar.
            fail("El mazo esta vacio al inicio de la prueba, no se puede probar takeCard().");
        }

        // Tomamos una copia de la lista original para saber cuál es la primera
        List<Card> originalCards = new ArrayList<>(this.deck.getCards());
        Card expectedTopCard = originalCards.get(0);
        int initialSize = this.deck.getNumeroCartas();

        Card takenCard = this.deck.takeCard(); // Esto puede lanzar EmptyDeckException

        assertNotNull(takenCard, "La carta tomada no deberia ser nula.");
        assertEquals(expectedTopCard, takenCard, "La carta tomada deberia ser la que estaba al tope del mazo (primera en la lista interna original).");
        assertEquals(initialSize - 1, this.deck.getNumeroCartas(), "El numero de cartas en el mazo deberia disminuir en 1.");

        // Verificar que la carta tomada ya no está en el mazo
        assertFalse(this.deck.getCards().contains(takenCard), "La carta tomada no deberia estar mas en el mazo.");

        if (this.deck.getNumeroCartas() > 0) {
            // La nueva primera carta en el mazo (si aún quedan) no debería ser la que se tomó.
            assertNotEquals(takenCard, this.deck.getCards().get(0), "La nueva carta superior deberia ser diferente a la tomada.");
        }
    }

    /**
     * Prueba que Deck.takeCard() lanza EmptyDeckException cuando el mazo esta vacio.
     * Este es el cambio principal debido a la refactorización de takeCard().
     */
    @Test
    void takeCard_throwsEmptyDeckException_whenDeckIsEmpty() throws EmptyDeckException { // Añadir throws para el bucle
        // Vaciar el mazo
        int currentSize = this.deck.getNumeroCartas();
        for (int i = 0; i < currentSize; i++) {
            this.deck.takeCard(); // Tomar todas las cartas existentes
        }

        assertEquals(0, this.deck.getNumeroCartas(), "El mazo deberia estar vacio despues de tomar todas las cartas.");

        // Verificar que se lanza la excepción esperada
        EmptyDeckException thrownException = assertThrows(
                EmptyDeckException.class,
                () -> this.deck.takeCard(),
                "Se esperaba que takeCard() lanzara EmptyDeckException cuando el mazo esta vacio."
        );

        // Opcionalmente, verificar el mensaje de la excepción si es relevante
        assertNotNull(thrownException.getMessage());
        assertTrue(thrownException.getMessage().contains("mazo principal está vacío"), "El mensaje de la excepcion deberia indicar que el mazo principal esta vacio.");
    }

    /**
     * Prueba que Deck.getNumeroCartas() refleja correctamente
     * el número de cartas después de tomar algunas.
     */
    @Test
    void getNumeroCartas_reflectsTakenCards() throws EmptyDeckException { // Añadir throws
        assertEquals(EXPECTED_DECK_SIZE, this.deck.getNumeroCartas(), "Tamaño inicial del mazo.");

        if (EXPECTED_DECK_SIZE > 0) {
            this.deck.takeCard();
            assertEquals(EXPECTED_DECK_SIZE - 1, this.deck.getNumeroCartas(), "Tamaño del mazo después de tomar 1 carta.");
        }

        if (EXPECTED_DECK_SIZE > 2) { // Solo si hay suficientes cartas para tomar 2 más
            this.deck.takeCard();
            this.deck.takeCard();
            assertEquals(EXPECTED_DECK_SIZE - 3, this.deck.getNumeroCartas(), "Tamaño del mazo después de tomar 3 cartas en total.");
        }
    }

    /**
     * Prueba que el metodo Deck.getCards() devuelve la lista interna
     * de cartas.
     */
    @Test
    void getCards_returnsInternalListOfCards() {
        List<Card> internalCards = deck.getCards();
        assertNotNull(internalCards);
        assertEquals(EXPECTED_DECK_SIZE, internalCards.size());
    }

}