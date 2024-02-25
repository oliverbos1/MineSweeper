package com.babymakingjuice.minesweeper;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GameBoardTest {

    @Test
    void checkAdjacentMines() {

        GameBoard gameBoard = new GameBoard(4,4);
        gameBoard.setField(1, 2, new GameBoard.FieldState(false, true, false, 0));
        gameBoard.setField(3, 2, new GameBoard.FieldState(false, true, false, 0));

        var result = gameBoard.checkAdjacentMines(2, 2);

        assertEquals(2, result);
    }

    @Test
    void checkAdjacentMinesEdgeMin() {

        GameBoard gameBoard = new GameBoard(4,4);
        gameBoard.setField(1, 1, new GameBoard.FieldState(false, true, false, 0));
        gameBoard.setField(3, 2, new GameBoard.FieldState(false, true, false, 0));

        var result = gameBoard.checkAdjacentMines(0, 0);

        assertEquals(1, result);
    }

    @Test
    void checkAdjacentMinesEdgeMax() {

        GameBoard gameBoard = new GameBoard(4,4);
        gameBoard.setField(1, 2, new GameBoard.FieldState(false, true, false, 0));
        gameBoard.setField(3, 2, new GameBoard.FieldState(false, true, false, 0));

        var result = gameBoard.checkAdjacentMines(3, 3);

        assertEquals(1, result);
    }

    @Test
    void AdjacentMineCoordinates() {

        GameBoard gameBoard = new GameBoard(10, 10);
        gameBoard.adjacentTileCoordinates(5, 5);
    }

    @Test
    void checkWinningStateTrue() {

        GameBoard gameBoard = new GameBoard(2,2);
        gameBoard.setField(0, 0, new GameBoard.FieldState(false, true, true, 0));
        gameBoard.setField(0, 1, new GameBoard.FieldState(true, false, false, 0));
        gameBoard.setField(1, 0, new GameBoard.FieldState(false, true, true, 0));
        gameBoard.setField(1, 1, new GameBoard.FieldState(true, false, false, 0));

        var result = gameBoard.winningState();

        assertEquals(true, result);
    }

    @Test
    void checkWinningStateFalse() {

        GameBoard gameBoard = new GameBoard(2,2);
        gameBoard.setField(0, 0, new GameBoard.FieldState(false, true, true, 0));
        gameBoard.setField(0, 1, new GameBoard.FieldState(true, false, false, 0));
        gameBoard.setField(1, 0, new GameBoard.FieldState(false, true, false, 0));
        gameBoard.setField(1, 1, new GameBoard.FieldState(true, false, false, 0));

        var result = gameBoard.winningState();

        assertEquals(false, result);
    }

    @Test
    void checkLosingStateTrue() {

        GameBoard gameBoard = new GameBoard(2,2);
        gameBoard.setField(0, 0, new GameBoard.FieldState(true, true, false, 0));
        gameBoard.setField(0, 1, new GameBoard.FieldState(true, false, false, 0));
        gameBoard.setField(1, 0, new GameBoard.FieldState(false, true, true, 0));
        gameBoard.setField(1, 1, new GameBoard.FieldState(true, false, false, 0));

        var result = gameBoard.losingState();
        Optional<GameBoard.Coordinates> expectedCoordinate = Optional.of(new GameBoard.Coordinates(0, 0));

        assertEquals(expectedCoordinate, result);
    }

    @Test
    void checkLosingStateFalse() {

        GameBoard gameBoard = new GameBoard(2,2);
        gameBoard.setField(0, 0, new GameBoard.FieldState(false, true, true, 0));
        gameBoard.setField(0, 1, new GameBoard.FieldState(true, false, false, 0));
        gameBoard.setField(1, 0, new GameBoard.FieldState(false, false, true, 0));
        gameBoard.setField(1, 1, new GameBoard.FieldState(true, false, false, 0));

        var result = gameBoard.losingState();

        assertEquals(Optional.empty(), result);
    }
}