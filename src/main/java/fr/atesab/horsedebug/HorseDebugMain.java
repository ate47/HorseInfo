package fr.atesab.horsedebug;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.phys.EntityHitResult;

public class HorseDebugMain {
	private static final Logger log = LogManager.getLogger("HorseDebug");
	private static HorseDebugMain instance;

	private static void log(String message) {
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
	 * @throws IllegalStateException if an API is already register for it
	 */
	public static HorseDebugMain registerAPI(BuildAPI api) throws IllegalStateException {
		instance = new HorseDebugMain();
		log("Starting HorseDebug with " + api.getAPIName());
		return instance;
	}

	public static String getHorseColorNameDescription(Variant color) {
		switch (color.getId()) {
		case 0:
			return "white";
		case 1:
			return "creamy";
		case 2:
			return "chestnut";
		case 3:
			return "brown";
		case 4:
			return "black";
		case 5:
			return "gray";
		case 6:
			return "darkbrown";
		default:
			return "unknown";
		}
	}

	public static String getCatColorNameDescription(int color) {
		switch (color) {
		case 0:
			return "tabby";
		case 1:
			return "black";
		case 2:
			return "red";
		case 3:
			return "siamese";
		case 4:
			return "british_shorthair";
		case 5:
			return "calico";
		case 6:
			return "persian";
		case 7:
			return "ragdoll";
		case 8:
			return "white";
		case 9:
			return "jellie";
		case 10:
			return "all_black";
		default:
			return "unknown";
		}
	}

	public static String getHorseColorName(Variant color) {
		return I18n.get("gui.act.invView.horse.variant." + getHorseColorNameDescription(color));
	}

	public static String getCatColorName(int color) {
		return I18n.get("gui.act.invView.cat.variant." + getCatColorNameDescription(color));
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

	public void drawInventory(PoseStack stack, Minecraft mc, int posX, int posY, String[] addText,
			LivingEntity entity) {
		int l = addText.length;
		if (l == 0)
			return;
		int sizeX = 0;
		int sizeY = 0;
		int itemSize = 20;
		if (mc.font.lineHeight * 2 + 2 > itemSize)
			itemSize = mc.font.lineHeight * 2 + 2;
		for (int i = 0; i < addText.length; i++) {
			sizeY += mc.font.lineHeight + 1;
			int a = mc.font.width(addText[i]) + 10;
			if (a > sizeX)
				sizeX = a;
		}
		if (entity != null) {
			sizeX += 100;
			if (sizeY < 100)
				sizeY = 100;
		}
		var mw = mc.getWindow();
		posX += 5;
		posY += 5;
		if (posX + sizeX > mw.getGuiScaledWidth())
			posX -= sizeX + 10;
		if (posY + sizeY > mw.getGuiScaledHeight())
			posY -= sizeY + 10;
		int posY1 = posY + 5;
		for (int i = 0; i < addText.length; i++) {
			mc.font.draw(stack, addText[i], posX + 5, posY1, 0xffffffff);
			posY1 += (mc.font.lineHeight + 1);
		}
		if (entity != null) {
			InventoryScreen.renderEntityInInventory(posX + sizeX - 55, posY + 105, 50, 50, 0, entity);
		}
	}

	public String[] getEntityData(LivingEntity entity) {
		List<String> text = Lists.newArrayList();
		text.add("\u00a7b" + entity.getDisplayName().getString());
		text.add("\u00a77" + EntityType.getKey(entity.getType()).toString());

		if (entity instanceof Cat cat) {
			var color = cat.getCatType();
			text.add(I18n.get("gui.act.invView.horse.variant") + ": " + getCatColorName(color) + " (" + color + ")");
		} else if (entity instanceof Sheep sheep) {
			var color = sheep.getColor();
			text.add(I18n.get("gui.act.invView.horse.variant") + ": " + color.getName() + " (" + color.getId() + ")");
		} else if (entity instanceof AbstractHorse baby) {

			var jumpStrength = baby.getAttribute(Attributes.JUMP_STRENGTH).getBaseValue();
			double yVelocity = jumpStrength;
			double jumpHeight = 0;
			while (yVelocity > 0) {
				jumpHeight += yVelocity;
				yVelocity -= 0.08;
				yVelocity *= 0.98;
			}

			if (baby instanceof Horse horse) {
				var color = horse.getVariant();
				text.add(I18n.get("gui.act.invView.horse.variant") + ": " + getHorseColorName(color) + " ("
						+ color.getId() + ")");
			}

			text.add(I18n.get("gui.act.invView.horse.jump") + ": "
					+ getFormattedText(jumpHeight, BAD_JUMP, EXELLENT_JUMP) + " " + "("
					+ significantNumbers(jumpStrength) + " iu)");
			text.add(I18n.get("gui.act.invView.horse.speed") + ": "
					+ getFormattedText(baby.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() * 43, BAD_SPEED,
							EXELLENT_SPEED)
					+ " m/s " + "(" + significantNumbers(baby.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue())
					+ " iu)");
			text.add(I18n.get("gui.act.invView.horse.health") + ": "
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

	public void renderOverlay(PoseStack stack) {
		Minecraft mc = Minecraft.getInstance();
		if (!mc.options.renderDebug)
			return;
		var mw = mc.getWindow();
		if (mc.player.getVehicle() instanceof AbstractHorse baby) {
			drawInventory(stack, mc, mw.getGuiScaledWidth(), mw.getGuiScaledHeight(), getEntityData(baby), baby);
		} else {
			var obj = mc.hitResult;
			if (obj != null && obj instanceof EntityHitResult eo) {
				if (eo.getEntity() instanceof LivingEntity le)
					drawInventory(stack, mc, mw.getGuiScaledWidth(), mw.getGuiScaledHeight(), getEntityData(le), le);
			}
		}
	}

}
