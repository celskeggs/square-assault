package codeday.squareassault.client;

public class Entity {
	public final int objectID;
	public String icon;
	public int x, y;

	public Entity(int object, String icon, int x, int y) {
		this.objectID = object;
		this.icon = icon;
		this.x = x;
		this.y = y;
	}

	public void update(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void updateIcon(String icon) {
		this.icon = icon;
	}
}
