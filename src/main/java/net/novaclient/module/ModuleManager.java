package net.novaclient.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import net.novaclient.NovaClient;
import net.novaclient.event.events.KeyPressEvent;
import net.novaclient.event.EventBus;
import net.novaclient.module.modules.client.ClickGuiModule;
import net.novaclient.module.modules.client.HudModule;
import net.novaclient.module.modules.combat.AimAssistModule;
import net.novaclient.module.modules.combat.AnchorAuraModule;
import net.novaclient.module.modules.combat.AutoTotemModule;
import net.novaclient.module.modules.combat.CriticalsModule;
import net.novaclient.module.modules.combat.CrystalAuraModule;
import net.novaclient.module.modules.combat.KillAuraModule;
import net.novaclient.module.modules.combat.MaceKillModule;
import net.novaclient.module.modules.combat.TriggerBotModule;
import net.novaclient.module.modules.misc.AntiAfkModule;
import net.novaclient.module.modules.misc.AntiWaterPushModule;
import net.novaclient.module.modules.misc.AutoReconnectModule;
import net.novaclient.module.modules.misc.AutoToolModule;
import net.novaclient.module.modules.misc.BetterChatModule;
import net.novaclient.module.modules.misc.BoatFlyModule;
import net.novaclient.module.modules.misc.InfiniChatModule;
import net.novaclient.module.modules.misc.InvWalkModule;
import net.novaclient.module.modules.misc.NoChatReportsModule;
import net.novaclient.module.modules.misc.NoTelemetryModule;
import net.novaclient.module.modules.misc.PacketLoggerModule;
import net.novaclient.module.modules.misc.PortalGuiModule;
import net.novaclient.module.modules.misc.RestockModule;
import net.novaclient.module.modules.movement.AntiCactusModule;
import net.novaclient.module.modules.movement.AutoSprintModule;
import net.novaclient.module.modules.movement.AutoWalkModule;
import net.novaclient.module.modules.movement.BlinkModule;
import net.novaclient.module.modules.movement.BoostModule;
import net.novaclient.module.modules.movement.FishModule;
import net.novaclient.module.modules.movement.FlightModule;
import net.novaclient.module.modules.movement.HighJumpModule;
import net.novaclient.module.modules.movement.JesusModule;
import net.novaclient.module.modules.movement.JetpackModule;
import net.novaclient.module.modules.movement.LongJumpModule;
import net.novaclient.module.modules.movement.NoFallModule;
import net.novaclient.module.modules.movement.NoSlowdownModule;
import net.novaclient.module.modules.movement.NoWebModule;
import net.novaclient.module.modules.movement.PacketFlyModule;
import net.novaclient.module.modules.movement.ScaffoldModule;
import net.novaclient.module.modules.movement.SnowShoeModule;
import net.novaclient.module.modules.movement.SpeedHackModule;
import net.novaclient.module.modules.movement.SpiderModule;
import net.novaclient.module.modules.movement.StepModule;
import net.novaclient.module.modules.player.AntiKnockbackModule;
import net.novaclient.module.modules.player.AutoArmorModule;
import net.novaclient.module.modules.player.AutoEatModule;
import net.novaclient.module.modules.player.AutoPotionModule;
import net.novaclient.module.modules.player.ReachModule;
import net.novaclient.module.modules.player.VelocityModule;
import net.novaclient.module.modules.render.AntiBlindModule;
import net.novaclient.module.modules.render.AntiWobbleModule;
import net.novaclient.module.modules.render.BarrierEspModule;
import net.novaclient.module.modules.render.BreadcrumbsModule;
import net.novaclient.module.modules.render.CameraNoClipModule;
import net.novaclient.module.modules.render.ChamsModule;
import net.novaclient.module.modules.render.FullbrightModule;
import net.novaclient.module.modules.render.HealthTagsModule;
import net.novaclient.module.modules.render.HoleEspModule;
import net.novaclient.module.modules.render.ItemEspModule;
import net.novaclient.module.modules.render.NameProtectModule;
import net.novaclient.module.modules.render.NameTagsModule;
import net.novaclient.module.modules.render.NoFogModule;
import net.novaclient.module.modules.render.NoHurtcamModule;
import net.novaclient.module.modules.render.NoOverlayModule;
import net.novaclient.module.modules.render.NoPumpkinModule;
import net.novaclient.module.modules.render.NoShieldOverlayModule;
import net.novaclient.module.modules.render.NoVignetteModule;
import net.novaclient.module.modules.render.NoWeatherModule;
import net.novaclient.module.modules.render.PlayerEspModule;
import net.novaclient.module.modules.render.SearchModule;
import net.novaclient.module.modules.render.TrajectoriesModule;
import net.novaclient.module.modules.render.TrueSightModule;
import net.novaclient.module.modules.render.WaypointsModule;
import net.novaclient.module.modules.render.XRayModule;
import net.novaclient.module.modules.world.FastBreakModule;
import net.novaclient.module.modules.world.KaboomModule;
import net.novaclient.module.modules.world.NukerModule;
import net.novaclient.module.modules.world.VeinMinerModule;

