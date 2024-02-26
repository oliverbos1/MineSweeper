package com.babymakingjuice.minesweeper;

import java.util.*;

public class GameBoard {

    private final FieldState[][] fields;
    private final int width;
    private final int height;

    public GameBoard(int width, int height) {
        this.width = width;
        this.height = height;
        this.fields = new FieldState[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                fields[x][y] = new FieldState(false, false, false, 0);
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public FieldState getField(int x, int y) {
        return fields[x][y];
    }

    public int getMineAmount() {
        int mineCount = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (getField(x, y).hasMine) {
                    mineCount++;
                }
            }
        }
        return mineCount;
    }

    public int getFlagAmount() {
        int flagCount = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (getField(x, y).hasFlag) {
                    flagCount++;
                }
            }
        }
        return flagCount;
    }

    public void setField(int x, int y, FieldState field) {
        fields[x][y] = field;
    }

    public boolean winningState() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!((getField(x, y).isOpen && !getField(x, y).hasMine) ||
                        (getField(x, y).hasMine && getField(x, y).hasFlag))) {
                    return false;
                }
            }
        }

        return true;
    }

    public Optional<Coordinates> losingState() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (getField(x, y).hasMine && getField(x, y).isOpen) {
                    return Optional.of(new Coordinates(x, y));
                }
            }
        }

        return Optional.empty();
    }

    public int checkAdjacentMines(int xPar, int yPar) {
        return adjacentTileCoordinates(xPar, yPar)
                .stream()
                .filter(coordinate -> getField(coordinate.x(), coordinate.y()).hasMine)
                .toList().size();
    }

    public List<Coordinates> adjacentTileCoordinates(int xPar, int yPar) {
        List<Coordinates> coordinates = new ArrayList<>();

        int xMin = Math.max(xPar - 1, 0);
        int xMax = Math.min(xPar + 1, width - 1);
        int yMin = Math.max(yPar - 1, 0);
        int yMax = Math.min(yPar + 1, height - 1);

        for (int x = xMin; x < xMax + 1; x++) {
            for (int y = yMin; y < yMax + 1; y++) {
                if (x != xPar || y != yPar) {
                    coordinates.add(new Coordinates(x, y));
                }
            }
        }
        return coordinates;
    }

    public record Coordinates(int x, int y) {
    }

    public record FieldState(boolean isOpen, boolean hasMine, boolean hasFlag, int adjacentMineCount) {
        public FieldState withIsOpen(boolean isOpen) {
            return new FieldState(isOpen, hasMine, hasFlag, adjacentMineCount);
        }

        public FieldState withHasMine(boolean hasMine) {
            return new FieldState(isOpen, hasMine, hasFlag, adjacentMineCount);
        }

        public FieldState withHasFlag(boolean hasFlag) {
            return new FieldState(isOpen, hasMine, hasFlag, adjacentMineCount);
        }

        public FieldState withAdjacentMineCount(int adjacentMineCount) {
            return new FieldState(isOpen, hasMine, hasFlag, adjacentMineCount);
        }
    }
}
