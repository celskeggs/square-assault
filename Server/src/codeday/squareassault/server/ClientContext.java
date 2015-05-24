package codeday.squareassault.server;

import java.util.concurrent.LinkedBlockingQueue;

import codeday.squareassault.protobuf.Messages.ToClient;
import codeday.squareassault.protobuf.Messages.ToServer;

public final class ClientContext {

	public final ServerContext server;
	private final LinkedBlockingQueue<ToClient> sendQueue;
	public final String name;
	private int x, y;
	public final int objectID;
	private boolean canMove = false;

	public ClientContext(ServerContext serverContext, LinkedBlockingQueue<ToClient> sendQueue, String name) {
		this.server = serverContext;
		this.sendQueue = sendQueue;
		this.name = name;
		objectID = serverContext.register(this);
		this.x = serverContext.getMap().getSpawnX();
		this.y = serverContext.getMap().getSpawnY();
		resendStatus();
	}
	
	public synchronized void setCanMove() {
		canMove = true;
	}

	public synchronized void receiveMessage(ToServer taken) {
		if (taken.hasPosition()) {
			if (canMove) {
				int wantX = taken.getPosition().getX(), wantY = taken.getPosition().getY();
				if (server.canMoveTo(wantX, wantY)) {
					x = wantX;
					y = wantY;
					resendStatus();
				}
			}
		} else {
			Logger.warning("Bad message: " + taken);
		}
	}

	public void onRemove() {
		server.destroyObject(objectID);
	}

	public void sendMessage(ToClient built) {
		sendQueue.add(built);
	}

	public void resendStatus() {
		server.updateObjectPosition(objectID, x, y);
	}
}
