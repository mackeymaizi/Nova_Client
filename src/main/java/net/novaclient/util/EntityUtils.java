package net.novaclient.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;

public enum EntityUtils
{
	;

	public static final Predicate<Entity> IS_NOT_SELF =
		e -> !e.equals(NovaClient.MC.player);

	public static Vec3 getLerpedPos(Entity e, float partialTicks)
	{
		if(e.isRemoved())
			return e.position();

		double x = Mth.lerp(partialTicks, e.xo, e.getX());
		double y = Mth.lerp(partialTicks, e.yo, e.getY());
		double z = Mth.lerp(partialTicks, e.zo, e.getZ());
		return new Vec3(x, y, z);
	}

	public static AABB getLerpedBox(Entity e, float partialTicks)
	{
		if(e.isRemoved())
			return e.getBoundingBox();

		Vec3 offset = getLerpedPos(e, partialTicks).subtract(e.position());
		return e.getBoundingBox().move(offset);
	}

	public static <T extends Entity> List<T> getAliveEntities(Class<T> type)
	{
		return getAliveEntities(type, e -> true);
	}

	public static <T extends Entity> List<T> getAliveEntities(Class<T> type,
		Predicate<T> filter)
	{
		List<T> list = new ArrayList<>();
		if(NovaClient.MC.level == null)
			return list;

		for(Entity e : NovaClient.MC.level.entitiesForRendering())
			if(e.isAlive() && type.isInstance(e) && filter.test(type.cast(e)))
				list.add(type.cast(e));

		return list;
	}

	public static List<Player> getPlayers()
	{
		return getAliveEntities(Player.class, IS_NOT_SELF::test);
	}

	public static List<ItemEntity> getItemEntities()
	{
		return getAliveEntities(ItemEntity.class);
	}

	public static Stream<Entity> stream()
	{
		if(NovaClient.MC.level == null)
			return Stream.empty();

		return java.util.stream.StreamSupport
			.stream(NovaClient.MC.level.entitiesForRendering().spliterator(),
				false)
			.filter(Entity::isAlive);
	}
}
