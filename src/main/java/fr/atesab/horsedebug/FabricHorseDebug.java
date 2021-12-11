package fr.atesab.horsedebug;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.util.math.MatrixStack;

public class FabricHorseDebug implements BuildAPI, HudRenderCallback, ClientModInitializer,
		WorldRenderEvents.AfterEntities {

	private HorseDebugMain mod;

	public FabricHorseDebug() {
		this.mod = HorseDebugMain.registerAPI(this);
	}

	@Override
	public String getAPIName() {
		return "Fabric";
	}

	@Override
	public void onInitializeClient() {
		HudRenderCallback.EVENT.register(this);
	}

	@Override
	public void onHudRender(MatrixStack matrixStack, float tickDelta) {
		mod.renderOverlay(matrixStack);
	}

	@Override
	public void afterEntities(WorldRenderContext context) {
		mod.renderWorld(context.world().getEntities());
	}
}
