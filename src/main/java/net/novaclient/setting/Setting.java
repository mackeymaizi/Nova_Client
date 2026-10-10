package net.novaclient.setting;

import com.google.gson.JsonElement;

public abstract class Setting<T>
{
	private final String name;
	private final String description;
	private final T defaultValue;

	protected T value;

	public Setting(String name, String description, T value)
	{
		this.name = name;
		this.description = description;
		this.value = value;
		this.defaultValue = value;
	}

	public String name()
	{
		return name;
	}

	public String description()
	{
		return description;
	}

	public T get()
	{
		return value;
	}

	public void set(T value)
	{
		this.value = value;
	}

	public void reset()
	{
		value = defaultValue;
	}

	public abstract void fromJson(JsonElement element);

	public abstract JsonElement toJson();
}
