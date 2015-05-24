package codeday.squareassault.client;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import codeday.squareassault.protobuf.Messages;
import codeday.squareassault.protobuf.Messages.ChatMessage;
import codeday.squareassault.protobuf.SharedConfig;
import codeday.squareassault.protobuf.Messages.Connect;
import codeday.squareassault.protobuf.Messages.Disconnect;
import codeday.squareassault.protobuf.Messages.Map;
import codeday.squareassault.protobuf.Messages.ObjectType;
import codeday.squareassault.protobuf.Messages.SetPosition;
import codeday.squareassault.protobuf.Messages.TurretCount;

public class Context {

	private static final int SNAP_DISTANCE = 10;
	public static final int MAX_CHAT_LINES = 10;
	private int objectID;
	private final Network net;
	public String[] cells;
	public int[][] backgroundImages;
	public final CopyOnWriteArrayList<Entity> entities = new CopyOnWriteArrayList<>();
	private int x, y, dx, dy, health;
	public int turretCount = -1, turretMaximum = 1;
	public View view;
	public final ArrayList<String> chatMessages = new ArrayList<>();
	public final StringBuilder newMessage = new StringBuilder();

	public Context(Network net) {
		this.net = net;
	}

	public void handleKey(int keyCode, char keyChar, boolean isPress) throws InterruptedException {
		if (keyChar != KeyEvent.CHAR_UNDEFINED && !Character.isISOControl(keyChar)) {
			if (isPress) {
				newMessage.append(keyChar);
			}
			return;
		} else if (keyCode == KeyEvent.VK_ENTER) {
			if (isPress) {
				net.send(Messages.ToServer.newBuilder().setChat(Messages.SendChat.newBuilder().setText(newMessage.toString())).build());
				newMessage.setLength(0);
			}
			return;
		} else if (keyCode == KeyEvent.VK_BACK_SPACE) {
			if (isPress && newMessage.length() > 0) {
				newMessage.setLength(newMessage.length() - 1);
			}
			return;
		}
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
		if (isDead()) {
			if (view != null) {
				view.shiftViewForSpectate(dx, dy);
			}
		} else {
			if (dx != 0 || dy != 0) {
				sendRelativeMove(dx, dy);
			}
		}
	}

	private void sendRelativeMove(int dx, int dy) throws InterruptedException {
		if (tryRelativeMove(dx, dy)) {
			return;
		}

		if (dx > 0) {
			for (int rx = dx - 1; rx > 0; rx--) {
				if (tryRelativeMove(rx, 0)) {
					break;
				}
			}
		} else if (dx < 0) {
			for (int rx = dx + 1; rx < 0; rx++) {
				if (tryRelativeMove(rx, 0)) {
					break;
				}
			}
		}

		if (dy > 0) {
			for (int ry = dy - 1; ry > 0; ry--) {
				if (tryRelativeMove(0, ry)) {
					break;
				}
			}
		} else if (dy < 0) {
			for (int ry = dy + 1; ry < 0; ry++) {
				if (tryRelativeMove(0, ry)) {
					break;
				}
			}
		}
	}

	private boolean tryRelativeMove(int dx, int dy) throws InterruptedException {
		if (isDead()) {
			return true;
		} else {
			int curX = this.x + dx, curY = this.y + dy;
			if (!canMoveTo(curX, curY)) {
				return false;
			}
			net.send(Messages.ToServer.newBuilder().setPosition(Messages.SetPosition.newBuilder().setX(curX).setY(curY)).build());
			this.x = curX;
			this.y = curY;
			return true;
		}
	}

	public boolean isDead() {
		return this.getHealth() <= 0;
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
		} else {
			System.out.println("Couldn't find object: " + disconnect.getObject() + "; " + entities);
		}
	}

	public synchronized void handleSetPosition(SetPosition position) {
		if (position.getObject() == objectID) {
			int tx = position.getX(), ty = position.getY();
			if ((tx - x) * (tx - x) + (ty - y) * (ty - y) >= SNAP_DISTANCE * SNAP_DISTANCE) {
				x = tx;
				y = ty;
			}
			if (position.hasHealth()) {
				this.health = position.getHealth();
			}
		}
		for (Iterator<Entity> iterator = entities.iterator(); iterator.hasNext();) {
			Entity e = iterator.next();
			if (e.objectID == position.getObject()) {
				if (position.hasIcon()) {
					e.updateIcon(position.getIcon());
				}
				if (position.hasType()) {
					e.updateType(position.getType());
				}
				if (position.hasHealth()) {
					e.updateHealth(position.getHealth());
				}
				e.update(position.getX(), position.getY());
				return;
			}
		}
		Entity entity = new Entity(this, position.getObject(), position.getIcon(), position.hasType() ? position.getType() : ObjectType.PLAYER, position.getX(), position.getY(), position.getParent());
		if (position.hasHealth()) {
			entity.updateHealth(position.getHealth());
		}
		entities.add(entity);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getPlayerID() {
		return objectID;
	}

	public void mousePress(int x, int y, int button) throws InterruptedException {
		net.send(Messages.ToServer.newBuilder().setTurret(Messages.PlaceTurret.newBuilder().setX(x - 32).setY(y - 32)).build());
	}

	public Entity getObjectByID(int parentID) {
		for (Entity ent : entities) {
			if (ent.objectID == parentID) {
				return ent;
			}
		}
		return null;
	}

	public int getHealth() {
		return health;
	}

	public void handleTurretCount(TurretCount count) {
		turretCount = count.getCount();
		turretMaximum = count.getMaximum();
	}

	public boolean canMoveTo(int wantX, int wantY) {
		// hard-coded client values
		int centerCoord = 32, radius = SharedConfig.PLAYER_RADIUS;
		int x1 = wantX + centerCoord - radius, x2 = wantX + centerCoord + radius - 1;
		int y1 = wantY + centerCoord - radius, y2 = wantY + centerCoord + radius - 1;
		return isEmptyAt(x1, y1) && isEmptyAt(x2, y1) && isEmptyAt(x1, y2) && isEmptyAt(x2, y2);
	}

	private boolean isEmptyAt(int wantX, int wantY) {
		int cX = wantX >> 6; // / 64;
		int cY = wantY >> 6; // / 64;
		if (cX < 0 || cX >= backgroundImages.length) {
			return false;
		}
		if (cY < 0 || cY >= backgroundImages[cX].length) {
			return false;
		}
		return backgroundImages[cX][cY] == 0;
	}

	public void handleChat(ChatMessage chat) {
		synchronized (chatMessages) {
			chatMessages.add(chat.getText());
			while (chatMessages.size() > MAX_CHAT_LINES) {
				chatMessages.remove(0);
			}
		}
	}
}
