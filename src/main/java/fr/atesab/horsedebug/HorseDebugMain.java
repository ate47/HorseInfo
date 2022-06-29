package fr.atesab.horsedebug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.phys.EntityHitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Locale;

public class HorseDebugMain {
	private static final Logger log = LogManager.getLogger("HorseDebug");
	private static HorseDebugMain instance;

	private static void log(String message) {
		log.info("[" + log.getName() + "] " + message);
	}

	private static double getBaseValue(LivingEntity entity, Attribute attribute) {
		AttributeInstance inst = entity.getAttribute(attribute);
		if (inst == null) {
			return attribute.getDefaultValue();
		}
		return inst.getBaseValue();
	}
	/**
	 * get this mod
	 * @deprecated will be removed in future version
	 */
	@Deprecated
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

	public static String getHorseColorNameDescription(Markings color) {
		return switch (color.getId()) {
			case 0 -> "none";
			case 1 -> "white";
			case 2 -> "white_field";
			case 3 -> "white_dots";
			case 4 -> "black_dots";
			default -> "unknown";
		};
	}

	public static String getHorseColorNameDescription(Variant color) {
		return switch (color.getId()) {
			case 0 -> "white";
			case 1 -> "creamy";
			case 2 -> "chestnut";
			case 3 -> "brown";
			case 4 -> "black";
			case 5 -> "gray";
			case 6 -> "darkbrown";
			default -> "unknown";
		};
	}

	public static String getHorseColorName(Variant color, Markings markings) {
		return I18n.get("gui.act.invView.horse.variant." + getHorseColorNameDescription(color)) + " / "
				+ I18n.get("gui.act.invView.horse.variant.marking." + getHorseColorNameDescription(markings));
	}

	public static String getCatColorName(CatVariant color) {
		ResourceLocation key = Registry.CAT_VARIANT.getKey(color);
		if (key == null) {
			return I18n.get("gui.act.invView.cat.variant.unknown");
		} else {
			return I18n.get("gui.act.invView.cat.variant." + key.getPath());
		}
	}

	private HorseDebugMain() {
		if (isAPIRegister()) {
			throw new IllegalStateException("An API is already register for this mod!");
		}
	}

	public static final double BAD_HP = 10; // min: 7.5
	public static final double BAD_JUMP = 2.75; // min: 1.2
	public static final double BAD_SPEED = 9; // min: ~7?
	public static final double EXCELLENT_HP = 14;
	public static final double EXCELLENT_JUMP = 5;
	public static final double EXCELLENT_SPEED = 13;

	/**
	 * @deprecated use {@link #EXCELLENT_HP} instead
	 */
	@Deprecated
	public static final double EXELLENT_HP = EXCELLENT_HP; // max: 15
	/**
	 * @deprecated use {@link #EXCELLENT_JUMP} instead
	 */
	@Deprecated
	public static final double EXELLENT_JUMP = EXCELLENT_JUMP; // max: 5.5?
	/**
	 * @deprecated use {@link #EXCELLENT_SPEED} instead
	 */
	@Deprecated
	public static final double EXELLENT_SPEED = EXCELLENT_SPEED;// max: 14.1?

	public void drawInventory(PoseStack stack, Minecraft mc, int posX, int posY, String[] addText,
			LivingEntity entity) {
		if (addText.length == 0) {
			return;
		}
		int sizeX = 0;
		int sizeY = 0;

		for (String text : addText) {
			sizeY += mc.font.lineHeight + 1;
			int textSize = mc.font.width(text) + 10;
			if (textSize > sizeX)
				sizeX = textSize;
		}
		if (entity != null) {
			sizeX += 100;
			if (sizeY < 100) {
				sizeY = 100;
			}
		}
		Window mw = mc.getWindow();
		posX += 5;
		posY += 5;
		if (posX + sizeX > mw.getGuiScaledWidth()) {
			posX -= sizeX + 10;
		}
		if (posY + sizeY > mw.getGuiScaledHeight()) {
			posY -= sizeY + 10;
		}
		int posY1 = posY + 5;
		for (String s : addText) {
			mc.font.draw(stack, s, posX + 5, posY1, 0xffffffff);
			posY1 += (mc.font.lineHeight + 1);
		}
		if (entity != null) {
			InventoryScreen.renderEntityInInventory(posX + sizeX - 55, posY + 105, 50, 50, 0, entity);
		}
	}

