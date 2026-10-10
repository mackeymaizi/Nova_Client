package net.novaclient.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import net.novaclient.module.Module;

public final class EventBus
{
	public interface Handler<T>
	{
		void accept(T event);
	}

	private record Listener(Object owner, Handler<?> handler, int priority)
	{}

	private static final Map<Class<?>, List<Listener>> MAP =
		new ConcurrentHashMap<>();

	private EventBus()
	{}

	public static <T> void subscribe(Class<T> type, Object owner,
		Handler<T> handler)
	{
		subscribe(type, owner, handler, 0);
	}

	public static <T> void subscribe(Class<T> type, Object owner,
		Handler<T> handler, int priority)
	{
		List<Listener> list =
			MAP.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>());
		list.add(new Listener(owner, handler, priority));
		List<Listener> sorted = new ArrayList<>(list);
		sorted.sort((a, b) -> Integer.compare(b.priority, a.priority));
		MAP.put(type, new CopyOnWriteArrayList<>(sorted));
	}

	public static void unsubscribe(Object owner)
	{
		MAP.values()
			.forEach(list -> list.removeIf(l -> l.owner() == owner));
	}

	@SuppressWarnings("unchecked")
	public static void post(Object event)
	{
		List<Listener> list = MAP.get(event.getClass());
		if(list == null || list.isEmpty())
			return;

		for(Listener l : list)
		{
			if(l.owner() instanceof Module m && !m.isEnabled())
				continue;

			((Handler<Object>)l.handler()).accept(event);
		}
	}
}
