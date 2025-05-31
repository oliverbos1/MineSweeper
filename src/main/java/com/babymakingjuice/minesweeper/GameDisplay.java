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
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.Optional;

import static com.almasb.fxgl.dsl.FXGL.getDialogService;
import static com.almasb.fxgl.dsl.FXGL.getGameController;
import static com.almasb.fxgl.dsl.FXGLForKtKt.*;

@SuppressWarnings("preview")
public class GameDisplay implements EntityFactory {
    private Entity[][] boardContent;
    private Entity flagCountDigit1 = new Entity();
    private Entity flagCountDigit2 = new Entity();

    int tileWidthInPixels = 100;
    int tileHeightInPixels = 100;

    public void initialize(MineSweeperSettings settings) {
        getGameWorld().addEntityFactory(this);

        boardContent = new Entity[settings.nrTilesHorizontal()][settings.nrTilesVertical()];

        spawn("bannerBackground", 0, 0);
        spawn("remainingFlagCountBackground", 105, 10);
        flagCountDigit1 = spawn("remainingFlagCountDigit1", 109, 14);
        flagCountDigit2 = spawn("remainingFlagCountDigit2", 143, 14);

        int boardHeightInPixels = getAppHeight() - 100;

        double scaleX = getAppWidth() / (double) (settings.nrTilesHorizontal() * tileWidthInPixels);
        double scaleY = boardHeightInPixels / (double) (settings.nrTilesVertical() * tileHeightInPixels);

        for (int x = 0; x < settings.nrTilesHorizontal(); x++) {
            for (int y = 0; y < settings.nrTilesVertical(); y++) {
                double xOffset = (double) (x * getAppWidth()) / settings.nrTilesHorizontal();
                double yOffset = y * ((double) boardHeightInPixels / settings.nrTilesVertical()) + 100;
                scaledSpawn("tileBackground", xOffset, yOffset, scaleX, scaleY);
                boardContent[x][y] = scaledSpawn("tile", xOffset, yOffset, scaleX, scaleY);
                boardContent[x][y].addComponent(new TileStateComponent(x, y));
            }
        }

        getEventBus().addEventHandler(GameEvents
                .BoardUpdatedEvent.BOARD_UPDATED_EVENT_EVENT_TYPE, this::onBoardUpdatedEvent);
    }

    private static Entity scaledSpawn(String entityName, double x, double y, double scaleX, double scaleY) {
        Entity tile = spawn(entityName, x, y);
        tile.setScaleX(scaleX);
        tile.setScaleY(scaleY);
        tile.setScaleOrigin(new Point2D(0, 0));
        return tile;
    }

    @Spawns("bannerBackground")
    public Entity newBannerBackground(SpawnData data) {

        return entityBuilder(data)
                .view("bannerBackgroundNoTitle.png")
                .build();
    }
    @Spawns("tileBackground")
    public Entity newTileBackground(SpawnData data) {

        return entityBuilder(data)
                .view("tileBackground.png")
                .build();
    }
    @Spawns("tile")
    public Entity newTile(SpawnData data) {
        HitBox hitBox = new HitBox(BoundingShape.box(tileWidthInPixels, tileHeightInPixels));
        var tile = entityBuilder(data)
                .bbox(hitBox)
                .view("tileUnpressed.png")
                .build();
        tile.getViewComponent().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> onTileClick(tile, e.getButton()));

        return tile;
    }
    @Spawns("remainingFlagCountDigit1")
    public Entity newRemainingFlagsCountDigit1(SpawnData data) {

        return entityBuilder(data)
                .view("remainingFlagCountNumber/count0.png")
                .build();
    }
    @Spawns("remainingFlagCountDigit2")
    public Entity newRemainingFlagsCountDigit2(SpawnData data) {

        return entityBuilder(data)
                .view("remainingFlagCountNumber/count0.png")
                .build();
    }
    @Spawns("remainingFlagCountBackground")
    public Entity newRemainingFlagsCountBackground(SpawnData data) {

        return entityBuilder(data)
                .view("remainingFlagCountNumber/background.png")
                .build();
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

        if (gameBoard.checkLosingState().isPresent()) {
            getDialogService().showConfirmationBox("You Lose!" + "\nPlay again?", yes -> {
                if (yes) {
                    publishNewGame();
                } else getGameController().exit();
            });
        }

        if (gameBoard.checkWinningState()) {
            getDialogService().showConfirmationBox("You win!" + "\nPlay again?", yes -> {
                if (yes) {
                    publishNewGame();
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

    private void publishNewGame(){
        getEventBus().fireEvent(new GameEvents.RestartGameEvent());
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
