package net.novaclient.module;

import net.novaclient.event.EventBus;
import net.novaclient.setting.Setting;
import net.novaclient.setting.Settings;
import net.novaclient.util.ChatUtils;

public abstract class Module
{
	private final String name;
	private final String description;
	private final ModuleCategory category;

	/** Localized (Chinese) name shown in the ClickGUI and HUD. */
	private String displayName;

	public final Settings settings = new Settings();

	private boolean enabled;
	private int keybind = -1;

	protected Module(String name, String description, ModuleCategory category)
	{
		this.name = name;
		this.displayName = name;
		this.description = description;
		this.category = category;
	}

	/** Sets the localized display name. Call this from the constructor. */
	protected final void displayName(String displayName)
	{
		this.displayName = displayName;
	}

	protected final <T> void subscribe(Class<T> type,
		EventBus.Handler<T> handler)
	{
		EventBus.subscribe(type, this, handler);
	}

	public void onEnable()
	{}

	public void onDisable()
	{}

	/** Extra text shown next to the module name in the HUD and ClickGUI. */
	public String getInfoString()
	{
		return null;
	}

	public String name()
	{
		return name;
	}

	/** Localized name for display. Falls back to {@link #name()}. */
	public String displayName()
	{
		return displayName;
	}

	public String description()
	{
		return description;
	}

	public ModuleCategory category()
	{
		return category;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		if(this.enabled == enabled)
			return;

		this.enabled = enabled;

		if(enabled)
			onEnable();
		else
			onDisable();
	}

	public void toggle()
	{
		setEnabled(!enabled);
	}

	public int keybind()
	{
		return keybind;
	}

	public void setKeybind(int keybind)
	{
		this.keybind = keybind;
	}

	public boolean hasKeybind()
	{
		return keybind != -1;
	}

	protected final <T extends Setting<?>> T addSetting(T setting)
	{
		return settings.add(setting);
	}

	public void info(String message, Object... args)
	{
		ChatUtils.nova(args.length == 0 ? message : String.format(message, args));
	}

	public void warning(String message, Object... args)
	{
		ChatUtils.warning(
			args.length == 0 ? message : String.format(message, args));
	}

	public void error(String message, Object... args)
	{
		ChatUtils
			.error(args.length == 0 ? message : String.format(message, args));
	}

	/** Sends the "Module toggled on/off" chat message. */
	public void sendToggledMsg()
	{
		ChatUtils.nova((enabled ? "Enabled " : "Disabled ")
			+ net.minecraft.ChatFormatting.WHITE + name
			+ net.minecraft.ChatFormatting.RESET + ".");
	}

	@Override
	public String toString()
	{
		return name;
	}
}
