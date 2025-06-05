package com.oliverbos1.minesweeper;

import javafx.event.Event;
import javafx.event.EventType;

public class GameEvents {

    public static class MoveEvent extends Event {

        public static final EventType<MoveEvent> MOVE_EVENT_TYPE = new EventType<>(Event.ANY, "MoveEvent");

        public enum MoveType {
            FlagPlaced, TileOpened
        }

        public final int x;
        public final int y;
        public final MoveType moveType;

        public MoveEvent(int x, int y, MoveType moveType) {
            super(MOVE_EVENT_TYPE);
            this.x = x;
            this.y = y;
            this.moveType = moveType;
        }
    }

    public static class BoardUpdatedEvent extends Event {

        public static final EventType<BoardUpdatedEvent> BOARD_UPDATED_EVENT_EVENT_TYPE = new EventType<>(Event.ANY, "BoardUpdatedEvent");

        public final GameBoard updatedGameBoard;

        public BoardUpdatedEvent(GameBoard updatedGameBoard) {
            super(BOARD_UPDATED_EVENT_EVENT_TYPE);
            this.updatedGameBoard = updatedGameBoard;
        }
    }

    public static class NewGameEvent extends Event {

        public static final EventType<NewGameEvent> NEW_GAME_EVENT_EVENT_TYPE = new EventType<>(Event.ANY, "NewGameEvent");

        public final MineSweeperSettings mineSweeperSettings;

        public NewGameEvent(MineSweeperSettings mineSweeperSettings) {
            super(NEW_GAME_EVENT_EVENT_TYPE);
            this.mineSweeperSettings = mineSweeperSettings;
        }

    }

}