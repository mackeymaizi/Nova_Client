package net.novaclient.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class DoubleSetting extends Setting<Double>
{
	private final double min;
	private final double max;
	private final double step;
	private final String suffix;

	public DoubleSetting(String name, String description, double value,
		double min, double max, double step, String suffix)
	{
		super(name, description, value);
		this.min = min;
		this.max = max;
		this.step = step;
		this.suffix = suffix;
	}

	public DoubleSetting(String name, String description, double value,
		double min, double max, double step)
	{
		this(name, description, value, min, max, step, "");
	}

	public DoubleSetting(String name, double value, double min, double max,
		double step)
	{
		this(name, "", value, min, max, step, "");
	}

	public double min()
	{
		return min;
	}

	public double max()
	{
		return max;
	}

	public double step()
	{
		return step;
	}

	public String suffix()
	{
		return suffix;
	}

	@Override
	public void set(Double value)
	{
		super.set(Math.max(min, Math.min(max, value)));
	}

	@Override
	public void fromJson(JsonElement element)
	{
		if(element.isJsonPrimitive())
			set(element.getAsDouble());
	}

	@Override
	public JsonElement toJson()
	{
		return new JsonPrimitive(get());
	}
}
