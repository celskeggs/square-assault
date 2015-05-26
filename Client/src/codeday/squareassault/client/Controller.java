package codeday.squareassault.client;

import java.awt.event.KeyEvent;

import codeday.squareassault.protobuf.NewMessages;
import codeday.squareassault.protobuf.NewMessages.ChatLine;
import codeday.squareassault.protobuf.NewMessages.Entity;
import codeday.squareassault.protobuf.SharedConfig;

public class Controller {

	public static final int MAX_CHAT_LINES = 10;
	private final MainModel context;

	public Controller(MainModel context) {
		this.context = context;
	}

	private NewMessages.Entity.Builder getEntityBuilderById(int id) {
		for (NewMessages.Entity.Builder ent : context.model.getEntityBuilderList()) {
			if (ent.getId() == id) {
				return ent;
			}
		}
		return null;
	}

	private NewMessages.Entity getEntityById(int id) {
		for (NewMessages.Entity ent : context.model.getEntityList()) {
			if (ent.getId() == id) {
				return ent;
			}
		}
		return null;
	}

	public void mousePress(int x, int y, int button) throws InterruptedException {
		synchronized (context) {
			x -= context.shiftX;
			y -= context.shiftY;
			x -= 32;
			y -= 32;
			context.model.addEntity(NewMessages.Entity.newBuilder().setType(NewMessages.EntityType.TURRET).setX(x).setY(y).setIcon("turret").setHealth(100).setParent(context.model.getPlayerID()));
		}
	}

	public void tick() throws InterruptedException {
		synchronized (context) {
			if (!isDead()) {
				if (context.dx != 0 || context.dy != 0) {
					sendRelativeMove(context.dx, context.dy);
				}
			}
			while (context.model.getChatCount() > MAX_CHAT_LINES) {
				context.model.removeChat(0);
			}
		}
	}

	public void handleKey(int keyCode, char keyChar, boolean isPress) throws InterruptedException {
		synchronized (context) {
			if (keyChar != KeyEvent.CHAR_UNDEFINED && !Character.isISOControl(keyChar)) {
				if (isPress) {
					context.newMessage.append(keyChar);
				}
				return;
			} else if (keyCode == KeyEvent.VK_ENTER) {
				if (isPress) {
					String chatMessage = context.newMessage.toString();
					context.newMessage.setLength(0);
					context.model.addChat(ChatLine.newBuilder().setSpeaker(context.model.getPlayerID()).setText(chatMessage));
				}
				return;
			} else if (keyCode == KeyEvent.VK_BACK_SPACE) {
				if (isPress && context.newMessage.length() > 0) {
					context.newMessage.setLength(context.newMessage.length() - 1);
				}
				return;
			}
			int mul = isPress ? 1 : -1;
			switch (keyCode) {
			case KeyEvent.VK_UP:
				context.dy -= 5 * mul;
				break;
			case KeyEvent.VK_DOWN:
				context.dy += 5 * mul;
				break;
			case KeyEvent.VK_LEFT:
				context.dx -= 5 * mul;
				break;
			case KeyEvent.VK_RIGHT:
				context.dx += 5 * mul;
				break;
			}
			if (context.dx > 5) {
				context.dx = 5;
			} else if (context.dx < -5) {
				context.dx = -5;
			}
			if (context.dy > 5) {
				context.dy = 5;
			} else if (context.dy < -5) {
				context.dy = -5;
			}
		}
	}

	private boolean isDead() {
		return getPlayer().getHealth() <= 0;
	}

	private Entity.Builder getPlayerBuilder() {
		return getEntityBuilderById(context.model.getPlayerID());
	}

	private Entity getPlayer() {
		return getEntityById(context.model.getPlayerID());
	}

	private void sendRelativeMove(int dx, int dy) throws InterruptedException {
		synchronized (context) {
			if (isDead() || tryRelativeMove(dx, dy)) {
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
	}

	private boolean tryRelativeMove(int dx, int dy) throws InterruptedException {
		Entity player = getPlayer();
		int curX = player.getX() + dx, curY = player.getY() + dy;
		if (!canMoveTo(curX, curY)) {
			return false;
		}
		getPlayerBuilder().setX(curX);
		getPlayerBuilder().setY(curY);
		return true;
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
		if (cX < 0 || cX >= context.model.getMap().getWidth()) {
			return false;
		}
		if (cY < 0 || cY >= context.model.getMap().getHeight()) {
			return false;
		}
		return context.model.getMap().getCell(cX + cY * context.model.getMap().getWidth()) == 0;
	}
}
