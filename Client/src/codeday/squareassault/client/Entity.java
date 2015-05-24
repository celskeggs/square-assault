package codeday.squareassault.client;

import codeday.squareassault.protobuf.Messages.ObjectType;

public class Entity {
	public final int objectID;
	private String icon;
	public ObjectType type;
	public int x, y;
	public int parentID;

	public Entity(int object, String icon, ObjectType type, int x, int y, int parentID) {
		this.objectID = object;
		this.parentID = parentID;
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

	public String getIconForRender(int playerID) {
		if (objectID == playerID) {
			return "user";
		} else if (parentID == playerID) {
			return "user" + icon;
		} else if (icon == null) {
			return "none";
		} else {
			return icon;
		}
	}

	public void updateType(ObjectType type) {
		this.type = type;
	}

	public void updateParent(int parentID) {
		this.parentID = parentID;
	}
}
