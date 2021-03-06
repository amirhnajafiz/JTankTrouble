package game.Process;

import game.Control.Location;
import game.Control.LocationController;

import java.io.Serializable;

/**
 * this class probably represents an implementation for the behaviour of a bullet in the game.
 */
public class Bullet implements Serializable {
    // Fields
    public String imagePath;
    private final int DIAM = 8; //location fields&radios of the circle
    public int locX, locY, direction, lifeTime, firstX, firstY; //Location fields
    public boolean fired, exploded, isRPG, makeSound, firedSound;
    public transient int mapRowsLimit, mapColsLimit, counterFired, counterDead, firstCounter;
    public transient boolean isAlive, justShot; //Status fields
    private transient final long start;
    private transient GameMap gameMap; //Each bullet needs the map
    private transient VectorFactory vectorFactory;
    private transient LocationController locationController;

    /**
     * The constructor of the bullet class.
     *
     * @param locX    the first x coordinate
     * @param locY    the first y coordinate
     * @param gameMap the game map instance
     * @param speed the speed of this bullet
     * @param imagePath this bullet image address
     */
    public Bullet(int locX, int locY, GameMap gameMap, int speed, String imagePath) {
        // The starting point of the square
        this.locX = locX;
        this.locY = locY;
        // Keeping the old coordinate
        firstX = this.locX;
        firstY = this.locY;
        // Setting the limits
        this.mapRowsLimit = gameMap.getNumberOfRows();
        this.mapColsLimit = gameMap.getNumberOfColumns();
        //
        isAlive = true;
        justShot = true;
        fired = true;
        exploded = false;
        isRPG = false;
        makeSound = false;
        firedSound = false;
        //
        lifeTime = 4;
        counterFired = 0;
        counterDead = 0;
        firstCounter = 0;
        //
        this.imagePath = imagePath;
        start = System.currentTimeMillis(); // Keeping the start time
        //
        this.gameMap = gameMap;
        vectorFactory = new VectorFactory(speed);
        this.locationController = gameMap.locationController;
    }

    /**
     * This method will get the tank location and will
     * chose the bullet direction to go.
     *
     * @param directions the tank direction
     */
    public void setDirections(int directions) {
        this.direction = directions;
    }

    /**
     * This method will create a runnable of the BulletMove
     * and will give it back so it can be execute.
     *
     * @return an instance of the runnable
     */
    public BulletMove getMover() {
        return new BulletMove();
    }

    /**
     * A getter method for getting the bullet
     * status.
     * @return dead or not
     */
    public boolean isAlive() { return isAlive; }

    /**
     * This class is an inner class which will
     * update the bullet class.
     * It changes the bullet place and will check
     * the walls and the changes in that the bullet had
     * make in the map.
     */
    private class BulletMove implements Runnable, Serializable {
        /**
         * This method will change the directions of the bullet
         * base on the wall that it hits.
         */
        private void wallChangingWay(Location location) {
            // Getting the locations needed of the wall
            int centerX = locX + DIAM / 2; // We locate the center of
            int centerY = locY + DIAM / 2; // bullet
            // And then check for points of the circle for overlapping
            boolean top = location.isOverlap(centerX, centerY - DIAM / 2 - 4, 0, 0, 0);
            boolean bottom = location.isOverlap(centerX, centerY + DIAM / 2 + 4, 0, 0, 0);
            boolean left = location.isOverlap(centerX - DIAM / 2 - 4, centerY, 0, 0, 0);
            boolean right = location.isOverlap(centerX + DIAM / 2 + 4, centerY, 0, 0, 0);
            if (top && !bottom || bottom && !top) {
                direction = 360 - direction;
            }
            if (left && !right || right && !left) {
                direction = 180 - direction;
            }
        }

        /**
         * This method will update the movement of the
         * bullet.
         * Changing the place and the borders bouncy.
         */
        private void update() {
            // Update the location
            // The walls bouncy
            if (locX <= GameFrame.DRAWING_START_X || locX + DIAM >= mapColsLimit * GameMap.CHANGING_FACTOR + GameFrame.DRAWING_START_X) {
                direction = 180 - direction;
            }
            if (locY <= GameFrame.DRAWING_START_Y || locY + DIAM >= mapRowsLimit * GameMap.CHANGING_FACTOR + GameFrame.DRAWING_START_Y) {
                direction = 360 - direction;
            }
            // Updating the place
            vectorFactory.setTheta(direction);
            vectorFactory.solveTheorem(1);
            locX += (int) vectorFactory.x;
            locY += (int) vectorFactory.y;
            //
            locX = Math.max(locX, GameFrame.DRAWING_START_X); // Setting the new locations based on the limits
            locX = Math.min(locX, mapColsLimit * GameMap.CHANGING_FACTOR + GameFrame.DRAWING_START_X - DIAM);
            locY = Math.max(locY, GameFrame.DRAWING_START_Y);
            locY = Math.min(locY, mapRowsLimit * GameMap.CHANGING_FACTOR + GameFrame.DRAWING_START_Y - DIAM);
        }

        @Override
        public void run() {
            // The time checking
            makeSound = false;
            firedSound = firstCounter < 1;
            int time = (int) ((System.currentTimeMillis() - start) / 1000);
            if (time >= lifeTime && !exploded) {
                counterDead = 0;
                exploded = true;
            }
            if (justShot) {
                if (Math.abs(firstX - locX) > GameMap.CHANGING_FACTOR / 5 || Math.abs(firstY - locY) > GameMap.CHANGING_FACTOR / 5)
                    justShot = false; // This is for avoiding destroying the tank as soon as the bullet fired
            }
            // To check if the bullet is hitting any walls with it's center
            Location location = locationController.bulletWallCheck(locX + DIAM / 2, locY + DIAM / 2);
            if (location != null) {
                if (location.type == 1) {
                    if (!isRPG) {
                        counterDead = 0;
                        exploded = true; // This means that the bullet has hit a breakable wall
                        makeSound = true;
                    }
                    gameMap.binaryMap[location.getBinaryY()][location.getBinaryX()].health--;
                    if (gameMap.binaryMap[location.getBinaryY()][location.getBinaryX()].health < 0) {
                        gameMap.binaryMap[location.getBinaryY()][location.getBinaryX()].setState(0);
                        gameMap.updateStatus();
                    }
                    return;
                } else {
                    wallChangingWay(location);
                }
            }
            if (!exploded)
                update();
            if (counterFired == 8)
                fired = false;
            if (exploded && counterDead == 4)
                isAlive = false;
            counterFired++;
            counterDead++;
            firstCounter++;
        }
    }

    public boolean hitTheTank(int tankX, int tankY, int width, int height) { return Math.abs(System.currentTimeMillis() - start) > 500 && tankX < locX && locX < tankX + width && tankY < locY && locY < tankY + height; }
}