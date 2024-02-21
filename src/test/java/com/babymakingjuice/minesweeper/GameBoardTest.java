package com.babymakingjuice.minesweeper;

import org.junit.jupiter.api.Test;

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
}