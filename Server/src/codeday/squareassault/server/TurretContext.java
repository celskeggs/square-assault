package codeday.squareassault.server;

import codeday.squareassault.protobuf.Messages.ObjectType;

public class TurretContext extends ObjectContext {

	private static final int COOLDOWN = 100, SIZE = 51;
	private int cooldown = COOLDOWN;

	public TurretContext(ServerContext server, int x, int y, int playerID) {
		super(server, x, y, playerID);
	}

	@Override
	protected ObjectType getType() {
		return ObjectType.TURRET;
	}

	@Override
	protected String getIcon() {
		return "turret";
	}

	@Override
	public void tick() {
		super.tick();
		if (cooldown-- <= 0) {
			cooldown = COOLDOWN;
			BulletContext bullet = new BulletContext(server, x + getCenterCoord(), y + getCenterCoord(), objectID, 0, -4);
			bullet.x -= bullet.getCenterCoord();
			bullet.y -= bullet.getCenterCoord();
			server.register(bullet);
		}
	}

	@Override
	public int getRadius() {
		return (SIZE + 1) / 2;
	}

	@Override
	public int getCenterCoord() {
		return 32;
	}
}
