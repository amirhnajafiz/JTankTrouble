package game.Process;

import game.Control.Location;
import game.Control.LocationController;
import game.Server.GameData;
import game.Server.User;

import java.io.*;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class is our map of the game.
 * It creates the map places the walls and
 * places the tanks in the empty spaces.
 */
public class GameMap implements Serializable {
    // Fields
    public static final int CHANGING_FACTOR = 50; // This is the factor that we show the map bigger size in gui
    public boolean gameOver = false;
    public Cell[][] binaryMap; // The array of the map
    public transient LocationController locationController;
    private int numberOfRows, numberOfColumns;
    private transient Random random = new Random(); // The random instance
    private GameData gameData;
    private boolean[][] mark;

    /**
     * The constructor of the map class.
     *
     * @param gameData           the game setting
     * @param locationController the location controller of this map
     */
    public GameMap(LocationController locationController, GameData gameData) {
        numberOfRows = random.nextInt(6) + 6;     // Max 12
        numberOfColumns = random.nextInt(17) + 7; // Max 23
        this.locationController = locationController;
        this.gameData = gameData;
        init(null);
    }

    private boolean check() {
        mark = new boolean[numberOfRows][numberOfColumns];
        for (int i = 0; i < numberOfRows; i++)
            for (int j = 0; j < numberOfColumns; j++)
                mark[i][j] = false;
        for (int i = 0; i < numberOfRows; i++)
            for (int j = 0; j < numberOfColumns; j++)
                if (binaryMap[i][j].getState() == -1)
                    return dfs(i, j) == gameData.numberOfPeople;
        return true;
    }

    private Integer dfs(int x, int y) {
        int ret = (binaryMap[x][y].getState() == -1 ? 1 : 0);
        mark[x][y] = true;
        for (int px = -1; px <= 1; px++)
            for (int py = -1; py <= 1; py++) {
                int xx = x + px;
                int yy = y + py;
                if (Math.abs(px) + Math.abs(py) != 1)   continue;
                if (!valid(xx, yy) || mark[xx][yy] || binaryMap[xx][yy].getState() == 2)
                    continue;
                ret += dfs(xx, yy);
            }
        return ret;
    }

    private boolean valid(int xx, int yy) {
        return xx >= 0 && yy >= 0 && xx < numberOfRows && yy < numberOfColumns;
    }

    /**
     * This method will create the map game.
     * Also creates the location controller.
     */
    public void init(String address) {
        binaryMap = new Cell[numberOfRows][numberOfColumns];
        locationController.init(); // Creating the controller
        makeGameMap(address);
    }


    /**
     * This method will iterate in the array
     * and will make the map binary form to show it
     * in a big size in gui.
     */
    private void makeGameMap(String address) {
        if (address == null) {
            for (int y = 0; y < numberOfRows; y++) {
                for (int x = 0; x < numberOfColumns; x++) {
                    binaryMap[y][x] = new Cell(random.nextInt(3), random.nextInt(2), gameData.wallHealth);
                    if (random.nextInt(100) % 2 == 0)
                        binaryMap[y][x].setState(0);
                    if (binaryMap[y][x].getState() != 0)
                        locationController.add(new Location(x, y, game.Process.GameFrame.DRAWING_START_X + x * GameMap.CHANGING_FACTOR, game.Process.GameFrame.DRAWING_START_Y + y * GameMap.CHANGING_FACTOR, binaryMap[y][x].getState(), gameData.wallHealth));
                }
            }
        } else {
            try ( FileReader fileReader = new FileReader(new File(address));
                  Scanner scanner = new Scanner(fileReader);
            ){
                numberOfRows = scanner.nextInt();
                numberOfColumns = scanner.nextInt();
                for (int y = 0; y < numberOfRows; y++) {
                    for (int x = 0; x < numberOfColumns; x++) {
                        binaryMap[y][x] = new Cell(scanner.nextInt(), random.nextInt(2), gameData.wallHealth);
                        if (binaryMap[y][x].getState() != 0)
                            locationController.add(new Location(x, y, game.Process.GameFrame.DRAWING_START_X + x * GameMap.CHANGING_FACTOR, game.Process.GameFrame.DRAWING_START_Y + y * GameMap.CHANGING_FACTOR, binaryMap[y][x].getState(), gameData.wallHealth));
                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        updateStatus(); // We need this update
    }

    /**
     * This method will place the tanks into empty spaces.
     */
    public void setPlaces(CopyOnWriteArrayList<User> users) {
        do {
            setUserTanksPositions(users);
        } while (!check());

        flushTank(); // We need to empty the tank places
    }

    private void setUserTanksPositions(CopyOnWriteArrayList<User> users) {
        flushTank();
        for (User u : users) {
            while (true) {
                int x = random.nextInt(numberOfColumns); // A random place for the states
                int y = random.nextInt(numberOfRows);
                if (binaryMap[y][x].getState() == 0) {
                    u.getState().setLocation(x * GameMap.CHANGING_FACTOR + GameFrame.DRAWING_START_X, y * GameMap.CHANGING_FACTOR + GameFrame.DRAWING_START_Y);
                    binaryMap[y][x].setState(-1); // Showing the tank is in this house
                    break;
                }
            }
        }
    }

    /**
     * This method will clear the tank places
     * in the map so when we want to draw the map
     * we don't get any problems.
     */
    private void flushTank() {
        for (int y = 0; y < numberOfRows; y++)
            for (int x = 0; x < numberOfColumns; x++)
                if (binaryMap[y][x].getState() == -1)
                    binaryMap[y][x].setState(0);
    }

    /**
     * This method is for changing the status
     * situations of the map cells.
     */
    public void updateStatus() {
        for (int y = 0; y < numberOfRows; y++)
            for (int x = 0; x < numberOfColumns; x++) {
                binaryMap[y][x].status = 0; // Resetting the status
                if (check(x, y - 1))
                    if (binaryMap[y - 1][x].getState() == 0)
                        binaryMap[y][x].status += 1000; // Up
                if (check(x, y + 1))
                    if (binaryMap[y + 1][x].getState() == 0)
                        binaryMap[y][x].status += 100; // Down
                if (check(x - 1, y))
                    if (binaryMap[y][x - 1].getState() == 0)
                        binaryMap[y][x].status += 10; // Left
                if (check(x + 1, y))
                    if (binaryMap[y][x + 1].getState() == 0)
                        binaryMap[y][x].status += 1; // Right
            }
    }

    private boolean check(int x, int y) {
        return 0 <= x && x < numberOfColumns && 0 <= y && y < numberOfRows;
    }

    /**
     * Getter for number of columns.
     *
     * @return the number of columns
     */
    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    /**
     * Getter for number of rows.
     *
     * @return the number of rows
     */
    public int getNumberOfRows() {
        return numberOfRows;
    }
}
