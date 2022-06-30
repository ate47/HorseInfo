package fr.atesab.horsedebug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("horsedebug")
public class ForgeHorseDebug implements BuildAPI {

	private final HorseDebugMain mod;

	public ForgeHorseDebug() {
		this.mod = HorseDebugMain.registerAPI(this);
		MinecraftForge.EVENT_BUS.register(this);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}

	@SubscribeEvent
	public void onRenderOverlay(RenderGameOverlayEvent.Post ev) {
		if (ev.getType() == ElementType.DEBUG && mod != null) {
			mod.renderOverlay(ev.getPoseStack());
		}
	}

	@Override
	public String getAPIName() {
		return "Forge";
	}

	@SubscribeEvent
	public void renderWordEvent(RenderLevelLastEvent ev) {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null) {
			return;
		}

		mod.renderWorld(level.entitiesForRendering(), ev.getPoseStack(), mc.gameRenderer.getMainCamera(), ev.getPartialTick(), mc.renderBuffers().bufferSource());
	}


	private void setup(final FMLCommonSetupEvent event) {
		mod.setup();
	}


	@SubscribeEvent
	public void onKeyEvent(InputEvent.KeyInputEvent ev) {
		var client = Minecraft.getInstance();
		if (client.screen != null)
			return;

		mod.onKey();
	}
}
