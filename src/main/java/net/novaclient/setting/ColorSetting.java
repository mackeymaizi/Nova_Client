package net.novaclient.setting;

import java.awt.Color;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class ColorSetting extends Setting<Color>
{
	public ColorSetting(String name, String description, Color value)
	{
		super(name, description, value);
	}

	public int getRGB()
	{
		return get().getRGB();
	}

	/** Returns the color as 0xAARRGGBB with the given alpha (0-255). */
	public int getPacked(int alpha)
	{
		Color c = get();
		return (alpha & 0xFF) << 24 | (c.getRed() & 0xFF) << 16
			| (c.getGreen() & 0xFF) << 8 | (c.getBlue() & 0xFF);
	}

	public int getPacked(float alpha)
	{
		return getPacked((int)(alpha * 255));
	}

	@Override
	public void fromJson(JsonElement element)
	{
		if(element.isJsonPrimitive())
			set(new Color(element.getAsInt(), true));
	}

	@Override
	public JsonElement toJson()
	{
		return new JsonPrimitive(get().getRGB());
	}
}
