package net.novaclient.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class IntSetting extends Setting<Integer>
{
	private final int min;
	private final int max;
	private final int step;

	public IntSetting(String name, String description, int value, int min,
		int max, int step)
	{
		super(name, description, value);
		this.min = min;
		this.max = max;
		this.step = step;
	}

	public int min()
	{
		return min;
	}

	public int max()
	{
		return max;
	}

	public int step()
	{
		return step;
	}

	@Override
	public void set(Integer value)
	{
		super.set(Math.max(min, Math.min(max, value)));
	}

	@Override
	public void fromJson(JsonElement element)
	{
		if(element.isJsonPrimitive())
			set(element.getAsInt());
	}

	@Override
	public JsonElement toJson()
	{
		return new JsonPrimitive(get());
	}
}
