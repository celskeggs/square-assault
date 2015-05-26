package codeday.squareassault.protobuf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import codeday.squareassault.protobuf.NewMessages.Entity;
import codeday.squareassault.protobuf.NewMessages.Model;
import codeday.squareassault.protobuf.NewMessages.Model.Builder;
import codeday.squareassault.protobuf.NewMessages.PrivateData;

public class DiffEngine {

	public static Model diff(Model old, Builder updated) {
		NewMessages.Model.Builder out = NewMessages.Model.newBuilder();
		// note: map, playerID, and protocol cannot be updated currently.
		Map<Integer, NewMessages.Entity> oldEnts = checkEntitiesUnique(old.getEntityList());
		Map<Integer, NewMessages.Entity> newEnts = checkEntitiesUnique(updated.getEntityList());
		for (NewMessages.Entity updatedEntity : updated.getEntityList()) {
			if (updatedEntity.hasId()) {
				if (oldEnts.containsKey(updatedEntity.getId())) {
					NewMessages.Entity entDiff = diffEntities(oldEnts.get(updatedEntity.getId()), updatedEntity);
					if (entDiff != null) {
						out.addEntity(entDiff);
					}
				} else {
					out.addEntity(updatedEntity);
				}
			} else {
				out.addEntity(updatedEntity);
			}
		}
		oldEnts.keySet().removeAll(newEnts.keySet());
		for (Integer i : oldEnts.keySet()) { // everything that is now removed
			out.addEntity(NewMessages.Entity.newBuilder().setId(i).setType(NewMessages.EntityType.NONEXISTENT));
		}
		Set<Integer> existingChats = checkChatsUnique(old.getChatList());
		// note: attributes of chat messages cannot be updated currently
		for (NewMessages.ChatLine updatedMessage : updated.getChatList()) {
			if (!updatedMessage.hasUid() || !existingChats.contains(updatedMessage.getUid())) {
				out.addChat(updatedMessage);
			}
		}
		return out.build();
	}

	private static Entity diffEntities(Entity entity, Entity updatedEntity) {
		boolean any = false;
		NewMessages.Entity.Builder build = NewMessages.Entity.newBuilder().setId(entity.getId());
		if (entity.getId() != updatedEntity.getId() || !entity.hasId() || !updatedEntity.hasId()) {
			throw new RuntimeException("Invalid diff targets!");
		}
		if (updatedEntity.getType() != entity.getType()) {
			build.setType(updatedEntity.getType());
			any = true;
		}
		if (updatedEntity.getX() != entity.getX()) {
			build.setX(updatedEntity.getX());
			any = true;
		}
		if (updatedEntity.getY() != entity.getY()) {
			build.setY(updatedEntity.getY());
			any = true;
		}
		if (!Objects.equals(updatedEntity.getName(), entity.getName())) {
			build.setName(updatedEntity.getName());
			any = true;
		}
		if (!Objects.equals(updatedEntity.getIcon(), entity.getIcon())) {
			build.setIcon(updatedEntity.getIcon());
			any = true;
		}
		NewMessages.PrivateData pdiff = diffPrivate(entity.getPrivate(), updatedEntity.getPrivate());
		if (pdiff != null) {
			build.setPrivate(pdiff);
			any = true;
		}
		if (updatedEntity.getHealth() != entity.getHealth()) {
			build.setHealth(updatedEntity.getHealth());
			any = true;
		}
		if (updatedEntity.getParent() != entity.getParent()) {
			build.setParent(updatedEntity.getParent());
			any = true;
		}
		if (any) {
			return build.build();
		} else {
			return null;
		}
	}

	private static PrivateData diffPrivate(PrivateData oldPrivate, PrivateData updatedPrivate) {
		boolean any = false;
		NewMessages.PrivateData.Builder build = NewMessages.PrivateData.newBuilder();
		if (oldPrivate.getTurretCount() != updatedPrivate.getTurretCount()) {
			build.setTurretCount(updatedPrivate.getTurretCount());
			any = true;
		}
		if (oldPrivate.getTurretMaximum() != updatedPrivate.getTurretMaximum()) {
			build.setTurretMaximum(updatedPrivate.getTurretMaximum());
			any = true;
		}
		if (any) {
			return build.build();
		} else {
			return null;
		}
	}

	private static Map<Integer, Entity> checkEntitiesUnique(List<Entity> entityList) {
		HashMap<Integer, Entity> total = new HashMap<>();
		for (Entity ent : entityList) {
			if (total.containsKey(ent.getId())) {
				throw new RuntimeException("Entity copy: " + ent + " and " + total.get(ent.getId()));
			} else {
				total.put(ent.getId(), ent);
			}
		}
		return total;
	}

	private static Set<Integer> checkChatsUnique(List<NewMessages.ChatLine> entityList) {
		HashSet<Integer> total = new HashSet<>();
		for (NewMessages.ChatLine chat : entityList) {
			if (total.contains(chat.getUid())) {
				throw new RuntimeException("Chat copy: " + chat);
			} else {
				total.add(chat.getUid());
			}
		}
		return total;
	}
}
