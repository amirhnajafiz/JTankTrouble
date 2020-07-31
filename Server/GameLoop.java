package game.Server;

import game.Process.Bullet;
import game.Process.GameFrame;
import game.Process.GameMap;
import game.Process.GameState;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A very simple structure for the main game loop.
 * THIS IS NOT PERFECT, but works for most situations.
 * Note that to make this work, none of the 2 methods
 * in the while loop (update() and render()) should be
 * long running! Both must execute very quickly, without
 * any waiting and blocking!
 * <p>
 * Detailed discussion on different game loop design
 * patterns is available in the following link:
 * http://gameprogrammingpatterns.com/game-loop.html
 */
public class GameLoop implements Runnable {

    /**
     * Frame Per Second.
     * Higher is better, but any value above 24 is fine.
     */
    public static final int FPS = 30;

    private Vector<User> playersVector;
    private int numberOfPlayers;

    private ArrayList<Bullet> bullets;
    private ExecutorService executorService;

    /**
     * The constructor of the game loop.
     * To create the frame of the game.
     */
    public GameLoop(GameMap gameMap, Vector<User> vector) {
        playersVector = vector;
        this.numberOfPlayers = vector.size();
        initialize(gameMap);
    }

    private void initialize(GameMap gameMap) {
        for (User u : playersVector) {
            GameState state = new GameState();
            u.setState(state);
        }
        gameMap.setPlaces(playersVector);
        for (User u : playersVector) {
            write(gameMap, u);
            u.setCanvas(new GameFrame("JTank", false));
            u.getCanvas().setGameMap(gameMap);
        }
    }

    /**
     * This must be called before the game loop starts.
     */
    public void init() {
        for (User u : playersVector) {
            write(u.getState(), u);
            write(playersVector, u);
        }
        for (User u : playersVector) {
            GameState state = u.getState();
            state.setLimits(u.getCanvas().getGameMap().getNumberOfRows(), u.getCanvas().getGameMap().getNumberOfColumns());
            state.width = u.getCanvas().getImage().getWidth() / 8; // Setting the width and the height
            state.height = u.getCanvas().getImage().getHeight() / 8; // based on the user tank image
        }
        bullets = new ArrayList<>();
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {

        while ((numberOfPlayers == 1 && !playersVector.get(0).getState().gameOver) || (playersVector.size() > 1)) { // onio ke gameOver shod az vector bendazim biroon
            try {
                long start = System.currentTimeMillis();
                for (User u : playersVector) {
                    GameState state = (GameState) read(u);
                    assert state != null;
                    if (state.shotFired) {
                        Bullet bullet = new Bullet(state.locX + state.width / 2, state.locY + state.height / 2, u.getCanvas().getGameMap());
                        bullet.setDirections(state.direction());
                        bullets.add(bullet);
                    }
                    u.setState(state);
                }
                Iterator<Bullet> iterator = bullets.iterator();
                while (iterator.hasNext()) {
                    Bullet bullet = iterator.next();
                    if (bullet.isAlive())
                        executorService.execute(bullet.getMover());
                    else
                        iterator.remove();
                }
                //sasa
                for (User u : playersVector) {
                    // updating the game
                    write(bullets, u);
                    write(playersVector, u);
                    u.setState((GameState) read(u));
                    u.getCanvas().setBullets(bullets);
                    //TODO: add a update method for the bullets
                }
                // calculating the delay for avoiding lags in the game
                long delay = (1000 / FPS) - (System.currentTimeMillis() - start);
                if (delay > 0)
                    Thread.sleep(delay);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        // in tike bayad check beshe
//        for (User u : playersVector) {
//            u.getCanvas().setBullets(bullets);
//            u.getCanvas().render(playersVector);
//        }
    }

    private Object read(User u) {
        try {
            ObjectInputStream in = new ObjectInputStream(u.getClientSocket().getInputStream());
            return in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void write(Object object, User u) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(u.getClientSocket().getOutputStream());
            out.writeObject(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
