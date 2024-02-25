package com.babymakingjuice.minesweeper;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;

import java.util.Random;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getEventBus;

public class MineSweeperApp extends GameApplication {

    private GameLogic gameLogic;
    private GameDisplay gameDisplay = new GameDisplay();

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(1000);
        gameSettings.setHeight(1100);
        gameSettings.setTitle("Mine Sweeper");
        gameSettings.setVersion("v1.0");
        gameSettings.setGameMenuEnabled(false);
    }

    @Override
    protected void initGame() {
        gameLogic = new GameLogic(new Random(), getEventBus());
        gameLogic.initialize(10, 10, 4);
    }

    @Override
    protected void initUI() {
        gameDisplay.initialize();
    }

    public static void main(String[] args) {
        launch(args);
    }
}