	public String[] getEntityData(LivingEntity entity) {
		List<String> text = Lists.newArrayList();
		text.add("\u00a7b" + entity.getDisplayName().getString());
		text.add("\u00a77" + EntityType.getKey(entity.getType()));

		if (entity instanceof Cat cat) {
			var color = cat.getCatVariant(); // getCatType
			text.add(I18n.get("gui.act.invView.horse.variant") + ": " + getCatColorName(color) + " (" + Registry.CAT_VARIANT.getKey(color) + ")");
		} else if (entity instanceof Sheep sheep) {
			var color = sheep.getColor();
			text.add(I18n.get("gui.act.invView.horse.variant") + ": " + color.getName() + " (" + color.getId() + ")");
		} else if (entity instanceof AbstractHorse baby) {
			var jumpStrength = getBaseValue(baby, Attributes.JUMP_STRENGTH);
			double yVelocity = jumpStrength;
			double jumpHeight = 0;
			while (yVelocity > 0) {
				jumpHeight += yVelocity;
				yVelocity -= 0.08;
				yVelocity *= 0.98;
			}

			if (baby instanceof Horse horse) {
				var color = horse.getVariant();
				var markings = horse.getMarkings();
				var id = color.getId() + markings.getId() << 8;
				text.add(I18n.get("gui.act.invView.horse.variant") + ": " + getHorseColorName(color, markings) + " ("
						+ id + ")");
			}

			text.add(I18n.get("gui.act.invView.horse.jump") + ": "
					+ getFormattedText(jumpHeight, BAD_JUMP, EXCELLENT_JUMP) + " " + "("
					+ significantNumbers(jumpStrength) + " iu)");
			text.add(I18n.get("gui.act.invView.horse.speed") + ": "
					+ getFormattedText(getBaseValue(baby, Attributes.MOVEMENT_SPEED) * 43, BAD_SPEED,
					EXCELLENT_SPEED)
					+ " m/s " + "(" + significantNumbers(getBaseValue(baby, Attributes.MOVEMENT_SPEED))
					+ " iu)");
			text.add(I18n.get("gui.act.invView.horse.health") + ": "
					+ getFormattedText((baby.getMaxHealth() / 2D), BAD_HP, EXCELLENT_HP) + " HP");
		}
		return text.toArray(String[]::new);
	}

	private static String getFormattedText(double value, double bad, double excellent) {
		return new String(new char[] { '\u00a7', ((value > excellent) ? '6' : (value < bad) ? 'c' : 'a') })
				+ significantNumbers(value);
	}

	private static String significantNumbers(double d) {
		boolean negative = d < 0;
		if (negative) {
			d *= -1;
		}
		int d1 = (int) (d);
		d %= 1;
		String s = String.format("%.3G", d);
		if (s.length() > 0)
			s = s.substring(1);
		if (s.contains("E+"))
			s = String.format(Locale.US, "%.0f", Double.valueOf(String.format("%.3G", d)));
		return (negative ? "-" : "") + d1 + s;
	}

	public void renderOverlay(PoseStack stack) {
		Minecraft mc = Minecraft.getInstance();
		if (!mc.options.renderDebug)
			return;
		var mw = mc.getWindow();
		if (mc.player.getVehicle() instanceof AbstractHorse baby) {
			drawInventory(stack, mc, mw.getGuiScaledWidth(), mw.getGuiScaledHeight(), getEntityData(baby), baby);
		} else {
			if (mc.hitResult instanceof EntityHitResult eo) {
				if (eo.getEntity() instanceof LivingEntity le)
					drawInventory(stack, mc, mw.getGuiScaledWidth(), mw.getGuiScaledHeight(), getEntityData(le), le);
			}
		}
	}

}
