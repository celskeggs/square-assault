package codeday.squareassault.client;

import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import codeday.squareassault.protobuf.Messages;
import codeday.squareassault.protobuf.Messages.Connect;
import codeday.squareassault.protobuf.Messages.Disconnect;
import codeday.squareassault.protobuf.Messages.Map;
import codeday.squareassault.protobuf.Messages.ObjectType;
import codeday.squareassault.protobuf.Messages.SetPosition;

public class Context {

	private int objectID;
	private final Network net;
	public String[] cells;
	public int[][] backgroundImages;
	public final CopyOnWriteArrayList<Entity> entities = new CopyOnWriteArrayList<>();
	private int x, y, dx, dy;

	public Context(Network net) {
		this.net = net;
	}

	public void handleKey(int keyCode, char keyChar, boolean isPress) throws InterruptedException {
		int mul = isPress ? 1 : -1;
		switch (keyCode) {
		case KeyEvent.VK_UP:
			dy -= 5 * mul;
			break;
		case KeyEvent.VK_DOWN:
			dy += 5 * mul;
			break;
		case KeyEvent.VK_LEFT:
			dx -= 5 * mul;
			break;
		case KeyEvent.VK_RIGHT:
			dx += 5 * mul;
			break;
		}
		if (dx > 5) {
			dx = 5;
		} else if (dx < -5) {
			dx = -5;
		}
		if (dy > 5) {
			dy = 5;
		} else if (dy < -5) {
			dy = -5;
		}
	}

	public void tick() throws InterruptedException {
		if (dx != 0 || dy != 0) {
			sendRelativeMove(dx, dy);
		}
	}

	private void sendRelativeMove(int x, int y) throws InterruptedException {
		int curX = this.x + x, curY = this.y + y;
		net.send(Messages.ToServer.newBuilder().setPosition(Messages.SetPosition.newBuilder().setX(curX).setY(curY)).build());
	}

	public void handleMap(Map map) {
		int width = map.getWidth();
		int height = map.getHeight();
		backgroundImages = new int[width][height];
		cells = map.getTilenamesList().toArray(new String[map.getTilenamesCount()]);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				backgroundImages[x][y] = map.getCells(x + y * width);
			}
		}
	}

	public void handleConnectInfo(Connect info) {
		if (info.hasMap()) {
			handleMap(info.getMap());
		}
		if (info.hasObjectID()) {
			this.objectID = info.getObjectID();
		}
	}

	public void handleDisconnect(Disconnect disconnect) {
		Entity target = null;
		for (Entity e : entities) {
			if (e.objectID == disconnect.getObject()) {
				target = e;
			}
		}
		if (target != null) {
			if (!entities.remove(target)) {
				System.out.println("Failed to disconnect object: " + target);
			}
			System.out.println("Disconnected object...");
		} else {
			System.out.println("Couldn't find object: " + disconnect.getObject() + "; " + entities);
		}
	}

	public synchronized void handleSetPosition(SetPosition position) {
		System.out.println("Set position: " + position.getIcon() + "::" + position.getType());
		String icon = "user";
		if (position.getObject() == objectID) {
			this.x = position.getX();
			this.y = position.getY();
		} else if (position.hasIcon()) {
			icon = position.getIcon();
		}
		for (Iterator<Entity> iterator = entities.iterator(); iterator.hasNext();) {
			Entity e = iterator.next();
			if (e.objectID == position.getObject()) {
				if (position.hasIcon()) {
					e.updateIcon(icon);
				}
				if (position.hasType()) {
					e.updateType(position.getType());
				}
				e.update(position.getX(), position.getY());
				return;
			}
		}
		entities.add(new Entity(position.getObject(), icon, position.hasType() ? position.getType() : ObjectType.PLAYER, position.getX(), position.getY()));
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void mousePress(int x, int y, int button) throws InterruptedException {
		net.send(Messages.ToServer.newBuilder().setTurret(Messages.PlaceTurret.newBuilder().setX(x - 32).setY(y - 32)).build());
	}
}
