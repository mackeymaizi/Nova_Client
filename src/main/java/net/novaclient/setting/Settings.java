package net.novaclient.setting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class Settings implements Iterable<Setting<?>>
{
	private final List<Setting<?>> settings = new ArrayList<>();

	public <T extends Setting<?>> T add(T setting)
	{
		settings.add(setting);
		return setting;
	}

	public List<Setting<?>> all()
	{
		return settings;
	}

	public int size()
	{
		return settings.size();
	}

	public void fromJson(JsonObject object)
	{
		for(Setting<?> setting : settings)
		{
			JsonElement element = object.get(setting.name());
			if(element != null)
				setting.fromJson(element);
		}
	}

	public JsonObject toJson()
	{
		JsonObject object = new JsonObject();
		for(Setting<?> setting : settings)
			object.add(setting.name(), setting.toJson());

		return object;
	}

	@Override
	public Iterator<Setting<?>> iterator()
	{
		return settings.iterator();
	}
}
