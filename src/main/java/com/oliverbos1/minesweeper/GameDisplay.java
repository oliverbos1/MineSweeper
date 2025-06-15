package com.oliverbos1.minesweeper;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.oliverbos1.minesweeper.GameBoard.FieldState;
import com.oliverbos1.minesweeper.GameEvents.MoveEvent.MoveType;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.Optional;

import static com.almasb.fxgl.dsl.FXGL.getDialogService;
import static com.almasb.fxgl.dsl.FXGLForKtKt.*;

@SuppressWarnings("preview")
public class GameDisplay implements EntityFactory {
    private Entity[][] boardContent;
    private Entity[][] boardContentBackground;
    private Entity flagCountDigit0 = new Entity();
    private Entity flagCountDigit1 = new Entity();
    private Entity flagCountDigit2 = new Entity();
    private Entity newGameTile = new Entity();
    private Entity difficultyTile = new Entity();

    private static final int tileWidthInPixels = 100;
    private static final int tileHeightInPixels = 100;
    private static final int headerTileHeight = 100;

    private int currentNrTilesHorizontal;

    private int nrTilesHorizontal;
    private Difficulty difficulty;

    public GameDisplay(MineSweeperSettings settings) {
        nrTilesHorizontal = settings.nrTilesHorizontal();
        difficulty = settings.difficulty();
    }

    public void initialize() {
        getGameWorld().addEntityFactory(this);

        spawn("bannerBackground", 0, 0);
        spawn("remainingFlagCountBackground", 10, 10);
        flagCountDigit0 = spawn("remainingFlagCountDigit", 14, 14);
        flagCountDigit1 = spawn("remainingFlagCountDigit", 47, 14);
        flagCountDigit2 = spawn("remainingFlagCountDigit", 80, 14);

        spawn("settingTileBackground", 460, 10);
        newGameTile = scaledSpawnSettingsTile("newGameTile", 464);

        spawn("settingTileBackground", 840, 10);
        scaledSpawnSettingsTile("gameSizeTile", 844);

        spawn("settingTileBackground", 910, 10);
        difficultyTile = scaledSpawnSettingsTile("difficultyTile", 914);

        spawnGridTiles(composeNewSettings());

        getEventBus().addEventHandler(GameEvents
                .BoardUpdatedEvent.BOARD_UPDATED_EVENT_EVENT_TYPE, this::onBoardUpdatedEvent);
    }

    private MineSweeperSettings composeNewSettings() {
        return new MineSweeperSettings(nrTilesHorizontal, difficulty);
    }

    private void spawnGridTiles(MineSweeperSettings settings) {
        boardContentBackground = new Entity[settings.nrTilesHorizontal()][settings.nrTilesHorizontal()];
        boardContent = new Entity[settings.nrTilesHorizontal()][settings.nrTilesHorizontal()];
        currentNrTilesHorizontal = settings.nrTilesHorizontal();

        double scaleFactor = getAppWidth() / (double) (settings.nrTilesHorizontal() * tileWidthInPixels);

        for (int x = 0; x < settings.nrTilesHorizontal(); x++) {
            for (int y = 0; y < settings.nrTilesHorizontal(); y++) {
                double xOffset = (double) (x * getAppWidth()) / settings.nrTilesHorizontal();
                double yOffset =
                        y * ((double) (getAppHeight() - headerTileHeight) / settings.nrTilesHorizontal()) + 100;
                boardContentBackground[x][y] = scaledSpawnGridTile("tileBackground", xOffset, yOffset, scaleFactor);
                boardContent[x][y] = scaledSpawnGridTile("tile", xOffset, yOffset, scaleFactor);
                boardContent[x][y].addComponent(new TileStateComponent(x, y));
            }
        }
    }

    private static Entity scaledSpawnGridTile(String entityName, double x, double y, double scaleFactor) {
        Entity tile = spawn(entityName, x, y);
        tile.setScaleX(scaleFactor);
        tile.setScaleY(scaleFactor);
        tile.setScaleOrigin(new Point2D(0, 0));
        return tile;
    }

    private static Entity scaledSpawnSettingsTile(String entityName, double x) {
        Entity tile = spawn(entityName, x, 14);
        tile.setScaleX(0.7);
        tile.setScaleY(0.7);
        tile.setScaleOrigin(new Point2D(0, 0));
        return tile;
    }

    private void despawnGridTiles(int oldNrTilesHorizontal) {
        for (int x = 0; x < oldNrTilesHorizontal; x++) {
            for (int y = 0; y < oldNrTilesHorizontal; y++) {
                boardContentBackground[x][y].removeFromWorld();
                boardContent[x][y].removeFromWorld();
            }
        }
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
        HitBox tileHitBox = new HitBox(BoundingShape.box(tileWidthInPixels, tileHeightInPixels));
        var tile = entityBuilder(data)
                .bbox(tileHitBox)
                .view("tileUnpressed.png")
                .build();
        tile.getViewComponent().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> onTileClick(tile, e.getButton()));

