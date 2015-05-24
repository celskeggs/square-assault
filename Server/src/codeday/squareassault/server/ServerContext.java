package codeday.squareassault.server;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import codeday.squareassault.protobuf.Messages;
import codeday.squareassault.protobuf.Messages.Map;
import codeday.squareassault.protobuf.Messages.ObjectType;

public class ServerContext {

	private final ConcurrentHashMap<Integer, ObjectContext> objects = new ConcurrentHashMap<>();
	private int nextID = 0;
	private Map map;
	private final AtomicBoolean roundInProgress = new AtomicBoolean();

	public ServerContext(Map map) {
		this.map = map;
	}
	
	public boolean isRoundInProgress() {
		return roundInProgress.get();
	}

	public synchronized int getObjectID() {
		return nextID++;
	}

	public ClientContext newClient(String name) {
		if (name.toLowerCase().contains("server")) {
			throw new RuntimeException("Bad name.");
		}
		ClientContext context = new ClientContext(this, name);
		context.reset();
		register(context);
		broadcastChat("[SERVER MONKEY] Welcome, " + name + "!", -1);
		String intromsg = isRoundInProgress() ? "[SERVER MONKEY] You have joined a round-in-progress!" : "[SERVER MONKEY] No round is in progress. Feel free to start one with \"/start\"!";
		context.personalChat(intromsg, -1);
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
		if (context instanceof ClientContext) {
			broadcastChat("[SERVER MONKEY] Goodbye, " + ((ClientContext) context).name + "!", -1);
		}
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

	public boolean freeLineOfSight(ObjectContext from, ObjectContext to) {
		int x1 = from.x + from.getCenterCoord(), x2 = to.x + to.getCenterCoord();
		int y1 = from.y + from.getCenterCoord(), y2 = to.y + to.getCenterCoord();
		if (x1 > x2) {
			int t = x1;
			x1 = x2;
			x2 = t;
			t = y1;
			y1 = y2;
			y2 = t;
		}
		// now x1 <= x2
		if (Math.abs(y2 - y1) > x2 - x1) {
			float islope = (x2 - x1) / (float) (y2 - y1);
			if (y1 < y2) {
				for (int y = y1; y <= y2; y++) {
					int x = Math.round(islope * (y - y1) + x1);
					if (!isEmptyAt(x, y)) {
						return false;
					}
				}
			} else {
				for (int y = y2; y <= y1; y++) {
					int x = Math.round(islope * (y - y1) + x1);
					if (!isEmptyAt(x, y)) {
						return false;
					}
				}
			}
		} else {
			float slope = (y2 - y1) / (float) (x2 - x1);
			for (int x = x1; x <= x2; x++) {
				int y = Math.round(slope * (x - x1) + y1);
				if (!isEmptyAt(x, y)) {
					return false;
				}
			}
		}
		return true;
	}

	public void broadcastChat(String text, int objectID) {
		System.out.println("CHAT: " + text + " from " + objectID);
		broadcast(Messages.ToClient.newBuilder().setChat(Messages.ChatMessage.newBuilder().setPlayer(objectID).setText(text)).build());
	}

	public String tryStartRound() {
		if (isRoundInProgress()) {
			return "A round is already in progress!";
		}
		int count = 0;
		for (ObjectContext context : objects.values()) {
			if (context.getType() == ObjectType.PLAYER) {
				count++;
			}
		}
		if (count < 2) {
			return "There must be at least two players!";
		}
		for (ObjectContext context : objects.values()) {
			if (context instanceof ClientContext) {
				((ClientContext) context).reset();
			}
		}
		roundInProgress.set(true);
		broadcastChat("[SERVER MONKEY] The round has been started!", -1);
		broadcastChat("[SERVER MONKEY] There are " + count + " participants!", -1);
		return null;
	}

	public String tryEndRound() {
		if (!isRoundInProgress()) {
			return "No round is in progress!";
		}
		int count = 0;
		String name = null;
		for (ObjectContext context : objects.values()) {
			if (context.getType() == ObjectType.PLAYER && !context.isDead()) {
				count++;
				name = ((ClientContext) context).name;
			}
		}
		if (count >= 2 || name == null) {
			return "Exactly one player may remain!";
		}
		roundInProgress.set(false);
		broadcastChat("[SERVER MONKEY] The round has completed!", -1);
		broadcastChat("[SERVER MONKEY] The winner is... " + name + "!", -1);
		return null;
	}
}
