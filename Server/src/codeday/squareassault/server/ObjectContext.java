package codeday.squareassault.server;

import codeday.squareassault.protobuf.Messages.ObjectType;

public abstract class ObjectContext {

	public final ServerContext server;
	protected int x;
	protected int y;
	public final int objectID;
	public final int parentID;

	public ObjectContext(ServerContext server, int x, int y) {
		this(server, x, y, -1);
	}

	public ObjectContext(ServerContext server, int x, int y, int parentID) {
		this.server = server;
		this.x = x;
		this.y = y;
		this.objectID = server.getObjectID();
		this.parentID = parentID;
	}

	public void resendStatus() {
		if (hasHealth()) {
			server.updateObjectPosition(objectID, getIcon(), getType(), x, y, parentID, getHealth());
		} else {
			server.updateObjectPosition(objectID, getIcon(), getType(), x, y, parentID);
		}
	}

	protected int getHealth() {
		return 100;
	}

	protected boolean hasHealth() {
		return false;
	}

	public boolean isDead() {
		return hasHealth() && getHealth() <= 0;
	}

	protected abstract String getIcon();

	protected abstract ObjectType getType();

	public void tick() {
		if (server.getObject(parentID) == null) {
			server.delete(this);
		}
	}

	public abstract int getRadius();

	public abstract int getCenterCoord();

	public void damage(int damage) {
		// do nothing by default
	}
}
