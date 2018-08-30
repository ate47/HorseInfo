package fr.atesab.horsedebug;

import org.dimdev.rift.listener.client.OverlayRenderer;

public class RiftHorseDebug implements OverlayRenderer, BuildAPI {

	private HorseDebugMain mod;

	public RiftHorseDebug() {
		if (!HorseDebugMain.isAPIRegister())
			this.mod = HorseDebugMain.registerAPI(this);
	}

	@Override
	public void renderOverlay() {
		if (mod != null)
			mod.renderOverlay();
	}

	@Override
	public String getAPIName() {
		return "Rift";
	}
}
