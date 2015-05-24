package codeday.squareassault.server;

import java.util.Iterator;
import java.util.Random;
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

	private final Random rand = new Random();

	public ClientContext newClient(String name) {
		int spawnID = rand.nextInt(map.getSpawnXCount());
		ClientContext context = new ClientContext(this, name, map.getSpawnX(spawnID), map.getSpawnY(spawnID));
		register(context);
		return context;
	}

	public void register(ObjectContext context) {
		if (objects.containsKey(context.objectID)) {
			throw new RuntimeException("Attempt to recreate object!");
		}
		this.objects.put(context.objectID, context);
		if (context.getType() == ObjectType.PLAYER) {
			for (ObjectContext object : objects.values()) {
				object.resendStatus();
			}
		} else {
			context.resendStatus();
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

	// with health
	public void updateObjectPosition(int objectID, String icon, ObjectType type, int x, int y, int parentID, int health) {
		Messages.SetPosition.Builder builder = Messages.SetPosition.newBuilder();
		builder.setIcon(icon).setObject(objectID).setType(type).setX(x).setY(y).setParent(parentID).setHealth(health);
		broadcast(Messages.ToClient.newBuilder().setPosition(builder).build());
	}

	// without health
	public void updateObjectPosition(int objectID, String icon, ObjectType type, int x, int y, int parentID) {
		Messages.SetPosition.Builder builder = Messages.SetPosition.newBuilder();
		builder.setIcon(icon).setObject(objectID).setType(type).setX(x).setY(y).setParent(parentID);
		broadcast(Messages.ToClient.newBuilder().setPosition(builder).build());
	}

	private void broadcast(Messages.ToClient built) {
		for (ObjectContext object : objects.values()) {
			if (object instanceof ClientContext) {
				((ClientContext) object).sendMessage(built);
			}
		}
	}

	public boolean canMoveTo(int wantX, int wantY, ObjectContext what) {
		int centerCoord = what.getCenterCoord(), radius = what.getRadius();
		int x1 = wantX + centerCoord - radius, x2 = wantX + centerCoord + radius - 1;
		int y1 = wantY + centerCoord - radius, y2 = wantY + centerCoord + radius - 1;
		return isEmptyAt(x1, y1) && isEmptyAt(x2, y1) && isEmptyAt(x1, y2) && isEmptyAt(x2, y2);
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

	public Iterable<ObjectContext> findColliding(final ObjectContext from) {
		return new Iterable<ObjectContext>() {
			@Override
			public Iterator<ObjectContext> iterator() {
				return new Iterator<ObjectContext>() {
					private ObjectContext next = null;
					private Iterator<ObjectContext> iterator = objects.values().iterator();

					@Override
					public boolean hasNext() {
						if (next != null) {
							return true;
						}
						while (iterator.hasNext()) {
							ObjectContext object = iterator.next();
							int dist = object.getRadius() + from.getRadius();
							if (distanceSq(from, object) < dist * dist && object != from) {
								next = object;
								return true;
							}
						}
						return false;
					}

					@Override
					public ObjectContext next() {
						ObjectContext out = next;
						next = null;
						return out;
					}
				};
			}
		};
	}

	public int distanceSq(ObjectContext from, ObjectContext to) {
		int xd = from.x + from.getCenterCoord() - to.x - to.getCenterCoord(), yd = from.y + from.getCenterCoord() - to.y - to.getCenterCoord();
		return xd * xd + yd * yd;
	}

	public Iterable<ObjectContext> getObjects() {
		return objects.values();
	}
}
