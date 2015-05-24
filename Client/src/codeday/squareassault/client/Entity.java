package codeday.squareassault.client;

import codeday.squareassault.protobuf.Messages.ObjectType;

public class Entity {
	public final int objectID;
	private String icon;
	public ObjectType type;
	public int x, y;
	public int parentID;
	private final Context context;

	public Entity(Context context, int object, String icon, ObjectType type, int x, int y, int parentID) {
		this.context = context;
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

	public String getIconForRender() {
		if (objectID == context.getPlayerID()) {
			return "user";
		} else if (isAncestor(context.getPlayerID())) {
			return "user" + icon;
		} else if (icon == null) {
			return "none";
		} else {
			return icon;
		}
	}

	private boolean isAncestor(int acceptedID) {
		if (parentID == acceptedID) {
			return true;
		}
		Entity parent = context.getObjectByID(parentID);
		return parent != null && parent.isAncestor(acceptedID);
	}

	public void updateType(ObjectType type) {
		this.type = type;
	}

	public void updateParent(int parentID) {
		this.parentID = parentID;
	}
}
