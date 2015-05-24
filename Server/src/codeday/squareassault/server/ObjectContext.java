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
		server.updateObjectPosition(objectID, getIcon(), getType(), x, y, parentID);
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
}
