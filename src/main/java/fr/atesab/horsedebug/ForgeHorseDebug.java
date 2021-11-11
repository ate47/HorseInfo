package fr.atesab.horsedebug;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("horsedebug")
public class ForgeHorseDebug implements BuildAPI {

	private HorseDebugMain mod;

	public ForgeHorseDebug() {
		this.mod = HorseDebugMain.registerAPI(this);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onRenderOverlay(RenderGameOverlayEvent.Post ev) {
		if (ev.getType() == ElementType.DEBUG)
			if (mod != null)
				mod.renderOverlay(ev.getMatrixStack());
	}

	@Override
	public String getAPIName() {
		return "Forge";
	}
}
