package univalle.tedesoft.uno.model.Decks;

import java.util.Collections;
import java.util.LinkedList;
import univalle.tedesoft.uno.model.Cards.*;
import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

/**
 * Clase que representa el mazo principal del juego UNO.
 * Contiene todas las cartas iniciales necesarias para jugar, incluyendo cartas numéricas,
 * de acción (+2, reverse, skip) y comodines (wild, +4).
 * El mazo se inicializa en el constructor, se baraja automáticamente y permite tomar cartas.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class Deck {
    /** Lista que contiene las cartas del mazo */
    private final LinkedList<Card> cartas;
    /**
     * Constructor del mazo. Inicializa el mazo con las cartas estandar de UNO
     * y luego lo baraja aleatoriamente.
     */
    public Deck(){
        cartas = new LinkedList<>();
        inicializarDeck();
        barajar();
    }
    /**
     * Metodo privado que genera todas las cartas necesarias para el mazo.
     * Se agregan cartas numericas del 0 al 9 y cartas de acción (+2, reverse, skip)
     * para cada color excepto WILD. Luego se agregan los comodines.
     * Tambien imprime todas las cartas generadas para fines de prueba.
     */
    private void inicializarDeck() {
        for (int c = 0; c < Color.values().length; c++) {
            if (c == Color.WILD.ordinal()){
                continue;
            }
            // Cartas numéricas 0-9, +2, reverse y skip, 14 cartas por cada color
            for(int v = 0; v < Value.values().length; v++){
                if(v <= Value.DRAW_TWO.ordinal()){
                    cartas.add(new NumberCard(Color.values()[c], Value.values()[v]));
                    if(v == Value.DRAW_TWO.ordinal()){
                        cartas.add(new NumberCard(Color.values()[c], Value.values()[v]));
                    }
                }
            }
        }
        // Comodines (wild +4 y wild color change)
        for (int i = 0; i < 4; i++) {
            cartas.add(new WildCard());
            cartas.add(new WildDrawFourCard());
        }
        //64 cartas en total 56 de colores y 8 especiales
        //Prueba para saber si el mazo se genera de forma correcta.
        for(int i = 0; i< cartas.size(); i++){
            Card carta = cartas.get(i);
            System.out.println( i+1 + " " + carta.getValue() + " " + carta.getColor());
        }
    }
    /**
     * Retorna la lista de cartas actuales del mazo.
     *
     * @return LinkedList con las cartas del mazo.
     */
    public LinkedList<Card> getCartas() {
        return cartas;
    }
    /**
     * Baraja aleatoriamente las cartas del mazo.
     */
    public void barajar() {
        Collections.shuffle(cartas);
    }
    /**
     * Toma la primera carta del mazo (parte superior).
     *
     * @return La carta retirada del mazo o null si está vacío.
     */
    public Card tomarCarta() {
        return cartas.poll(); // saca la primera
    }
}
