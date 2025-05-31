package com.babymakingjuice.minesweeper;

import com.almasb.fxgl.event.EventBus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GameLogicTest {

    @Test
    void initialize() {

        var randomMock = Mockito.mock(Random.class);
        var eventBusMock = Mockito.mock(EventBus.class);
        Mockito.when(randomMock.nextInt(3)).thenReturn(0, 0, 1, 1, 2, 2);

        var gameLogic = new GameLogic(randomMock, eventBusMock);
        gameLogic.initialize(new MineSweeperSettings(3, 3, 3));

        assertEquals(true, gameLogic.gameBoard.getField(0, 0).hasMine());
        assertEquals(true, gameLogic.gameBoard.getField(1, 1).hasMine());
        assertEquals(true, gameLogic.gameBoard.getField(2, 2).hasMine());
        assertEquals(false, gameLogic.gameBoard.getField(0, 1).hasMine());
    }
}