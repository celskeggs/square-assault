package codeday.squareassault.server;

import java.util.concurrent.ConcurrentHashMap;

import codeday.squareassault.protobuf.Messages;
import codeday.squareassault.protobuf.Messages.Map;
import codeday.squareassault.protobuf.Messages.ObjectType;

public class ServerContext {

	private final ConcurrentHashMap<Integer, ObjectContext> objects = new ConcurrentHashMap<>();
	private int nextID = 0;
	private Map map;

	public ServerContext(Map map) {
		this.map = map;
	}

	public synchronized int getObjectID() {
		return nextID++;
	}

	public ClientContext newClient(String name) {
		ClientContext context = new ClientContext(this, name);
		register(context);
		return context;
	}

	public void register(ObjectContext context) {
		if (objects.containsKey(context.objectID)) {
			throw new RuntimeException("Attempt to recreate object!");
		}
		this.objects.put(context.objectID, context);
		for (ObjectContext object : objects.values()) {
			object.resendStatus();
		}
	}

	public void delete(ObjectContext context) {
		this.objects.remove(context.objectID);
		broadcastDestroyObject(context.objectID);
	}

	public void tick() {
		for (ObjectContext object : objects.values()) {
			object.tick();
		}
	}

	public Map getMap() {
		return map;
	}

	public void broadcastDestroyObject(int objectID) {
		broadcast(Messages.ToClient.newBuilder().setDisconnect(Messages.Disconnect.newBuilder().setObject(objectID)).build());
	}

	public void updateObjectPosition(int objectID, String icon, ObjectType type, int x, int y) {
		broadcast(Messages.ToClient.newBuilder().setPosition(Messages.SetPosition.newBuilder().setIcon(icon).setObject(objectID).setType(type).setX(x).setY(y)).build());
	}

	private void broadcast(Messages.ToClient built) {
		for (ObjectContext object : objects.values()) {
			if (object instanceof ClientContext) {
				((ClientContext) object).sendMessage(built);
			}
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

	public ObjectContext getObject(int objectID) {
		return objects.get(objectID);
	}
}
