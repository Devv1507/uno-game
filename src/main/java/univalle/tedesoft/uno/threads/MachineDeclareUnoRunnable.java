package univalle.tedesoft.uno.threads;

import javafx.application.Platform;
import univalle.tedesoft.uno.controller.GameController;
import univalle.tedesoft.uno.model.Players.MachinePlayer;

public class MachineDeclareUnoRunnable implements Runnable {
    private final GameController gameController;
    private final long delayMs;

    public MachineDeclareUnoRunnable(GameController gameController, long delayMs) {
        this.gameController = gameController;
        this.delayMs = delayMs;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(this.delayMs);

            // Verificar si el hilo fue interrumpido durante el sleep
            if (Thread.currentThread().isInterrupted()) {
                return; // Salir si fue interrumpido
            }

            Platform.runLater(() -> {
                // Solo ejecutar si el juego no ha sido interrumpido/terminado
                if (gameController == null || gameController.getGameState() == null || gameController.getGameState().isGameOver()) {
                    return;
                }

                MachinePlayer machinePlayer = gameController.getMachinePlayer(); // Asumiendo getter
                boolean machineSuccessfullyDeclaredUno = false;

                // Verificar si la máquina aún debe declarar UNO (no fue atrapada y sigue con 1 carta)
                if (machinePlayer.isUnoCandidate() && !machinePlayer.hasDeclaredUnoThisTurn()) {
                    gameController.getGameState().playerDeclaresUno(machinePlayer);
                    gameController.getGameView().displayMessage("¡Máquina dice UNO!"); // Asumiendo getter
                    machineSuccessfullyDeclaredUno = true;
                }

                // Si la máquina efectivamente declaró UNO, ya no se le puede castigar
                if (machineSuccessfullyDeclaredUno) {
                    gameController.setCanPunishMachine(false);
                }
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("MachineDeclareUnoRunnable interrumpido.");
        }
    }
}
