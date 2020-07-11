package game.Control;

import java.util.ArrayList;

/**
 * This class known as location controller is
 * the controller to check if the tanks and the
 * walls are overlapping each other or not.
 * Basically it keeps all the wall date and has
 * methods to check overlapping.
 *
 */
public class LocationController {

    // The list of the walls ,each type
    private static ArrayList<Location> locations;
    private static boolean created = false;

    /**
     * The init method will create the objects
     * required.
     *
     */
    public static void init() {
        locations = new ArrayList<>();
        created = true;
    }

    /**
     * A method for adding a new wall location.
     *
     * @param location the new location to add
     */
    public static void add (Location location) {
        if (created)
            locations.add(location);
    }

    /**
     * This method will remove the wall that matches to
     * the locations we give to it.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public static void remove (int x, int y) {
        // Check for class creations
        if (!created)
            return;
        // Search for any match
        for (Location l : locations)
            if (l.isMatch(x, y)) {
                locations.remove(l);
                break;
            }
    }

    public static void removeBinary (int x, int y) {
        // Check for class creations
        if (!created)
            return;
        // Search for any match
        for (Location l : locations)
            if (l.samePlace(x, y)) {
                locations.remove(l);
                break;
            }
    }

    /**
     * This method iterates through the walls
     * and will check the overlapping in tank
     * with each wall.
     *
     * @param x the tank x coordinate
     * @param y the tank y coordinate
     * @return making overlap or not
     */
    public static boolean check (int x, int y) {
        // Check for class creations
        if (!created)
            return true;
        // Search for any overlap
        for (Location l : locations)
            if( l.isOverlap(x, y) )
                return false;
        return true;
    }
}
