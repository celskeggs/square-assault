package codeday.squareassault.client;

import java.awt.Color;
import java.awt.Graphics;

import codeday.squareassault.protobuf.NewMessages;
import codeday.squareassault.protobuf.NewMessages.Entity;
import codeday.squareassault.protobuf.NewMessages.Map;

public class View {

	public static final long UPDATE_DELAY_MILLIS = 20;
	public static final Color BACKGROUND_COLOR = Color.BLACK;
	private static final int BORDER_SIZE_MIN = 256;
	private static final int BORDER_SIZE_MAX = 320;
	private static final int SHIFT_SCALE_FACTOR = 16;
	private static final int METER_SHIFT = 70, METER_RADIUS = 48;
	private final MainModel context;

	public View(MainModel context) {
		this.context = context;
	}

	private String getIconForRender(NewMessages.Entity ent) {
		if (ent.getType() == NewMessages.EntityType.PLAYER) {
			if (ent.getId() == context.model.getPlayerID()) {
				if (ent.getHealth() <= 0) {
					return "userdead";
				} else {
					return "user";
				}
			} else {
				if (ent.getHealth() <= 0) {
					return "enemydead";
				} else {
					return "enemy";
				}
			}
		} else if (isAncestor(ent, context.model.getPlayerID())) {
			return "user" + ent.getIcon();
		} else if (!ent.hasIcon()) {
			return "none";
		} else {
			return ent.getIcon();
		}
	}

	private NewMessages.Entity getEntityById(int id) {
		for (NewMessages.Entity ent : context.model.getEntityList()) {
			if (ent.getId() == id) {
				return ent;
			}
		}
		return null;
	}

	private boolean isAncestor(NewMessages.Entity entity, int acceptedID) {
		if (entity.getParent() == acceptedID) {
			return true;
		}
		NewMessages.Entity parent = getEntityById(entity.getParent());
		return parent != null && isAncestor(parent, acceptedID);
	}

	public synchronized void paint(Graphics go, int width, int height) {
		synchronized (context) {
			Map map = context.model.getMap();
			for (int i = 0; i < map.getCellCount(); i++) {
				go.drawImage(Loader.load(map.getTilenames(map.getCell(i))), (i % map.getWidth()) * 64 + context.shiftX, (i / map.getWidth()) * 64 + context.shiftY, null);
			}

			NewMessages.Entity player = null;

			for (NewMessages.Entity ent : context.model.getEntityList()) {
				if (ent.getId() == context.model.getPlayerID()) {
					player = ent;
				}
				go.drawImage(Loader.load(getIconForRender(ent)), ent.getX() + context.shiftX, ent.getY() + context.shiftY, null);
				if (ent.getType() == NewMessages.EntityType.TURRET || ent.getType() == NewMessages.EntityType.PLAYER) {
					String str = String.valueOf(ent.getHealth());
					go.drawString(str, ent.getX() + 32 + context.shiftX - go.getFontMetrics().stringWidth(str) / 2, ent.getY() + context.shiftY + 32);
				}
			}

			int health = player.getHealth();
			drawMeter(go, false, width, height, Color.RED, Color.GREEN, health, 100);
			if (player.getPrivate().getTurretCount() != -1) {
				drawMeter(go, true, width, height, Color.BLACK, Color.YELLOW, player.getPrivate().getTurretCount(), player.getPrivate().getTurretMaximum());
			}

			go.setColor(Color.WHITE);
			int h = go.getFontMetrics().getHeight();
			int y = 0;
			for (NewMessages.ChatLine line : context.model.getChatList()) {
				y += h;
				NewMessages.Entity speaker = getEntityById(line.getSpeaker());
				go.drawString("[" + (speaker == null ? "UNKNOWN" : speaker.getName()) + "] " + line.getText(), METER_SHIFT + METER_RADIUS, y);
			}
			go.drawString(">>> " + context.newMessage.toString(), METER_SHIFT + METER_RADIUS, h * (Controller.MAX_CHAT_LINES + 1));
		}
	}

	private void drawMeter(Graphics go, boolean atLeft, int width, int height, Color low, Color high, int health, int max) {
		go.setColor(Color.LIGHT_GRAY);
		int baseX = atLeft ? METER_SHIFT - METER_RADIUS : width - METER_SHIFT - METER_RADIUS;
		go.fillArc(baseX, METER_SHIFT - METER_RADIUS, METER_RADIUS * 2, METER_RADIUS * 2, 290, 320);
		go.setColor(blendColors(low, high, health / (double) max));
		go.fillArc(baseX, METER_SHIFT - METER_RADIUS, METER_RADIUS * 2, METER_RADIUS * 2, 250 - health * 320 / max, health * 320 / max);
		go.setColor(Color.GRAY);
		go.fillArc(baseX, METER_SHIFT - METER_RADIUS, METER_RADIUS * 2, METER_RADIUS * 2, 250, 40);
		go.drawOval(baseX, METER_SHIFT - METER_RADIUS, METER_RADIUS * 2, METER_RADIUS * 2);
	}

	private Color blendColors(Color zero, Color one, double d) {
		if (d < 0) {
			d = 0;
		} else if (d > 1) {
			d = 1;
		}
		double remain = 1 - d;
		return new Color((int) (one.getRed() * d + zero.getRed() * remain), (int) (one.getGreen() * d + zero.getGreen() * remain), (int) (one.getBlue() * d + zero.getBlue() * remain));
	}

	public void tick(int width, int height) {
		Entity ply;
		synchronized (context) {
			ply = getEntityById(context.model.getPlayerID());
			if (ply.getHealth() > 0) {
				int realX = ply.getX() + context.shiftX;
				if (realX < BORDER_SIZE_MIN) {
					context.shiftX += (BORDER_SIZE_MIN - realX) / SHIFT_SCALE_FACTOR;
				}
				if (realX > width - BORDER_SIZE_MAX) {
					context.shiftX -= (realX - width + BORDER_SIZE_MAX) / SHIFT_SCALE_FACTOR;
				}

				int realY = ply.getY() + context.shiftY;
				if (realY < BORDER_SIZE_MIN) {
					context.shiftY += (BORDER_SIZE_MIN - realY) / SHIFT_SCALE_FACTOR;
				}
				if (realY > height - BORDER_SIZE_MAX) {
					context.shiftY -= (realY - height + BORDER_SIZE_MAX) / SHIFT_SCALE_FACTOR;
				}
			} else {
				if (getEntityById(context.model.getPlayerID()).getHealth() <= 0) {
					context.shiftX -= context.dx;
					context.shiftY -= context.dy;
				}
			}
		}
	}
}
