package univalle.tedesoft.uno.model.Players;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import univalle.tedesoft.uno.model.Cards.Card;
import univalle.tedesoft.uno.model.Cards.NumberCard;
import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

import java.util.List;

/**
 * Pruebas unitarias para la clase Player.
 * Cubre la funcionalidad básica de gestión de cartas, nombre y estados de UNO.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
import static org.junit.jupiter.api.Assertions.*;
class PlayerTest {
    /** Jugador utilizado en las pruebas. */
    private Player player;
    /** Primera carta de ejemplo para las pruebas. */
    private Card card1;
    /** Segunda carta de ejemplo para las pruebas. */
    private Card card2;

    /**
     * Configura el entorno para cada prueba.
     * Inicializa un nuevo jugador y algunas cartas de ejemplo.
     */
    @BeforeEach
    void setUp() {
        player = new Player(); // Usamos el constructor por defecto como en el código
        card1 = new NumberCard(Color.RED, Value.FIVE);
        card2 = new NumberCard(Color.BLUE, Value.THREE);
    }

    /**
     * Verifica que el constructor de Player inicializa el objeto
     * con los valores por defecto esperados: lista de cartas vacia,
     * contador de cartas en cero, y los indicadores de UNO en falso.
     */
    @Test
    void constructor_initializesCorrectly() {
        assertNotNull(player.cards, "La lista de cartas no debería ser nula.");
        assertTrue(player.cards.isEmpty(), "La lista de cartas debería estar vacía inicialmente.");
        assertEquals(0, player.getNumeroCartas(), "El número de cartas inicial debería ser 0.");
        assertFalse(player.isUnoCandidate(), "isUnoCandidate debería ser false inicialmente.");
        assertFalse(player.hasDeclaredUnoThisTurn(), "hasDeclaredUnoThisTurn debería ser false inicialmente.");
        assertNull(player.name, "El nombre debería ser null inicialmente si no se pasa por constructor (como en la clase Player actual).");
    }

    /**
     * Prueba que el metodo Player.addCard(Card) añade correctamente
     * una carta a la mano del jugador e incrementa el contador de cartas.
     * Se prueba añadiendo una y luego una segunda carta.
     */
    @Test
    void addCard_addsCardToHand_and_increasesCardCount() {
        player.addCard(card1);
        assertEquals(1, player.getNumeroCartas(), "El número de cartas debería ser 1 después de agregar una carta.");
        assertTrue(player.getCards().contains(card1), "La mano debería contener la carta agregada.");

        player.addCard(card2);
        assertEquals(2, player.getNumeroCartas(), "El número de cartas debería ser 2 después de agregar otra carta.");
        assertTrue(player.getCards().contains(card2), "La mano debería contener la segunda carta agregada.");
    }

    /**
     * Prueba que el metodo Player.removeCardOfCards(Card) elimina
     * una carta existente de la mano del jugador y disminuye el contador
     * de cartas. Se verifica que la carta correcta sea eliminada y que
     * otras cartas permanezcan en la mano.
     */
    @Test
    void removeCardOfCards_removesExistingCard_and_decreasesCardCount() {
        player.addCard(card1);
        player.addCard(card2);

        player.removeCardOfCards(card1);
        assertEquals(1, player.getNumeroCartas(), "El número de cartas debería ser 1 después de remover una carta.");
        assertFalse(player.getCards().contains(card1), "La mano no debería contener la carta removida.");
        assertTrue(player.getCards().contains(card2), "La mano debería aún contener la otra carta.");
    }

    /**
     * Prueba el comportamiento del metodo Player.removeCardOfCards(Card)
     * cuando se intenta eliminar una carta que no esta presente en la mano
     * del jugador. Se espera que la mano y el contador de cartas no sufran
     * modificaciones.
     */
    @Test
    void removeCardOfCards_doesNothing_ifCardNotInHand() {
        player.addCard(card1);
        Card nonExistentCard = new NumberCard(Color.GREEN, Value.ZERO);

        player.removeCardOfCards(nonExistentCard);
        assertEquals(1, player.getNumeroCartas(), "El número de cartas no debería cambiar si se intenta remover una carta no existente.");
        assertTrue(player.getCards().contains(card1), "La carta original debería seguir en la mano.");
    }

    /**
     * Verifica que el metodo Player.getNumeroCartas() devuelve
     * el numero correcto de cartas en la mano del jugador en diferentes
     * situaciones: inicialmente, despues de agregar cartas y despues
     * de eliminar cartas.
     */
    @Test
    void getNumeroCartas_returnsCorrectCount() {
        assertEquals(0, player.getNumeroCartas(), "Número de cartas inicial.");
        player.addCard(card1);
        assertEquals(1, player.getNumeroCartas(), "Número de cartas después de agregar 1.");
        player.addCard(card2);
        assertEquals(2, player.getNumeroCartas(), "Número de cartas después de agregar 2.");
        player.removeCardOfCards(card1);
        assertEquals(1, player.getNumeroCartas(), "Número de cartas después de remover 1.");
    }

    /**
     * Prueba que el metodo Player.getCards() devuelve una lista
     * que refleja con precision las cartas actualmente en la mano del jugador.
     * Se verifica el estado inicial (lista vacia) y despues de agregar cartas.
     */
    @Test
    void getCards_returnsCorrectListOfCards() {
        assertTrue(player.getCards().isEmpty(), "Inicialmente la lista de cartas debe estar vacía.");
        player.addCard(card1);
        player.addCard(card2);
        List<Card> playerCards = player.getCards();
        assertEquals(2, playerCards.size());
        assertTrue(playerCards.contains(card1));
        assertTrue(playerCards.contains(card2));
    }

    /**
     * Verifica la funcionalidad de los metodos Player.setName(String)
     * y Player.getName(). Se prueba que el nombre se establece
     * correctamente y que puede ser recuperado.
     */
    @Test
    void setName_and_getName_workCorrectly() {
        assertNull(player.getName(), "El nombre inicial debería ser null.");
        String testName = "TestPlayer";
        player.setName(testName);
        assertEquals(testName, player.getName(), "getName debería retornar el nombre establecido.");
    }

    /**
     * Prueba que el metodo Player.clearHand() vacia completamente
     * la mano del jugador, resultando en una lista de cartas vacia y un
     * contador de cartas en cero.
     */
    @Test
    void clearHand_emptiesTheHand() {
        player.addCard(card1);
        player.addCard(card2);
        assertFalse(player.getCards().isEmpty());

        player.clearHand();
        assertTrue(player.getCards().isEmpty(), "La lista de cartas debería estar vacía después de clearHand.");
        assertEquals(0, player.getNumeroCartas(), "El número de cartas debería ser 0 después de clearHand.");
    }

    /**
     * Verifica la funcionalidad de los metodos Player.setUnoCandidate(boolean)
     * y Player.isUnoCandidate(). Se prueba que el estado de "candidato a UNO"
     * se puede establecer en verdadero y falso, y que el getter refleja
     * estos cambios.
     */
    @Test
    void setUnoCandidate_and_isUnoCandidate_workCorrectly() {
        assertFalse(player.isUnoCandidate(), "isUnoCandidate inicial debe ser false.");
        player.setUnoCandidate(true);
        assertTrue(player.isUnoCandidate(), "isUnoCandidate debe ser true después de setUnoCandidate(true).");
        player.setUnoCandidate(false);
        assertFalse(player.isUnoCandidate(), "isUnoCandidate debe ser false después de setUnoCandidate(false).");
    }

    /**
     * Verifica la funcionalidad de los metodos Player.setHasDeclaredUnoThisTurn(boolean)
     * y Player.hasDeclaredUnoThisTurn(). Se prueba que el estado de
     * "ha declarado UNO este turno" se puede establecer en verdadero y falso,
     * y que el getter refleja estos cambios.
     */
    @Test
    void setHasDeclaredUnoThisTurn_and_hasDeclaredUnoThisTurn_workCorrectly() {
        assertFalse(player.hasDeclaredUnoThisTurn(), "hasDeclaredUnoThisTurn inicial debe ser false.");
        player.setHasDeclaredUnoThisTurn(true);
        assertTrue(player.hasDeclaredUnoThisTurn(), "hasDeclaredUnoThisTurn debe ser true después de setHasDeclaredUnoThisTurn(true).");
        player.setHasDeclaredUnoThisTurn(false);
        assertFalse(player.hasDeclaredUnoThisTurn(), "hasDeclaredUnoThisTurn debe ser false después de setHasDeclaredUnoThisTurn(false).");
    }

    /**
     * Prueba que el metodo Player.resetUnoStatus() restablece
     * correctamente ambos indicadores de UNO ({@code isUnoCandidate} y
     * {@code hasDeclaredUnoThisTurn}) a falso, independientemente de su
     * estado previo.
     */
    @Test
    void resetUnoStatus_resetsBothUnoFlags() {
        player.setUnoCandidate(true);
        player.setHasDeclaredUnoThisTurn(true);

        player.resetUnoStatus();
        assertFalse(player.isUnoCandidate(), "isUnoCandidate debe ser false después de resetUnoStatus.");
        assertFalse(player.hasDeclaredUnoThisTurn(), "hasDeclaredUnoThisTurn debe ser false después de resetUnoStatus.");
    }

    /**
     * Verifica que el metodo Player.resetUnoStatus() funciona
     * correctamente incluso cuando los indicadores de UNO ya son falsos.
     * Esto asegura que no haya efectos secundarios inesperados al llamar
     * el metodo en este estado.
     */
    @Test
    void resetUnoStatus_worksCorrectly_whenFlagsAreFalse() {
        player.setUnoCandidate(false);
        player.setHasDeclaredUnoThisTurn(false);

        player.resetUnoStatus();
        assertFalse(player.isUnoCandidate(), "isUnoCandidate debe seguir false después de resetUnoStatus.");
        assertFalse(player.hasDeclaredUnoThisTurn(), "hasDeclaredUnoThisTurn debe seguir false después de resetUnoStatus.");
    }
  
}