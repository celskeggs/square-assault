package codeday.squareassault.server;

import java.util.concurrent.LinkedBlockingQueue;

import codeday.squareassault.protobuf.Messages.ObjectType;
import codeday.squareassault.protobuf.Messages.PlaceTurret;
import codeday.squareassault.protobuf.Messages.SetPosition;
import codeday.squareassault.protobuf.Messages.ToClient;
import codeday.squareassault.protobuf.Messages.ToServer;

public final class ClientContext extends ObjectContext {

	private static final int SIZE = 57;
	public final LinkedBlockingQueue<ToClient> sendQueue = new LinkedBlockingQueue<>();
	public final String name;
	private boolean canMove = false;
	private int turretCount = 1;
	private int health = 100;

	public ClientContext(ServerContext serverContext, String name) {
		super(serverContext, serverContext.getMap().getSpawnX(), serverContext.getMap().getSpawnY());
		this.name = name;
	}

	public synchronized void tick() {
		canMove = true;
	}

	public synchronized void receiveMessage(ToServer taken) {
		if (taken.hasPosition()) {
			performMove(taken.getPosition());
		} else if (taken.hasTurret()) {
			performTurretPlace(taken.getTurret());
		} else {
			Logger.warning("Bad message: " + taken);
		}
	}

	@Override
	protected boolean hasHealth() {
		return true;
	}

	@Override
	public int getHealth() {
		return health <= 0 ? 0 : health;
	}

	public boolean isDead() {
		return getHealth() <= 0;
	}

	private synchronized void performTurretPlace(PlaceTurret turret) {
		if (isDead()) {
			return;
		}
		TurretContext newTurret = new TurretContext(server, turret.getX(), turret.getY(), objectID);
		if (turretCount > 0 && server.canMoveTo(turret.getX(), turret.getY(), newTurret)) {
			turretCount--;
			server.register(newTurret);
		}
	}

	private void performMove(SetPosition position) {
		if (!canMove || isDead()) {
			return;
		}
		int wantX = position.getX(), wantY = position.getY();
		if (server.canMoveTo(wantX, wantY, this)) {
			x = wantX;
			y = wantY;
			resendStatus();
		} else {
			boolean changed = false;
			if (wantX > x) {
				for (int rx = wantX - 1; rx > x; rx--) {
					if (server.canMoveTo(rx, y, this)) {
						x = rx;
						changed = true;
						break;
					}
				}
			} else if (wantX < x) {
				for (int rx = wantX + 1; rx < x; rx++) {
					if (server.canMoveTo(rx, y, this)) {
						x = rx;
						changed = true;
						break;
					}
				}
			}

			if (wantY > y) {
				for (int ry = wantY - 1; ry > y; ry--) {
					if (server.canMoveTo(x, ry, this)) {
						y = ry;
						changed = true;
						break;
					}
				}
			} else if (wantY < y) {
				for (int ry = wantY + 1; ry < y; ry++) {
					if (server.canMoveTo(x, ry, this)) {
						y = ry;
						changed = true;
						break;
					}
				}
			}

			if (changed) {
				resendStatus();
			}
		}
	}

	public void sendMessage(ToClient built) {
		sendQueue.add(built);
	}

	@Override
	protected ObjectType getType() {
		return ObjectType.PLAYER;
	}

	@Override
	protected String getIcon() {
		return "enemy";
	}

	@Override
	public int getRadius() {
		return (SIZE + 1) / 2;
	}

	@Override
	public int getCenterCoord() {
		return 32;
	}

	public void damage(int damage) {
		this.health -= damage;
		if (health < 0) {
			health = 0;
		}
		resendStatus();
	}
}
