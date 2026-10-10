package net.novaclient.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class EnumSetting<T extends Enum<T>> extends Setting<T>
{
	private final T[] values;

	public EnumSetting(String name, String description, T[] values, T value)
	{
		super(name, description, value);
		this.values = values;
	}

	public EnumSetting(String name, String description, T[] values)
	{
		this(name, description, values, values[0]);
	}

	public T[] values()
	{
		return values;
	}

	public void selectNext()
	{
		int index = get().ordinal() + 1;
		if(index >= values.length)
			index = 0;
		set(values[index]);
	}

	@Override
	public void fromJson(JsonElement element)
	{
		if(!element.isJsonPrimitive())
			return;

		String name = element.getAsString();
		for(T value : values)
			if(value.name().equals(name))
			{
				set(value);
				return;
			}
	}

	@Override
	public JsonElement toJson()
	{
		return new JsonPrimitive(get().name());
	}
}
