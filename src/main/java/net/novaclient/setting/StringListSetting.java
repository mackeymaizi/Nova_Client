package net.novaclient.setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class StringListSetting extends Setting<List<String>>
{
	public StringListSetting(String name, String description, String... values)
	{
		super(name, description, new ArrayList<>(Arrays.asList(values)));
	}

	public void add(String value)
	{
		get().add(value);
	}

	public void remove(String value)
	{
		get().remove(value);
	}

	public boolean contains(String value)
	{
		return get().contains(value);
	}

	@Override
	public void fromJson(JsonElement element)
	{
		if(!element.isJsonArray())
			return;

		List<String> list = new ArrayList<>();
		for(JsonElement e : element.getAsJsonArray())
			list.add(e.getAsString());

		set(list);
	}

	@Override
	public JsonElement toJson()
	{
		JsonArray array = new JsonArray();
		get().forEach(v -> array.add(new JsonPrimitive(v)));
		return array;
	}
}
