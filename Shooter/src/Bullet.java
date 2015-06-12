import java.awt.Color;
import java.awt.Graphics2D;

public class Bullet {
	double x;
	double y;
	int r;

	double dx;
	double dy;
	double rad;
	double speed;

	public Color color1;

	public Bullet(double angle, double x, double y) {
		this.x = x;
		this.y = y;
		r = 2;

		rad = Math.toRadians(angle);

		speed = 10;

		dx = Math.cos(rad) * speed;
		dy = Math.sin(rad) * speed;

		color1 = Color.YELLOW;

	}

	public boolean update() {
		// TODO Auto-generated method stub

		x += dx;
		y += dy;

		if (x < r || x > GamePanel.WIDTH + r || y < -r || y > GamePanel.HEIGHT)
			return true;
		return false;
	}

	public void draw(Graphics2D g) {
		// TODO Auto-generated method stub
		g.setColor(color1);
		g.fillOval((int) (x - r), (int) (y - r), 2 * r, 2 * r);
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public int getR() {
		return r;
	}

	public void setR(int r) {
		this.r = r;
	}
	
	

}
