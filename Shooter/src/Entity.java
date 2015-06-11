import java.awt.Graphics2D;

public abstract class Entity {
	public int x;
	public int y;
	public int r;

	public int dx;
	public int dy;

	public int getx() {
		return x;
	}

	public void setx() {
		this.x = x;
	}

	public int gety() {
		return y;
	}

	public void sety() {
		this.y = y;
	}

	public int getr() {
		return r;
	}

	public void setr() {
		this.r = r;
	}

	public int dx() {
		return dx;
	}

	public void setdx() {
		this.dx = dx;
	}

	public int getdy() {
		return dy;
	}

	public void setdy() {
		this.dy = dy;
	}

	public void update() {

	}

	public void draw(Graphics2D g) {

	}

}
