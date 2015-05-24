package codeday.squareassault.client;

import codeday.squareassault.protobuf.Messages.ObjectType;

public class Entity {
	public final int objectID;
	public String icon;
	public ObjectType type;
	public int x, y;

	public Entity(int object, String icon, ObjectType type, int x, int y) {
		this.objectID = object;
		this.icon = icon;
		this.type = type;
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

	public void updateType(ObjectType type) {
		this.type = type;
	}
}
