package game.Control;

import game.Process.GameMap;

/**
 * This class creates for keeping the data of the
 * game walls.
 * Including the starting coordinates and the ending
 * coordinates to avoid calculations in the game processing.
 *
 */
public class Location {

    // The two points of the walls
    public int type;
    private int binaryX; // In-array indexes
    private int binaryY;
    private int topX; // In-map top left coordinates
    private int topY;
    private int bottomX; // In-map bottom right coordinates
    private int bottomY;

    /**
     * The constructor of the Location class.
     *
     * @param topX the first point x coordinate
     * @param topY the first point y coordinate
     */
    public Location (int binaryX, int binaryY, int topX, int topY, int type) {
        this.binaryX = binaryX;
        this.binaryY = binaryY;
        this.topX = topX;
        this.topY = topY;
        bottomX = topX + GameMap.CHANGING_FACTOR; // We calculate the second point coordinates base on the
        bottomY = topY + GameMap.CHANGING_FACTOR; // changing factor in the game
        this.type = type;
    }

    /**
     * This method checks if the user tank and the wall are
     * overlapping each others or not.
     *
     * @param userX the tank x coordinate
     * @param userY the tank y coordinate
     * @return the result of overlapping
     */
    public boolean isOverlap (int userX, int userY, int bound) {
        return ( topX < userX && bottomX > userX || topX < userX + GameMap.CHANGING_FACTOR / bound && bottomX > userX + GameMap.CHANGING_FACTOR / bound ) &&
                ( topY < userY && bottomY > userY || topY < userY + GameMap.CHANGING_FACTOR /bound && bottomY > userY + GameMap.CHANGING_FACTOR / bound );
    }

    public int getTopX() {
        return binaryX;
    }

    public int getTopY() {
        return binaryY;
    }

    /**
     * This method will check if the given coordinates
     * are the coordinates of this wall or not.
     *
     * @param inputX the input x coordinate
     * @param inputY the input y coordinate
     * @return match the wall or not
     */
    public boolean isMatch (int inputX, int inputY) {
        return inputX == topX && inputY == topY;
    }

    /**
     * This method will check if the given coordinates is the array
     * coordinates of the wall or not.
     * This is for deleting the walls from the binary map.
     *
     * @param inputX the input x coordinate
     * @param inputY the input y coordinate
     * @return is the same wall or not
     */
    public boolean samePlace (int inputX, int inputY) {
        return inputX == binaryX && inputY == binaryY;
    }
}
