package univalle.tedesoft.uno.threads;

import javafx.application.Platform;
import univalle.tedesoft.uno.controller.GameController;
import univalle.tedesoft.uno.model.Players.HumanPlayer;
import univalle.tedesoft.uno.model.State.GameState;

public class MachineCatchUnoRunnable implements Runnable {
    private final GameController gameController;
    private final long delayMs;

    public MachineCatchUnoRunnable(GameController gameController, long delayMs) {
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
                // y el GameController y sus componentes son aún válidos.
                if (gameController == null || gameController.getGameState() == null || gameController.getGameState().isGameOver()) {
                    return;
                }

                // La lógica original del antiguo startMachineCatchUnoTimer
                HumanPlayer humanPlayer = gameController.getHumanPlayer(); // Necesitarás un getter en GameController

                // Verificar de nuevo si el humano AÚN no ha dicho UNO y sigue con 1 carta
                if (humanPlayer.getNumeroCartas() == 1 && !humanPlayer.hasDeclaredUnoThisTurn()) {
                    gameController.getGameView().displayMessage("¡Máquina te atrapó! No dijiste UNO. Robas " + GameState.PENALTY_CARDS_FOR_UNO + " cartas.");
                    gameController.getGameState().penalizePlayerForUno(humanPlayer); // Modelo penaliza
                    gameController.getGameView().updatePlayerHand(humanPlayer.getCards(), gameController); // Vista actualiza
                }
                // Haya penalizado o no, la máquina ahora toma su turno.
                // Es crucial que esto se llame para que el juego continúe.
                gameController.scheduleMachineTurn();
            });

        } catch (InterruptedException e) {
            // Si el hilo es interrumpido (por ejemplo, al reiniciar el juego),
            // restaurar el estado de interrupción y terminar.
            Thread.currentThread().interrupt();
            System.out.println("MachineCatchUnoRunnable interrumpido.");
        }
    }
}
