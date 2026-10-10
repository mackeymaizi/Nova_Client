package net.novaclient.module;

public enum ModuleCategory
{
	COMBAT("Combat"),
	MOVEMENT("Movement"),
	RENDER("Render"),
	PLAYER("Player"),
	WORLD("World"),
	MISC("Misc"),
	CLIENT("Client");

	private final String displayName;

	ModuleCategory(String displayName)
	{
		this.displayName = displayName;
	}

	public String displayName()
	{
		return displayName;
	}
}
