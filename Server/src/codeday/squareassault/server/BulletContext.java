package codeday.squareassault.server;

import codeday.squareassault.protobuf.Messages.ObjectType;

public class BulletContext extends ObjectContext {

	private static final int SIZE = 23;
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
		for (ObjectContext colliding : server.findColliding(this, ObjectType.PLAYER)) {
			if (colliding.objectID != server.getObject(parentID).parentID) {
				System.out.println("HURT: " + ((ClientContext) colliding));
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
}
