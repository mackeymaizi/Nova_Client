package net.novaclient.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class StringSetting extends Setting<String>
{
	public StringSetting(String name, String description, String value)
	{
		super(name, description, value);
	}

	@Override
	public void fromJson(JsonElement element)
	{
		if(element.isJsonPrimitive())
			set(element.getAsString());
	}

	@Override
	public JsonElement toJson()
	{
		return new JsonPrimitive(get());
	}
}
