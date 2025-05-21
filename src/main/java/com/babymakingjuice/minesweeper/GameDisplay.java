package com.babymakingjuice.minesweeper;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.babymakingjuice.minesweeper.GameBoard.FieldState;
import com.babymakingjuice.minesweeper.GameEvents.MoveEvent.MoveType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.Optional;

import static com.almasb.fxgl.dsl.FXGL.getDialogService;
import static com.almasb.fxgl.dsl.FXGL.getGameController;
import static com.almasb.fxgl.dsl.FXGLForKtKt.*;

public class GameDisplay implements EntityFactory {
    private Entity[][] boardBackground = new Entity[10][10];
    private Entity[][] boardContent = new Entity[10][10];
    private Entity flagCountDigit1 = new Entity();
    private Entity flagCountDigit2 = new Entity();
    private Entity flagCountBackground = new Entity();

    public void initialize() {
        FXGL.getGameWorld().addEntityFactory(this);

        FXGL.spawn("bannerBackground", 0, 0);
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                boardBackground[x][y] = FXGL.spawn("tileBackground", (double) (x * FXGL.getAppWidth()) / 10, y * ((double) (FXGL.getAppHeight() - 100) / 10) + 100);
                boardContent[x][y] = FXGL.spawn("tile", (double) (x * FXGL.getAppWidth()) / 10, y * ((double) (FXGL.getAppHeight() - 100) / 10) + 100);
                boardContent[x][y].addComponent(new TileStateComponent(x, y));
                flagCountBackground = FXGL.spawn("remainingFlagCountBackground", 105, 10);
                flagCountDigit1 = FXGL.spawn("remainingFlagCountDigit1", 109, 14);
                flagCountDigit2 = FXGL.spawn("remainingFlagCountDigit2", 143, 14);
            }
        }

        getEventBus().addEventHandler(GameEvents
                .BoardUpdatedEvent.BOARD_UPDATED_EVENT_EVENT_TYPE, this::onBoardUpdatedEvent);
    }

    @Spawns("bannerBackground")
    public Entity newBannerBackground(SpawnData data) {
        var banner = entityBuilder(data)
                .view("bannerBackgroundNoTitle.png")
                .build();

        return banner;
    }
    @Spawns("tileBackground")
    public Entity newTileBackground(SpawnData data) {
        var tile = entityBuilder(data)
                .view("tileBackground.png")
                .build();

        return tile;
    }
    @Spawns("tile")
    public Entity newTile(SpawnData data) {
        HitBox box = new HitBox(BoundingShape.box(getAppWidth() / 10, ((getAppHeight() - 100) / 10) + 100));
        var tile = entityBuilder(data)
                .bbox(box)
                .view("tileUnpressed.png")
                .build();
        tile.getViewComponent().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> onTileClick(tile, e.getButton()));

        return tile;
    }
    @Spawns("remainingFlagCountDigit1")
    public Entity newRemainingFlagsCountDigit1(SpawnData data) {
        var flagCountDigit1 = entityBuilder(data)
                .view("remainingFlagCountNumber/count0.png")
                .build();

        return flagCountDigit1;
    }
    @Spawns("remainingFlagCountDigit2")
    public Entity newRemainingFlagsCountDigit2(SpawnData data) {
        var flagCountDigit2 = entityBuilder(data)
                .view("remainingFlagCountNumber/count0.png")
                .build();

        return flagCountDigit2;
    }
    @Spawns("remainingFlagCountBackground")
    public Entity newRemainingFlagsCountBackground(SpawnData data) {
        var flagCountDigit2 = entityBuilder(data)
                .view("remainingFlagCountNumber/background.png")
                .build();

        return flagCountDigit2;
    }


    private void onTileClick(Entity tile, MouseButton button) {
        TileStateComponent tileState = tile.getComponent(TileStateComponent.class);
        System.out.println(STR."tileClicked. x: \{tileState.x} y: \{tileState.y} button: \{button}");

        var moveType = switch (button) {
            case PRIMARY -> Optional.of(MoveType.TileOpened);
            case SECONDARY -> Optional.of(MoveType.FlagPlaced);
            default -> Optional.<MoveType>empty();
        };
        moveType.ifPresent(m -> getEventBus().fireEvent(new GameEvents.MoveEvent(tileState.x, tileState.y, m)));
    }

    private void onBoardUpdatedEvent(GameEvents.BoardUpdatedEvent boardUpdatedEvent) {
        GameBoard gameBoard = boardUpdatedEvent.updatedGameBoard;
        for (int x = 0; x < gameBoard.getWidth(); x++) {
            for (int y = 0; y < gameBoard.getHeight(); y++) {
                updateTile(x, y, gameBoard.getField(x, y));
            }
        }

        updateRemainingFlagCount(gameBoard);

        // Unresolved Bug line (114 - 130)

        if (gameBoard.checkLosingState().isPresent()) {
            getDialogService().showConfirmationBox("You Lose!" + "\nPlay again?", yes -> {
                if (yes) {
                    gameBoard.setDefaultBoard();
                    getGameController().startNewGame();
                } else getGameController().exit();
            });
        }

        if (gameBoard.checkWinningState()) {
            getDialogService().showConfirmationBox("You win!" + "\nPlay again?", yes -> {
                if (yes) {
                    gameBoard.setDefaultBoard();
                    getGameController().startNewGame();
                } else getGameController().exit();
            });
        }
    }

    private void updateTile(int x, int y, FieldState field) {
        var tile = boardContent[x][y];
        var tileImage = fieldStateToImage(field);
        setImage(tile, tileImage);
    }

    private String fieldStateToImage(FieldState field) {
        final String image;
        if (!field.isOpen() && !field.hasFlag()) image = "tileUnpressed.png";
        else if (!field.isOpen()) image = "tileFlag.png";
        else if (field.hasMine()) image = "tileMine.png";
        else image = STR."mineNumbers/mineCount\{field.adjacentMineCount()}.png";

        return image;
    }

    private void updateRemainingFlagCount (GameBoard gameBoard) {
        int digit1;
        int digit2;

        int remainingFlagCount = gameBoard.getMineAmount() - gameBoard.getFlagAmount();
        if (remainingFlagCount < 0) remainingFlagCount = 0;
        if (remainingFlagCount > 99) remainingFlagCount = 99;

        if (remainingFlagCount < 10) digit1 = 0;
        else digit1 = remainingFlagCount / 10;
        digit2 = remainingFlagCount % 10;
        String remainingFlagCountImageDigit1 = STR."remainingFlagCountNumber/count\{digit1}.png";
        String remainingFlagCountImageDigit2 = STR."remainingFlagCountNumber/count\{digit2}.png";

        setImage(flagCountDigit1, remainingFlagCountImageDigit1);
        setImage(flagCountDigit2, remainingFlagCountImageDigit2);

        System.out.println(STR."getMineAmount: \{gameBoard.getMineAmount()} getFlgAmount: \{gameBoard.getFlagAmount()}");
        System.out.println(STR."Digit 1: \{digit1} Digit 2: \{digit2}");
    }

    private static void setImage(Entity entity, String image) {
        entity.getViewComponent().clearChildren();
        entity.getViewComponent().addChild(FXGL.texture(image));
    }

    private static class TileStateComponent extends Component {

        public final int x;
        public final int y;

        public TileStateComponent(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
