package fr.atesab.horsedebug;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

public class FabricHorseDebug implements BuildAPI, HudElement, ClientModInitializer, WorldRenderEvents.AfterEntities {

	private final HorseDebugMain mod;

	public FabricHorseDebug() {
		this.mod = HorseDebugMain.registerAPI(this);
	}

	@Override
	public String getAPIName() {
		return "Fabric";
	}

	@Override
	public void onInitializeClient() {
		HudElementRegistry.addFirst(Identifier.of("horsedebug.hud"), this);
		WorldRenderEvents.AFTER_ENTITIES.register(this);
		ClientTickEvents.END_CLIENT_TICK.register(client -> mod.onKey());
		mod.setup();
	}

	@Override
	public void afterEntities(WorldRenderContext context) {
		mod.renderWorld(context.world().getEntities(), context.matrixStack(), context.camera(), context.consumers(), context.tickCounter().getTickProgress(true));
	}

	@Override
	public void render(DrawContext context, RenderTickCounter tickCounter) {
		mod.renderOverlay(context);
	}
}