public final class ModuleManager
{
	private static final Map<Class<? extends Module>, Module> BY_CLASS =
		new LinkedHashMap<>();
	private static final Map<String, Module> BY_NAME = new LinkedHashMap<>();
	private static final List<Module> MODULES = new ArrayList<>();

	private ModuleManager()
	{}

	public static void init()
	{
		// client
		register(new ClickGuiModule());
		register(new HudModule());

		// combat
		register(new KillAuraModule());
		register(new CrystalAuraModule());
		register(new AnchorAuraModule());
		register(new CriticalsModule());
		register(new MaceKillModule());
		register(new AutoTotemModule());
		register(new AimAssistModule());
		register(new TriggerBotModule());

		// movement
		register(new AutoSprintModule());
		register(new SpeedHackModule());
		register(new JesusModule());
		register(new JetpackModule());
		register(new FishModule());
		register(new NoWebModule());
		register(new NoSlowdownModule());
		register(new SnowShoeModule());
		register(new AntiCactusModule());
		register(new InvWalkModule());
		register(new BoatFlyModule());
		register(new BoostModule());
		register(new FlightModule());
		register(new HighJumpModule());
		register(new LongJumpModule());
		register(new SpiderModule());
		register(new StepModule());
		register(new ScaffoldModule());
		register(new AutoWalkModule());
		register(new NoFallModule());
		register(new BlinkModule());
		register(new PacketFlyModule());

		// render
		register(new FullbrightModule());
		register(new AntiBlindModule());
		register(new AntiWobbleModule());
		register(new NoFogModule());
		register(new NoOverlayModule());
		register(new NoVignetteModule());
		register(new NoPumpkinModule());
		register(new NoShieldOverlayModule());
		register(new NoHurtcamModule());
		register(new NoWeatherModule());
		register(new CameraNoClipModule());
		register(new TrueSightModule());
		register(new PlayerEspModule());
		register(new ItemEspModule());
		register(new BarrierEspModule());
		register(new ChamsModule());
		register(new HoleEspModule());
		register(new NameTagsModule());
		register(new HealthTagsModule());
		register(new NameProtectModule());
		register(new TrajectoriesModule());
		register(new BreadcrumbsModule());
		register(new WaypointsModule());
		register(new XRayModule());
		register(new SearchModule());

		// player
		register(new AntiKnockbackModule());
		register(new VelocityModule());
		register(new ReachModule());
		register(new AutoArmorModule());
		register(new AutoEatModule());
		register(new AutoPotionModule());

		// world
		register(new FastBreakModule());
		register(new VeinMinerModule());
		register(new NukerModule());
		register(new KaboomModule());

		// misc
		register(new AutoToolModule());
		register(new RestockModule());
		register(new AntiWaterPushModule());
		register(new PortalGuiModule());
		register(new NoTelemetryModule());
		register(new NoChatReportsModule());
		register(new InfiniChatModule());
		register(new BetterChatModule());
		register(new PacketLoggerModule());
		register(new AutoReconnectModule());
		register(new AntiAfkModule());

		EventBus.subscribe(KeyPressEvent.class, ModuleManager.class,
			ModuleManager::onKeyPress);
	}

	private static void register(Module module)
	{
		MODULES.add(module);
		BY_CLASS.put(module.getClass(), module);
		BY_NAME.put(module.name().toLowerCase(Locale.ROOT), module);
	}

	private static void onKeyPress(KeyPressEvent event)
	{
		if(event.action() != GLFW.GLFW_PRESS)
			return;

		// Ignore keybinds while a GUI is open, except Nova's own ClickGUI
		// (so its close key keeps working). Without this, pressing the
		// ClickGUI key inside chat / inventory would spawn another screen.
		net.minecraft.client.gui.screens.Screen screen = NovaClient.MC.gui.screen();
		if(screen != null
			&& !(screen instanceof net.novaclient.gui.clickgui.ClickGuiScreen))
			return;

		// Never toggle a module with the key currently being bound in the
		// ClickGUI - the user is choosing a bind, not firing it.
		if(net.novaclient.gui.clickgui.ClickGuiScreen.isRebinding())
			return;

		for(Module module : MODULES)
			if(module.hasKeybind() && module.keybind() == event.key())
				module.toggle();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Module> T get(Class<T> klass)
	{
		return (T)BY_CLASS.get(klass);
	}

	public static Module get(String name)
	{
		return BY_NAME.get(name.toLowerCase(Locale.ROOT));
	}

	public static Collection<Module> getAll()
	{
		return MODULES;
	}

	public static List<Module> getByCategory(ModuleCategory category)
	{
		List<Module> list = new ArrayList<>();
		for(Module module : MODULES)
			if(module.category() == category)
				list.add(module);

		return list;
	}
}
