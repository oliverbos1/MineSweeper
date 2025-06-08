package com.oliverbos1.minesweeper;

import com.almasb.fxgl.event.EventBus;

import java.util.Optional;
import java.util.Random;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getEventBus;

public class GameLogic {

    GameBoard gameBoard;
    Random rand;
    EventBus eventBus;
    private int width;
    private int height;

    public GameLogic(Random rand, EventBus eventBus) {
        this.rand = rand;
        this.eventBus = eventBus;
    }

    public void initialize(MineSweeperSettings settings) {
        initializeBoard(settings.nrTilesHorizontal(), settings.nrTilesHorizontal(), calcMineAmount(settings));

        eventBus.addEventHandler(GameEvents
                .MoveEvent.MOVE_EVENT_TYPE, this::onMoveEvent);
        eventBus.addEventHandler(GameEvents
                .NewGameEvent.NEW_GAME_EVENT_EVENT_TYPE, this::onNewGameEvent);
    }

    private void initializeBoard(int width, int height, int mineAmount) {
        this.width = width;
        this.height = height;
        gameBoard = new GameBoard(width, height);
        for (int r = 0; r < mineAmount; r++) {
            int rx;
            int ry;
            do {
                rx = rand.nextInt(width);
                ry = rand.nextInt(height);
            }
            while (gameBoard.getField(rx, ry).hasMine());

            GameBoard.FieldState existingField = gameBoard.getField(rx, ry);
            gameBoard.setField(rx, ry, existingField.withHasMine(true));
        }
    }

    private int calcMineAmount(MineSweeperSettings settings) {
        return (int) Math.round(settings.nrTilesHorizontal() * settings.nrTilesHorizontal() * switch (settings.difficulty()) {
            case EASY -> 0.15625;
            case MEDIUM -> 0.2;
            case HARD -> 0.3;
        });
    }

    private void onNewGameEvent(GameEvents.NewGameEvent newGameEvent) {
        MineSweeperSettings mineSweeperSettings = newGameEvent.mineSweeperSettings;
        initializeBoard(
                mineSweeperSettings.nrTilesHorizontal(),
                mineSweeperSettings.nrTilesHorizontal(),
                calcMineAmount(mineSweeperSettings)
        );
        publishBoard(gameBoard);
    }

    private void onMoveEvent(GameEvents.MoveEvent e) {
        System.out.println("onMoveEvent");
        if (!gameFinished()) {
            switch (e.moveType) {
                case TILE_OPENED -> tileOpened(e.x, e.y);
                case FLAG_PLACED -> flagPlaced(e.x, e.y);
            }
        }
    }

    private boolean gameFinished() {
        return checkWinningState() || checkLosingState().isPresent();
    }

    private boolean checkWinningState() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if ((gameBoard.getField(x, y).hasMine() && !gameBoard.getField(x, y).hasFlag()) ||
                        (!gameBoard.getField(x, y).hasMine() && gameBoard.getField(x, y).hasFlag())) {
                    return false;
                }
            }
        }

        return true;
    }

    private Optional<GameBoard.Coordinates> checkLosingState() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gameBoard.getField(x, y).hasMine() && gameBoard.getField(x, y).isOpen()) {
                    return Optional.of(new GameBoard.Coordinates(x, y));
                }
            }
        }

        return Optional.empty();
    }

    private void openBoard() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                GameBoard.FieldState existingField = gameBoard.getField(x, y);
                gameBoard.setField(x, y, existingField
                        .withIsOpen(true)
                        .withAdjacentMineCount(gameBoard.checkAdjacentMines(x, y))
                );
            }
        }
    }

    private void tileOpened(int x, int y) {
        if (!gameBoard.getField(x, y).hasFlag()) {
            tileOpenRecursion(x, y);
            updateGameState();

            openBoardOnGameFinished();
            publishBoard(gameBoard);
        }
    }

    private void tileOpenRecursion(int x, int y) {
        GameBoard.FieldState existingField = gameBoard.getField(x, y);

        if (!gameBoard.getField(x, y).isOpen()) {
            gameBoard.setField(x, y, existingField
                    .withIsOpen(true)
                    .withAdjacentMineCount(gameBoard.checkAdjacentMines(x, y))
            );
            if (gameBoard.checkAdjacentMines(x, y) == 0) {
                for (GameBoard.Coordinates coordinate : gameBoard.adjacentTileCoordinates(x, y)) {
                    tileOpenRecursion(coordinate.x(), coordinate.y());
                }
            }
        }
    }

    private void updateGameState() {
        final GameBoardState newState;

        if (checkWinningState()) newState = GameBoardState.WON;
        else if (checkLosingState().isPresent()) newState = GameBoardState.LOST;
        else newState = GameBoardState.PLAYING;

        gameBoard.setGameBoardState(newState);
    }

    private void openBoardOnGameFinished() {
        if (gameBoard.getGameBoardState() != GameBoardState.PLAYING) {
            openBoard();
        }
    }

    private void flagPlaced(int x, int y) {

        GameBoard.FieldState existingField = gameBoard.getField(x, y);
        if (!existingField.isOpen()) {
            gameBoard.setField(x, y, existingField.withHasFlag(!existingField.hasFlag()));
        }

        updateGameState();
        openBoardOnGameFinished();

        publishBoard(gameBoard);
    }

    private static void publishBoard(GameBoard gameBoard) {
        getEventBus().fireEvent(new GameEvents.BoardUpdatedEvent(gameBoard));
    }
}