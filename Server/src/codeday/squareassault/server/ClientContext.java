package codeday.squareassault.server;

import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import codeday.squareassault.protobuf.Messages;
import codeday.squareassault.protobuf.Messages.ObjectType;
import codeday.squareassault.protobuf.Messages.PlaceTurret;
import codeday.squareassault.protobuf.Messages.SendChat;
import codeday.squareassault.protobuf.Messages.SendPosition;
import codeday.squareassault.protobuf.Messages.ToClient;
import codeday.squareassault.protobuf.Messages.ToServer;
import codeday.squareassault.protobuf.SharedConfig;

public final class ClientContext extends ObjectContext {

	private static final int COOLDOWN = 1000, UPDATE_TICKS = 5, UPDATE_SELF_MUL = 5;
	private static final int TURRETS_MAX = 8;
	private static final int MAX_TURRET_DISTANCE = 5 * 64;
	private static final int MAX_MOVE = 20;
	public final LinkedBlockingQueue<ToClient> sendQueue = new LinkedBlockingQueue<>();
	public final String name;
	private boolean canMove = false;
	private int turretCount = 4;
	private int health = 100;
	private int cooldown = 0;
	private boolean dirty = false, avoidSelf = false;
	private int updateCount = UPDATE_TICKS;
	private int updateSelf = UPDATE_SELF_MUL;

	public ClientContext(ServerContext serverContext, String name) {
		super(serverContext, -100, -100);
		this.name = name;
	}

	public synchronized void tick() {
		if (!server.isRoundInProgress()) {
			cooldown = 0;
			turretCount = 4;
			health = 100;
			dirty = true;
			avoidSelf = false;
			canMove = false;
			if (--updateCount <= 0) {
				updateCount = UPDATE_TICKS;
				resendStatus();
			}
			updateSelf = UPDATE_SELF_MUL;
		} else {
			canMove = true;
			if (--cooldown <= 0) {
				cooldown = COOLDOWN;
				if (turretCount < TURRETS_MAX) {
					turretCount++;
					resendTurretCount();
				}
			}
			if (dirty && --updateCount <= 0) {
				updateCount = UPDATE_TICKS;
				if (--updateSelf != 0) {
					avoidSelf = true;
					resendStatus();
					avoidSelf = false;
				} else {
					resendStatus();
					updateSelf = UPDATE_SELF_MUL;
				}
			}
		}
	}

	@Override
	public void resendStatus() {
		super.resendStatus();
		dirty = false;
	}

	private void resendTurretCount() {
		sendMessage(Messages.ToClient.newBuilder().setCount(Messages.TurretCount.newBuilder().setCount(turretCount).setMaximum(TURRETS_MAX)).build());
	}

	public synchronized void receiveMessage(ToServer taken) {
		if (taken.hasChat()) {
			performChat(taken.getChat());
		} else if (taken.hasPosition()) {
			performMove(taken.getPosition());
		} else if (taken.hasTurret()) {
			performTurretPlace(taken.getTurret());
		} else {
			Logger.warning("Bad message: " + taken);
		}
	}

	private void performChat(SendChat chat) {
		if (chat.getText().startsWith("/")) {
			switch (chat.getText()) {
			case "/start":
				String msg = server.tryStartRound();
				if (msg != null) {
					personalChat("[SERVER MONKEY] " + msg, -1);
				}
				break;
			case "/end":
				msg = server.tryEndRound();
				if (msg != null) {
					personalChat("[SERVER MONKEY] " + msg, -1);
				}
				break;
			}
		} else {
			server.broadcastChat("[" + name + "] " + chat.getText(), objectID);
		}
	}

	private synchronized void performTurretPlace(PlaceTurret turret) {
		if (isDead() || !server.isRoundInProgress()) {
			return;
		}
		TurretContext newTurret = new TurretContext(server, turret.getX(), turret.getY(), objectID);
		if (turretCount > 0 && server.freeLineOfSight(this, newTurret) && server.canMoveTo(turret.getX(), turret.getY(), newTurret) && server.distanceSq(this, newTurret) <= MAX_TURRET_DISTANCE * MAX_TURRET_DISTANCE) {
			turretCount--;
			server.register(newTurret);
			resendTurretCount();
		}
	}

	private void performMove(SendPosition position) {
		if (!canMove || isDead() || !server.isRoundInProgress()) {
			System.out.println("Ignore move!");
			return;
		}
		int wantX = position.getX(), wantY = position.getY();
		{
			int rx = wantX - x, ry = wantY - y;
			if (rx * rx + ry * ry > MAX_MOVE * MAX_MOVE) {
				System.out.println("Overmotion!");
				resendStatus();
				return;
			}
		}
		if (server.canMoveTo(wantX, wantY, this)) {
			x = wantX;
			y = wantY;
			dirty = true;
		} else {
			System.out.println("Intersection!");
			resendStatus(); // because we need to tell the client that they couldn't move
		}
	}

	public void sendMessage(ToClient built) {
		if (avoidSelf && built.hasPosition() && built.getPosition().getObject() == objectID) {
			return;
		}
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
		return SharedConfig.PLAYER_RADIUS;
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

	public void personalChat(String msg, int playerID) {
		sendMessage(Messages.ToClient.newBuilder().setChat(Messages.ChatMessage.newBuilder().setPlayer(playerID).setText(msg)).build());
	}

	private static final Random rand = new Random();

	public void reset() {
		int spawnID = rand.nextInt(server.getMap().getSpawnXCount());
		x = server.getMap().getSpawnX(spawnID);
		y = server.getMap().getSpawnY(spawnID);
		resendStatus();
	}
}
