package fr.atesab.horsedebug;

import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class HorseDebugMain {
	private static final Logger log = LogManager.getLogger("HorseDebug");
	private static HorseDebugMain instance;

	public static void log(String message) {
		log.info("[" + log.getName() + "] " + message);
	}

	/**
	 * get this mod
	 */
	public static HorseDebugMain getMod() {
		return instance;
	}

	/**
	 * true if an API is already register for it
	 */
	public static boolean isAPIRegister() {
		return instance != null;
	}

	/**
	 * register an API for HorseDebug
	 * 
	 * @throws IllegalStateException
	 *             if an API is already register for it
	 */
	public static HorseDebugMain registerAPI(BuildAPI api) throws IllegalStateException {
		instance = new HorseDebugMain();
		log("Starting HorseDebug with " + api.getAPIName());
		return instance;
	}

	private HorseDebugMain() {
		if (isAPIRegister())
			throw new IllegalStateException("An API is already register for this mod!");
	}

	public static final double BAD_HP = 10; // min: 7.5
	public static final double BAD_JUMP = 2.75; // min: 1.2
	public static final double BAD_SPEED = 9; // min: ~7?
	public static final double EXELLENT_HP = 14; // max: 15
	public static final double EXELLENT_JUMP = 5; // max: 5.5?
	public static final double EXELLENT_SPEED = 13;// max: 14.1?

	public void drawInventory(MatrixStack stack, MinecraftClient mc, int posX, int posY, String[] addText,
			LivingEntity entity) {
		int l = addText.length;
		if (l == 0)
			return;
		int sizeX = 0;
		int sizeY = 0;
		int itemSize = 20;
		if (mc.textRenderer.fontHeight * 2 + 2 > itemSize)
			itemSize = mc.textRenderer.fontHeight * 2 + 2;
		for (int i = 0; i < addText.length; i++) {
			sizeY += mc.textRenderer.fontHeight + 1;
			int a = mc.textRenderer.getWidth(addText[i]) + 10;
			if (a > sizeX)
				sizeX = a;
		}
		if (entity != null) {
			sizeX += 100;
			if (sizeY < 100)
				sizeY = 100;
		}
		Window mw = mc.getWindow();
		posX += 5;
		posY += 5;
		if (posX + sizeX > mw.getScaledWidth())
			posX -= sizeX + 10;
		if (posY + sizeY > mw.getScaledHeight())
			posY -= sizeY + 10;
		int posY1 = posY + 5;
		for (int i = 0; i < addText.length; i++) {
			mc.textRenderer.drawWithShadow(stack, addText[i], posX + 5, posY1, 0xffffffff);
			posY1 += (mc.textRenderer.fontHeight + 1);
		}
		if (entity != null) {
			RenderSystem.color3f(1.0F, 1.0F, 1.0F);
			InventoryScreen.drawEntity(posX + sizeX - 55, posY + 105, 50, 50, 0, entity); // drawEntityOnScreen
		}
	}

	public String[] getEntityData(LivingEntity entity) {
		List<String> text = Lists.newArrayList();
		text.add("\u00a7b" + entity.getDisplayName().asString());
		if (entity instanceof HorseBaseEntity) {
			HorseBaseEntity baby = (HorseBaseEntity) entity;

			double yVelocity = baby.getJumpStrength();
			double jumpHeight = 0;
			while (yVelocity > 0) {
				jumpHeight += yVelocity;
				yVelocity -= 0.08;
				yVelocity *= 0.98;
			}
			text.add(I18n.translate("gui.act.invView.horse.jump") + " : "
					+ getFormattedText(jumpHeight, BAD_JUMP, EXELLENT_JUMP) + " " + "("
					+ significantNumbers(baby.getJumpStrength()) + " iu)");
			text.add(I18n.translate("gui.act.invView.horse.speed") + " : "
					+ getFormattedText(
							baby.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).getBaseValue() * 43,
							BAD_SPEED, EXELLENT_SPEED)
					+ " m/s " + "("
					+ significantNumbers(
							baby.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).getBaseValue())
					+ " iu)");
			text.add(I18n.translate("gui.act.invView.horse.health") + " : "
					+ getFormattedText((baby.getMaxHealth() / 2D), BAD_HP, EXELLENT_HP) + " HP");
		}
		return text.stream().toArray(String[]::new);
	}

	private static String getFormattedText(double value, double bad, double exellent) {
		return new String(new char[] { '\u00a7', ((value > exellent) ? '6' : (value < bad) ? 'c' : 'a') })
				+ significantNumbers(value);
	}

	private static String significantNumbers(double d) {
		boolean a;
		if (a = d < 0) {
			d *= -1;
		}
		int d1 = (int) (d);
		d %= 1;
		String s = String.format("%.3G", d);
		if (s.length() > 0)
			s = s.substring(1);
		if (s.contains("E+"))
			s = String.format(Locale.US, "%.0f", Double.valueOf(String.format("%.3G", d)));
		return (a ? "-" : "") + d1 + s;
	}

	public void renderOverlay(MatrixStack stack) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (!mc.options.debugEnabled)
			return;
		Window mw = mc.getWindow();
		if (mc.player.getVehicle() instanceof HorseBaseEntity) {
			HorseBaseEntity baby = (HorseBaseEntity) mc.player.getVehicle();
			drawInventory(stack, mc, mw.getScaledWidth(), mw.getScaledHeight(), getEntityData(baby), baby);
		} else {
			HitResult obj = mc.crosshairTarget;
			if (obj != null && obj instanceof EntityHitResult) {
				EntityHitResult eo = (EntityHitResult) obj;
				if (eo.getEntity() instanceof LivingEntity)
					drawInventory(stack, mc, mw.getScaledWidth(), mw.getScaledHeight(),
							getEntityData((LivingEntity) eo.getEntity()), (LivingEntity) eo.getEntity());
			}
		}
	}

}
