/*** In The Name of Allah ***/
package game.Process;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * This class holds the state of game and all of its elements.
 * This class also handles user inputs, which affect the game state.
 * 
 * @author Seyed Mohammad Ghaffarian
 */
public class GameState {

	private GameMap gameMap;
	
	public int locX, locY, diam;
	public boolean gameOver;
	
	private boolean keyUP, keyDOWN, keyRIGHT, keyLEFT;
	private boolean mousePress;
	private int mouseX, mouseY;	
	private KeyHandler keyHandler;
	private MouseHandler mouseHandler;
	
	public GameState() {
		locX = 100;
		locY = 100;
		diam = 32;
		gameOver = false;
		//
		keyUP = false;
		keyDOWN = false;
		keyRIGHT = false;
		keyLEFT = false;
		//
		mousePress = false;
		mouseX = 0;
		mouseY = 0;
		//
		keyHandler = new KeyHandler();
		mouseHandler = new MouseHandler();
	}

	public void setGameMap(GameMap gameMap) {
		this.gameMap = gameMap;
	}
	
	/**
	 * The method which updates the game state.
	 */
	public void update() {
		if (mousePress) {
			int bound = locY - mouseY;
			locY = (Math.abs(bound) > 8) ? (locY > mouseY) ? locY - 8 : locY + 8 : mouseY;
			bound = locX - mouseX;
			locX = (Math.abs(bound) > 8) ? (locX > mouseX) ? locX - 8 : locX + 8 : mouseX;
		}
		if (keyUP) {
			locY -= 8;
		}
		if (keyDOWN) {
			locY += 8;
		}
		if (keyLEFT) {
			locX -= 8;
		}
		if (keyRIGHT) {
			locX += 8;
		}

		locX = Math.max(locX, GameFrame.DRAWING_START_X);
		locX = Math.min(locX, gameMap.numberOfColumns * GameMap.CHANGING_FACTOR - GameMap.CHANGING_FACTOR / 2 + GameFrame.DRAWING_START_X);
		locY = Math.max(locY, GameFrame.DRAWING_START_Y);
		locY = Math.min(locY, gameMap.numberOfRows * GameMap.CHANGING_FACTOR - GameMap.CHANGING_FACTOR / 2 + GameFrame.DRAWING_START_Y);
	}
	
	
	public KeyListener getKeyListener() {
		return keyHandler;
	}
	public MouseListener getMouseListener() {
		return mouseHandler;
	}
	public MouseMotionListener getMouseMotionListener() {
		return mouseHandler;
	}



	/**
	 * The keyboard handler.
	 */
	class KeyHandler extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode())
			{
				case KeyEvent.VK_UP:
					keyUP = true;
					break;
				case KeyEvent.VK_DOWN:
					keyDOWN = true;
					break;
				case KeyEvent.VK_LEFT:
					keyLEFT = true;
					break;
				case KeyEvent.VK_RIGHT:
					keyRIGHT = true;
					break;
				case KeyEvent.VK_ESCAPE:
					gameOver = true;
					break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			switch (e.getKeyCode())
			{
				case KeyEvent.VK_UP:
					keyUP = false;
					break;
				case KeyEvent.VK_DOWN:
					keyDOWN = false;
					break;
				case KeyEvent.VK_LEFT:
					keyLEFT = false;
					break;
				case KeyEvent.VK_RIGHT:
					keyRIGHT = false;
					break;
			}
		}

	}

	/**
	 * The mouse handler.
	 */
	class MouseHandler extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			mouseX = e.getX();
			mouseY = e.getY();
			mousePress = true;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			mousePress = false;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			mouseX = e.getX();
			mouseY = e.getY();
		}
	}
}
