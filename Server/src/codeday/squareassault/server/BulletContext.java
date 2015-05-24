package codeday.squareassault.server;

import codeday.squareassault.protobuf.Messages.ObjectType;

public class BulletContext extends ObjectContext {

	private static final int SIZE = 23;
	private static final float SPEED = 8;
	private final int vx, vy;

	public BulletContext(ServerContext server, int x, int y, int turretID, int vx, int vy) {
		super(server, x, y, turretID);
		this.vx = vx;
		this.vy = vy;
	}

	@Override
	protected ObjectType getType() {
		return ObjectType.BULLET;
	}

	@Override
	protected String getIcon() {
		return "bullet";
	}

	@Override
	public void tick() {
		super.tick();
		this.x += this.vx;
		this.y += this.vy;
		if (!server.canMoveTo(x, y, this)) {
			server.delete(this);
			return;
		}
		boolean any = false;
		for (ObjectContext colliding : server.findColliding(this)) {
			if (!colliding.hasHealth() || colliding.getHealth() <= 0 || colliding.parentID == server.getObject(parentID).parentID) {
				continue;
			}
			if (colliding.objectID != server.getObject(parentID).parentID) {
				colliding.damage(10);
			}
			any = true;
		}
		if (any) {
			server.delete(this);
			return;
		}
		resendStatus();
	}

	@Override
	public int getRadius() {
		return (SIZE + 1) / 2;
	}

	@Override
	public int getCenterCoord() {
		return getRadius();
	}

	public static BulletContext withAim(TurretContext turretContext, ObjectContext target) {
		float aimX, aimY;
		aimX = target.x + target.getCenterCoord() - turretContext.x - turretContext.getCenterCoord();
		aimY = target.y + target.getCenterCoord() - turretContext.y - turretContext.getCenterCoord();
		double magnitude = Math.sqrt(aimX * aimX + aimY * aimY);
		aimX /= magnitude;
		aimY /= magnitude;
		aimX *= SPEED;
		aimY *= SPEED;
		return new BulletContext(target.server, turretContext.x + turretContext.getCenterCoord(), turretContext.y + turretContext.getCenterCoord(), turretContext.objectID, Math.round(aimX), Math.round(aimY));
	}
}
