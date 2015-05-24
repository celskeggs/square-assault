package codeday.squareassault.server;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import codeday.squareassault.protobuf.Messages;
import codeday.squareassault.protobuf.Messages.Map;
import codeday.squareassault.protobuf.Messages.ToClient;

public class ServerContext {

	private final CopyOnWriteArrayList<ClientContext> clients = new CopyOnWriteArrayList<>();
	private int nextID = 0;
	private Map map;

	public ServerContext(Map map) {
		this.map = map;
	}

	private synchronized int getObjectID() {
		return nextID++;
	}

	public ClientContext newClient(LinkedBlockingQueue<ToClient> sendQueue, String name) {
		return new ClientContext(this, sendQueue, name);
	}

	public int register(ClientContext context) {
		this.clients.add(context);
		for (ClientContext client : clients) {
			if (client != context) {
				client.resendStatus();
			}
		}
		return getObjectID();
	}

	public void removeClient(ClientContext context) {
		this.clients.remove(context);
		context.onRemove();
	}

	public void tick() {
		for (ClientContext client : clients) {
			client.setCanMove();
		}
	}

	public Map getMap() {
		return map;
	}

	public void destroyObject(int objectID) {
		broadcast(Messages.ToClient.newBuilder().setDisconnect(Messages.Disconnect.newBuilder().setObject(objectID)).build());
	}

	public void updateObjectPosition(int objectID, int x, int y) {
		broadcast(Messages.ToClient.newBuilder().setPosition(Messages.SetPosition.newBuilder().setObject(objectID).setX(x).setY(y)).build());
	}

	private void broadcast(Messages.ToClient built) {
		for (ClientContext client : clients) {
			client.sendMessage(built);
		}
	}

	public boolean canMoveTo(int wantX, int wantY) {
		return isEmptyAt(wantX, wantY) && isEmptyAt(wantX + 63, wantY) && isEmptyAt(wantX, wantY + 63) && isEmptyAt(wantX + 63, wantY + 63);
	}

	private boolean isEmptyAt(int wantX, int wantY) {
		int cX = wantX >> 6; // / 64;
		int cY = wantY >> 6; // / 64;
		if (cX < 0 || cX >= map.getWidth()) {
			return false;
		}
		if (cY < 0 || cY >= map.getHeight()) {
			return false;
		}
		return map.getCells(cX + cY * map.getWidth()) == 0;
	}
}
