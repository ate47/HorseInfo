package fr.atesab.horsedebug;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.HorseColor;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.HorseMarking;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Matrix4f;

public class HorseDebugMain {
	public static final String UTF8_STAR = "\u2B50";
	public static final String UTF8_HEART = "\u2764";
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
	 *                               if an API is already register for it
	 */
	public static HorseDebugMain registerAPI(BuildAPI api) throws IllegalStateException {
		instance = new HorseDebugMain();
		log("Starting HorseDebug with " + api.getAPIName());
		return instance;
	}

	public static String getHorseColorNameDescription(HorseColor color) {
		switch (color.getIndex()) {
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

	public static String getHorseColorNameDescription(HorseMarking color) {
		switch (color.getIndex()) {
			case 0:
				return "none";
			case 1:
				return "white";
			case 2:
				return "white_field";
			case 3:
				return "white_dots";
			case 4:
				return "black_dots";
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

	public static String getHorseColorName(HorseColor color, HorseMarking marking) {
		return I18n.translate("gui.act.invView.horse.variant." + getHorseColorNameDescription(color)) + " / "
				+ I18n.translate("gui.act.invView.horse.variant.marking." + getHorseColorNameDescription(marking));
	}

	public static String getCatColorName(int color) {
		return I18n.translate("gui.act.invView.cat.variant." + getCatColorNameDescription(color));
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
			InventoryScreen.drawEntity(posX + sizeX - 55, posY + 105, 50, 50, 0, entity); // drawEntityOnScreen
		}
	}

	public double getJumpHeight(HorseBaseEntity horse) {
		double yVelocity = horse.getJumpStrength();
		double jumpHeight = 0;
		while (yVelocity > 0) {
			jumpHeight += yVelocity;
			yVelocity -= 0.08;
			yVelocity *= 0.98;
		}

		return jumpHeight;
	}

	public String[] getEntityData(LivingEntity entity) {
		List<String> text = Lists.newArrayList();
		text.add("\u00a7b" + entity.getDisplayName().asString());

		if (entity instanceof CatEntity) {
			CatEntity cat = (CatEntity) entity;
			int color = cat.getCatType();
			text.add(I18n.translate("gui.act.invView.horse.variant") + ": " + getCatColorName(color) + " (" + color
					+ ")");
		} else if (entity instanceof SheepEntity) {
			SheepEntity sheep = (SheepEntity) entity;
			DyeColor color = sheep.getColor();
			text.add(I18n.translate("gui.act.invView.horse.variant") + ": " + color.getName() + " (" + color.getId()
					+ ")");
		} else if (entity instanceof HorseBaseEntity) {
			HorseBaseEntity baby = (HorseBaseEntity) entity;

			if (baby instanceof HorseEntity) {
				HorseEntity horse = (HorseEntity) baby;
				HorseColor color = horse.getColor();
				HorseMarking marking = horse.getMarking();
				int id = color.getIndex() + marking.getIndex() << 8;
				text.add(I18n.translate("gui.act.invView.horse.variant") + ": " + getHorseColorName(color, marking)
						+ " (" + id + ")");
			}

			text.add(I18n.translate("gui.act.invView.horse.jump") + ": "
					+ getFormattedText(getJumpHeight(
							baby), BAD_JUMP, EXELLENT_JUMP)
					+ " " + "("
					+ significantNumbers(baby.getJumpStrength()) + " iu)");
			text.add(I18n.translate("gui.act.invView.horse.speed") + ": "
					+ getFormattedText(
							baby.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).getBaseValue() * 43,
							BAD_SPEED, EXELLENT_SPEED)
					+ " m/s " + "("
					+ significantNumbers(
							baby.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).getBaseValue())
					+ " iu)");
			text.add(I18n.translate("gui.act.invView.horse.health") + ": "
					+ getFormattedText((baby.getMaxHealth() / 2D), BAD_HP, EXELLENT_HP) + " HP");
		}
		return text.stream().toArray(String[]::new);
	}

	private static String getFormattedText(double value, double bad, double exellent) {
		return getFormattedText(value, bad, exellent, "", false);
	}

	private static String getFormattedText(double value, double bad, double exellent, String unit, boolean stared) {
		return (value >= exellent ? Formatting.GOLD : (value < bad) ? Formatting.RED : Formatting.GREEN).toString()
				+ significantNumbers(value) + unit + (stared ? (Formatting.YELLOW + " " + UTF8_STAR) : "");
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

	/**
	 * sum the 3 normalized values of jump/health/speed
	 * 
	 * @param jump   the jump power
	 * @param health the health
	 * @param speed  the speed
	 * @return a score
	 */
	public double score(double jump, double health, double speed) {
		return (jump - (EXELLENT_JUMP + BAD_JUMP) / 2) / (EXELLENT_JUMP - BAD_JUMP)
				+ (health / 2D - (EXELLENT_HP + BAD_HP) / 2) / (EXELLENT_HP - BAD_HP)
				+ (speed - (EXELLENT_SPEED + BAD_SPEED) / 2) / (EXELLENT_SPEED - BAD_SPEED);
	}

	public void renderWorld(Iterable<Entity> entities, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
			Camera camera) {
		List<HorseBaseEntity> horses = new ArrayList<>();
		double bestScore = 0;
		double bestJump = 0;
		double bestSpeed = 0;
		double bestHealth = 0;

		MinecraftClient mc = MinecraftClient.getInstance();

		for (Entity entity : entities)
			if (entity instanceof HorseBaseEntity) {
				HorseBaseEntity h = (HorseBaseEntity) entity;

				double distance = mc.player.getPos().squaredDistanceTo(h.getPos());

				if (distance > 4096.0D)
					continue;

				horses.add(h);

				double jump = getJumpHeight(h);
				double health = h.getMaxHealth();
				double speed = h.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).getBaseValue() * 43;
				double score = score(jump, health, speed);

				if (jump > bestJump)
					bestJump = jump;
				if (health > bestHealth)
					bestHealth = health;
				if (speed > bestSpeed)
					bestSpeed = speed;
				if (score > bestScore)
					bestScore = score;
			}
		TextRenderer textRenderer = mc.textRenderer;
		float opacity = mc.options.getTextBackgroundOpacity(0.25F);

		Text[] texts = new Text[4];

		for (HorseBaseEntity h : horses) {
			double jump = getJumpHeight(h);
			double health = h.getMaxHealth();
			double speed = h.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).getBaseValue() * 43;
			double score = score(jump, health, speed);

			texts[0] = new LiteralText(getFormattedText(
					jump, BAD_JUMP, EXELLENT_JUMP, " b", jump >= bestJump));
			texts[1] = new LiteralText(getFormattedText(
					health, BAD_HP, EXELLENT_HP, " " + Formatting.RED + UTF8_HEART, health >= bestHealth));
			texts[2] = new LiteralText(getFormattedText(
					speed, BAD_SPEED, EXELLENT_SPEED, " m/s", speed >= bestSpeed));

			if (score >= bestScore) {
				texts[3] = new LiteralText(Formatting.YELLOW + " " + UTF8_STAR);
			} else {
				texts[3] = null;
			}

			float location = h.getHeight() + 0.5F;
			matrices.push();
			matrices.translate(h.getX() - camera.getPos().x, h.getY() - camera.getPos().y,
					h.getZ() - camera.getPos().z);
			matrices.translate(0.0D, (double) location, 0.0D);
			matrices.multiply(camera.getRotation());
			matrices.scale(-0.025F, -0.025F, 0.025F);
			Matrix4f matrix4f = matrices.peek().getModel();
			int background = (int) (opacity * 255.0F) << 24;
			int y = (h.hasCustomName() ? -(textRenderer.fontHeight + 4) : 0);
			for (Text text : texts) {
				if (text == null)
					continue;

				float x = (float) (-textRenderer.getWidth((StringVisitable) text) / 2);
				textRenderer.draw(text,
						x, (float) y, 0x22FFFFFF, false, matrix4f, vertexConsumers, true, background, 15728880);
				textRenderer.draw(text,
						x, (float) y, 0xffFFFFFF, false, matrix4f, vertexConsumers, false, 0, 15728880);

				y -= textRenderer.fontHeight + 2;
			}

			matrices.pop();
		}
	}
}
