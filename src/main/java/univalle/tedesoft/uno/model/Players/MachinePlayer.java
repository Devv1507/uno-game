package univalle.tedesoft.uno.model.Players;

import univalle.tedesoft.uno.model.Enum.Color;

import java.util.Random;

public class MachinePlayer extends Player {
    private final Random random;

    public MachinePlayer() {
        this.name = "Machine";
        this.random = new Random();
    }

    /**
     * Simula que la maquina elija aleatoriamente un color.
     * @return El color elegido aleatoriamente.
     */
    public Color chooseColor() {
        Color[] possibleColors = {Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE};
        int index = this.random.nextInt(possibleColors.length);
        return possibleColors[index];
    }
}
