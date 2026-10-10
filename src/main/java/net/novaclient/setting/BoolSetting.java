package net.novaclient.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class BoolSetting extends Setting<Boolean>
{
	public BoolSetting(String name, String description, boolean value)
	{
		super(name, description, value);
	}

	public BoolSetting(String name, boolean value)
	{
		this(name, "", value);
	}

	public boolean isChecked()
	{
		return get();
	}

	public void setChecked(boolean checked)
	{
		set(checked);
	}

	@Override
	public void fromJson(JsonElement element)
	{
		if(element.isJsonPrimitive())
			set(element.getAsBoolean());
	}

	@Override
	public JsonElement toJson()
	{
		return new JsonPrimitive(get());
	}
}
