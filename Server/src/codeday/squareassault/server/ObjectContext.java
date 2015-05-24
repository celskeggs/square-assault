package codeday.squareassault.server;

import codeday.squareassault.protobuf.Messages.ObjectType;

public abstract class ObjectContext {

	public final ServerContext server;
	protected int x;
	protected int y;
	public final int objectID;

	public ObjectContext(ServerContext server, int x, int y) {
		this.server = server;
		this.x = x;
		this.y = y;
		this.objectID = server.getObjectID();
	}

	public void resendStatus() {
		server.updateObjectPosition(objectID, getIcon(), getType(), x, y);
	}

	protected abstract String getIcon();

	protected abstract ObjectType getType();

	public abstract void tick();
}
