package codeday.squareassault.server;

import codeday.squareassault.protobuf.Messages.ObjectType;

public class TurretContext extends ObjectContext {

	private static final int COOLDOWN = 100, SHORT_COOLDOWN = 10, SIZE = 51;
	private int cooldown = SHORT_COOLDOWN;
	private int health = 100;

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
		if (isDead()) {
			server.delete(this);
			return;
		}
		super.tick();
		if (cooldown-- <= 0) {
			ObjectContext target = acquireTarget();
			if (target != null) {
				cooldown = COOLDOWN;
				BulletContext bullet = BulletContext.withAim(this, target);
				bullet.x -= bullet.getCenterCoord();
				bullet.y -= bullet.getCenterCoord();
				server.register(bullet);
			} else {
				cooldown = SHORT_COOLDOWN;
			}
		}
	}

	private ObjectContext acquireTarget() {
		int minimumSq = Integer.MAX_VALUE;
		ObjectContext minimum = null;
		for (ObjectContext context : server.getObjects()) {
			if (context.getType() == ObjectType.TURRET || context.getType() == ObjectType.PLAYER) {
				if (context.objectID == parentID || context.parentID == parentID) {
					continue; // don't shoot at my team!
				}
				if (context.isDead()) {
					continue; // don't shoot at dead enemies!
				}
				int distanceSq = server.distanceSq(this, context);
				if (distanceSq < minimumSq) {
					minimumSq = distanceSq;
					minimum = context;
				}
			}
		}
		return minimum;
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

	@Override
	protected boolean hasHealth() {
		return true;
	}

	@Override
	public int getHealth() {
		return health <= 0 ? 0 : health;
	}
}
