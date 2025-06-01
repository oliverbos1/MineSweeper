package com.oliverbos1.minesweeper;

import com.almasb.fxgl.event.EventBus;
import java.util.Random;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getEventBus;

public class GameLogic {

    GameBoard gameBoard;
    Random rand;
    EventBus eventBus;
    private int width;
    private int height;
    private int mineAmount;

    public GameLogic(Random rand, EventBus eventBus) {
        this.rand = rand;
        this.eventBus = eventBus;
    }

    public void initialize(MineSweeperSettings settings) {
        initializeBoard(settings.nrTilesHorizontal(), settings.nrTilesVertical(), settings.mineAmount());

        eventBus.addEventHandler(GameEvents
                .MoveEvent.MOVE_EVENT_TYPE, this::onMoveEvent);
        eventBus.addEventHandler(GameEvents
                .RestartGameEvent.RESTART_GAME_EVENT_EVENT_TYPE, this::onRestartGameEvent);
        eventBus.addEventHandler(GameEvents
                .MineSweeperSettingsChangedEvent.MINE_SWEEPER_SETTINGS_CHANGED_EVENT_EVENT_TYPE, this::onMineSweeperSettingsChangedEvent);
    }

    private void initializeBoard(int width, int height, int mineAmount) {
        this.width = width;
        this.height = height;
        this.mineAmount = Math.min(width * height - 1, mineAmount);
        gameBoard = new GameBoard(width, height);
        for (int r = 0; r < this.mineAmount; r++) {
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

    private void onMineSweeperSettingsChangedEvent(GameEvents.MineSweeperSettingsChangedEvent mineSweeperSettingsChangedEvent) {
        MineSweeperSettings mineSweeperSettings = mineSweeperSettingsChangedEvent.mineSweeperSettings;
        initializeBoard(mineSweeperSettings.nrTilesHorizontal(), mineSweeperSettings.nrTilesVertical(), mineSweeperSettings.mineAmount());
    }

    private void onRestartGameEvent(GameEvents.RestartGameEvent restartGameEvent) {
        initializeBoard(width, height, mineAmount);
        publishBoard(gameBoard);
    }

    private void onMoveEvent(GameEvents.MoveEvent e) {
        System.out.println("onMoveEvent");
        switch (e.moveType) {
            case TileOpened -> tileOpened(e.x, e.y);
            case FlagPlaced -> flagPlaced(e.x, e.y);
        }
    }

    public void openBoard() {
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

            if (gameBoard.checkLosingState().isPresent()) {
                openBoard();
            }
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

    private void flagPlaced(int x, int y) {

        GameBoard.FieldState existingField = gameBoard.getField(x, y);
        if (!existingField.isOpen()) {
            gameBoard.setField(x, y, existingField.withHasFlag(!existingField.hasFlag()));
        }

        publishBoard(gameBoard);
    }

    private static void publishBoard(GameBoard gameBoard) {
        getEventBus().fireEvent(new GameEvents.BoardUpdatedEvent(gameBoard));
    }



}
