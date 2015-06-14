import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable, KeyListener {

	public static int WIDTH = 400;
	public static int HEIGHT = 400;

	private Thread thread;
	private boolean running;

	private BufferedImage image;
	private Graphics2D g;

	private int FPS = 30;
	private double averageFPS;

	private Player player;
	public static ArrayList<Bullet> bullets;
	public static ArrayList<Enemy> enemies;
	public static ArrayList<PowerUp> powerUps;

	private long waveStartTimer;
	private long waveStartTimerDiff;
	private int waveNumber;
	private boolean waveStart;
	private int waveDelay = 1000;

	private long slowDownTimer;
	private long slowDownTimerDiff;
	private int slowDownLenght = 6000;

	public GamePanel() {
		super();
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setFocusable(true);
		requestFocus();
	}

	public void addNotify() {
		super.addNotify();
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
		addKeyListener(this);
	}

	public void run() {
		// TODO Auto-generated method stub
		running = true;

		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D) image.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		player = new Player();
		bullets = new ArrayList<Bullet>();
		enemies = new ArrayList<Enemy>();
		powerUps = new ArrayList<PowerUp>();

		waveStartTimer = 0;
		waveStartTimerDiff = 0;
		waveStart = true;

		long startTime;
		long URDTimeMillis;
		long waitTime;
		long totalTime = 0;

		int frameCount = 0;
		int maxFrameCount = 30;

		long targetTime = 1000 / FPS;

		while (running) {

			startTime = System.nanoTime();

			gameUpdate();
			gameRender();
			gameDraw();

			URDTimeMillis = (System.nanoTime() - startTime) / 1000000;
			waitTime = targetTime - URDTimeMillis;

			try {
				thread.sleep(waitTime);

			} catch (Exception e) {
				// TODO: handle exception
			}

			totalTime += System.nanoTime() - startTime;
			frameCount++;
			if (frameCount == maxFrameCount) {
				averageFPS = 1000.0 / ((totalTime / frameCount) / 1000000);
				frameCount = 0;
				totalTime = 0;
			}
		}
	}

	private void gameUpdate() {
		// TODO Auto-generated method stub

		if (waveStartTimer == 0 && enemies.size() == 0) {
			waveNumber++;
			waveStart = true;
			waveStartTimer = System.nanoTime();
		} else {
			waveStartTimerDiff = (System.nanoTime() - waveStartTimer) / 1000000;
			if (waveStartTimerDiff > waveDelay) {
				waveStart = true;
				waveStartTimer = 0;
				waveStartTimerDiff = 0;
			}
		}

		if (waveStart && enemies.size() == 0) {
			createNewEnemies();
		}

		player.update();

		for (int i = 0; i < bullets.size(); i++) {
			boolean remove = bullets.get(i).update();
			if (remove) {
				bullets.remove(i);
				i++;
			}
		}

		for (int i = 0; i < enemies.size(); i++) {
			enemies.get(i).update();
		}

		for (int i = 0; i < powerUps.size(); i++) {
			boolean remove = powerUps.get(i).update();
			if (remove) {
				powerUps.remove(i);
				i--;
			}
		}

		for (int i = 0; i < bullets.size(); i++) {
			Bullet b = bullets.get(i);
			double bx = b.getX();
			double by = b.getY();
			double br = b.getR();

			for (int j = 0; j < enemies.size(); j++) {
				Enemy e = enemies.get(j);
				double ex = e.getX();
				double ey = e.getY();
				double er = e.getR();

				double dx = bx - ex;
				double dy = by - ey;
				double dist = Math.sqrt(dx * dx + dy * dy);

				if (dist < br + er) {
					e.hit();
					bullets.remove(i);
					i--;
					break;
				}
			}
		}

		for (int i = 0; i < enemies.size(); i++) {
			if (enemies.get(i).isDead()) {
				Enemy e = enemies.get(i);

				double rand = Math.random();
				if (rand < 0.001) {
					powerUps.add(new PowerUp(1, e.getX(), e.getY()));
				} else if (rand < 0.020) {
					powerUps.add(new PowerUp(3, e.getX(), e.getY()));
				} else if (rand < 0.120) {
					powerUps.add(new PowerUp(2, e.getX(), e.getY()));
				} else if (rand < 0.130) {
					powerUps.add(new PowerUp(4, e.getX(), e.getY()));
				} else {
					powerUps.add(new PowerUp(3, e.getX(), e.getY()));
				}

				player.addScore(e.getType() + e.getRank());
				enemies.remove(i);
				i--;

				e.explode();
			}
		}

		if (!player.isRecovering()) {
			int px = player.getX();
			int py = player.getY();
			int pr = player.getR();

			for (int i = 0; i < enemies.size(); i++) {
				Enemy e = enemies.get(i);
				double ex = e.getX();
				double ey = e.getY();
				double er = e.getR();

				double dx = px - ex;
				double dy = py - ey;
				double dist = Math.sqrt(dx * dx + dy * dy);

				if (dist < pr + er) {
					player.loseLife();
				}
			}
		}

		int px = player.getX();
		int py = player.getY();
		int pr = player.getR();

		for (int i = 0; i < powerUps.size(); i++) {
			PowerUp p = powerUps.get(i);
			double x = p.getX();
			double y = p.getY();
			double r = p.getR();

			double dx = px - x;
			double dy = py - y;
			double dist = Math.sqrt(dx * dx * +dy * dy);

			if (player.getPower()< 5) {
				if (dist < pr + r) {
					int type = p.getType();

					if (type == 1) {
						player.gainLife();
					}

					if (type == 2) {
						player.increasePower(1);
					}

					if (type == 3) {
						player.increasePower(2);
					}

					if (type == 4) {
						slowDownTimer = System.nanoTime();
						for (int j = 0; j < enemies.size(); j++) {
							enemies.get(j).setSlow(true);
						}
					}
					powerUps.remove(i);
					i--;
				}
			}
		}

		if (slowDownTimer != 0) {
			slowDownTimerDiff = (System.nanoTime() - slowDownTimer) / 1000000;
			if (slowDownTimerDiff > slowDownLenght) {
				slowDownTimer = 0;
				for (int j = 0; j < enemies.size(); j++) {
					enemies.get(j).setSlow(false);
				}
			}
		}
	}

	private void gameRender() {
		// TODO Auto-generated method stub
		g.setColor(new Color(0, 100, 255));
		g.fillRect(0, 0, WIDTH, HEIGHT);
		g.setColor(Color.WHITE);
		g.drawString("FPS: " + averageFPS, 345, 390);

		player.draw(g);

		for (int i = 0; i < bullets.size(); i++) {
			bullets.get(i).draw(g);
		}

		for (int i = 0; i < enemies.size(); i++) {
			enemies.get(i).draw(g);
		}

		for (int i = 0; i < powerUps.size(); i++) {
			powerUps.get(i).draw(g);
		}

		if (waveStartTimer != 0) {
			g.setFont(new Font("Century Gothic", Font.PLAIN, 18));
			String s = "- W A V E  " + waveNumber + "  -";
			int length = (int) g.getFontMetrics().getStringBounds(s, g)
					.getWidth();
			int alpha = (int) (225 * Math.sin(3.14 * waveStartTimerDiff
					/ waveDelay));

			if (alpha > 255)
				alpha = 255;
			g.setColor((new Color(255, 255, 255, alpha)));
			g.drawString(s, WIDTH / 2 - length / 2, HEIGHT / 2);
		}

		for (int i = 0; i < player.getLives(); i++) {
			g.setColor(Color.WHITE);
			g.fillOval(20 + (20 * i), 20, player.getR() * 2, player.getR() * 2);
			g.setStroke(new BasicStroke(3));
			g.setColor(Color.WHITE.darker());
			g.drawOval(20 + (20 * i), 20, player.getR() * 2, player.getR() * 2);
			g.setStroke(new BasicStroke(1));
		}

		g.setColor(Color.YELLOW);
		g.fillRect(20, 40, player.getPower() * 8, 8);
		g.setColor(Color.YELLOW.darker());
		g.setStroke(new BasicStroke(2));

		for (int i = 0; i < player.getRequiredPower(); i++) {
			g.drawRect(20 + 8 * i, 40, 8, 8);
		}

		g.setStroke(new BasicStroke(1));

		g.setColor(Color.WHITE);
		g.setFont(new Font("Century Gothic", Font.PLAIN, 14));
		g.drawString("Score: " + player.getScore(), WIDTH - 100, 30);

		if (slowDownTimer != 0) {
			g.setColor(Color.WHITE);
			g.drawRect(20, 60, 100, 8);
			g.fillRect(20, 60, (int) (100 - 100.0 * slowDownTimerDiff
					/ slowDownLenght), 8);
		}

	}

	private void gameDraw() {
		// TODO Auto-generated method stub
		Graphics g2 = this.getGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();

	}

	private void createNewEnemies() {
		enemies.clear();
		Enemy e;

		if (waveNumber == 1) {
			for (int i = 0; i < 1; i++) {
				enemies.add(new Enemy(1, 1));
			}
		}

		if (waveNumber == 2) {
			for (int i = 0; i < 1; i++) {
				enemies.add(new Enemy(1, 1));
			}

		}

		if (waveNumber == 3) {
			for (int i = 0; i < 1; i++) {
				enemies.add(new Enemy(1, 1));
			}
		}

		if (waveNumber == 4) {
			for (int i = 0; i < 1; i++) {
				enemies.add(new Enemy(2, 1));
			}
		}

		if (waveNumber == 5) {
			enemies.add(new Enemy(1, 4));
		}

		if (waveNumber == 6) {
			for (int i = 0; i < 1; i++) {
				enemies.add(new Enemy(2, 1));
			}
		}

		if (waveNumber == 7) {
			enemies.add(new Enemy(1, 3));
		}

		if (waveNumber == 8) {
			enemies.add(new Enemy(1, 4));
		}
	}

	public void keyPressed(KeyEvent key) {
		// TODO Auto-generated method stub
		int keyCode = key.getKeyCode();

		if (keyCode == key.VK_LEFT) {
			player.setLeft(true);

		}
		if (keyCode == key.VK_RIGHT) {
			player.setRight(true);
		}
		if (keyCode == key.VK_UP) {
			player.setUp(true);
		}
		if (keyCode == key.VK_DOWN) {
			player.setDown(true);
		}
	}

	public void keyReleased(KeyEvent key) {
		// TODO Auto-generated method stub
		int keyCode = key.getKeyCode();

		if (keyCode == key.VK_LEFT) {
			player.setLeft(false);
		}
		if (keyCode == key.VK_RIGHT) {
			player.setRight(false);
		}
		if (keyCode == key.VK_UP) {
			player.setUp(false);
		}
		if (keyCode == key.VK_DOWN) {
			player.setDown(false);
		}
	}

	public void keyTyped(KeyEvent key) {
		// TODO Auto-generated method stub
	}
}