        return tile;
    }

    @Spawns("settingTileBackground")
    public Entity newSettingTileBackground(SpawnData data) {
        return entityBuilder(data)
                .view("settingTileBackground.png")
                .build();
    }

    @Spawns("gameSizeTile")
    public Entity newGameSizeTile(SpawnData data) {
        HitBox gameSizeTileHitBox = new HitBox(BoundingShape.box(100, 100));
        var gameSizeTile = entityBuilder(data)
                .bbox(gameSizeTileHitBox)
                .view("gameSizeTile.png")
                .build();
        gameSizeTile.getViewComponent().addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> onGameSizeTileClick());

        return gameSizeTile;
    }

    @Spawns("difficultyTile")
    public Entity newDifficultyTile(SpawnData data) {
        HitBox difficultyTileHitBox = new HitBox(BoundingShape.box(100, 100));
        var difficultyTile = entityBuilder(data)
                .bbox(difficultyTileHitBox)
                .view("gameDifficultyTileEASY.png")
                .build();
        difficultyTile.getViewComponent().addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> onDifficultyTileClick());

        return difficultyTile;
    }

    @Spawns("newGameTile")
    public Entity newNewGameTile(SpawnData data) {
        HitBox newGameTileHitBox = new HitBox(BoundingShape.box(100, 100));
        var newGameTile = entityBuilder(data)
                .bbox(newGameTileHitBox)
                .view("newGameTile/newGameTileDefault.png")
                .build();
        newGameTile.getViewComponent().addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> onNewGameTileClick());

        return newGameTile;
    }

    @Spawns("remainingFlagCountDigit")
    public Entity newRemainingFlagsCountDigit(SpawnData data) {
        return entityBuilder(data)
                .view("remainingFlagCountNumber/countIdle.png")
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

        var moveType = switch (button) {
            case PRIMARY -> Optional.of(MoveType.TILE_OPENED);
            case SECONDARY -> Optional.of(MoveType.FLAG_PLACED);
            default -> Optional.<MoveType>empty();
        };
        moveType.ifPresent(m -> getEventBus().fireEvent(new GameEvents.MoveEvent(tileState.x, tileState.y, m)));
    }

    private void onGameSizeTileClick() {
        getDialogService().showInputBoxWithCancel(
                "Amount of tiles (Value from 1 to 50)",
                input -> {
                    try {
                        int parsedInput = Integer.parseInt(input);
                        return !(parsedInput < 1 || parsedInput > 50);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                },
                input -> {
                    if (!input.isBlank()) {
                        nrTilesHorizontal = Integer.parseInt(input);
                    }
                }
        );
    }

    private void onDifficultyTileClick() {
        getDialogService().showChoiceBox(
                "Choose difficulty",
                Difficulty.class,
                diff -> difficulty = diff
        );
    }

    private void onNewGameTileClick() {
        despawnGridTiles(currentNrTilesHorizontal);
        MineSweeperSettings newSettings = composeNewSettings();
        spawnGridTiles(newSettings);
        getEventBus().fireEvent(new GameEvents.NewGameEvent(newSettings));
        setImage(newGameTile, "newGameTile/newGameTileDefault.png");
        setImage(difficultyTile, STR."gameDifficultyTile\{difficulty}.png");
    }

    private void onBoardUpdatedEvent(GameEvents.BoardUpdatedEvent boardUpdatedEvent) {
        GameBoard gameBoard = boardUpdatedEvent.updatedGameBoard;
        for (int x = 0; x < gameBoard.getWidth(); x++) {
            for (int y = 0; y < gameBoard.getHeight(); y++) {
                updateTile(x, y, gameBoard.getField(x, y));
            }
        }

        updateRemainingFlagCount(gameBoard);

        switch (gameBoard.getGameBoardState()) {
            case LOST -> {
                getDialogService().showMessageBox("Too bad, you lose!");
                setImage(newGameTile, "newGameTile/newGameTileLost.png");
            }
            case WON -> {
                getDialogService().showMessageBox("Congrats, you win!");
                setImage(newGameTile, "newGameTile/newGameTileWon.png");
            }
            default -> {
            }
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

    private void updateRemainingFlagCount(GameBoard gameBoard) {
        int digit0;
        int digit1;
        int digit2;

        String remainingFlagCountImageDigit0;
        String remainingFlagCountImageDigit1;

        int remainingFlagCount = Math.max(-99, Math.min(999, gameBoard.getMineAmount() - gameBoard.getFlagAmount()));

        if (remainingFlagCount < 0) {
            digit0 = 0;
            digit1 = remainingFlagCount % 100 / 10 * -1;
            digit2 = remainingFlagCount % 100 % 10 * -1;
        } else {
            digit0 = remainingFlagCount / 100;
            digit1 = remainingFlagCount % 100 / 10;
            digit2 = remainingFlagCount % 100 % 10;
        }

        if (remainingFlagCount < 100 && gameBoard.getMineAmount() < 100) {
            remainingFlagCountImageDigit0 = "remainingFlagCountNumber/countIdle.png";
        } else remainingFlagCountImageDigit0 = STR."remainingFlagCountNumber/count\{digit0}.png";

        if (remainingFlagCount < 10 && gameBoard.getMineAmount() < 10) {
            remainingFlagCountImageDigit1 = "remainingFlagCountNumber/countIdle.png";
        } else remainingFlagCountImageDigit1 = STR."remainingFlagCountNumber/count\{digit1}.png";

        if (remainingFlagCount < -9) {
            remainingFlagCountImageDigit0 = "remainingFlagCountNumber/countMinus.png";
        }

        if (remainingFlagCount < 0 && remainingFlagCount > -10) {
            remainingFlagCountImageDigit1 = "remainingFlagCountNumber/countMinus.png";
        }
        String remainingFlagCountImageDigit2 = STR."remainingFlagCountNumber/count\{digit2}.png";

        setImage(flagCountDigit0, remainingFlagCountImageDigit0);
        setImage(flagCountDigit1, remainingFlagCountImageDigit1);
        setImage(flagCountDigit2, remainingFlagCountImageDigit2);
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