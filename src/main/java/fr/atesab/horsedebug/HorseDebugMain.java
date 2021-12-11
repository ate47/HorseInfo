package fr.atesab.horsedebug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HorseDebugMain {
	public static final String UTF8_STAR = "\u2B50";
	public static final String UTF8_HEART = "\u2764";
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

	public static final StatValue STAT_HEALTH = StatValue.builder().base(15.0).add(8).add(9).showScale(0.5)
			.build();
	public static final StatValue STAT_JUMP = StatValue.builder().base(0.4000000059604645D).add(0.2).add(0.2).add(
			0.2).showFunc(d -> -0.2 * Math.pow(d, 3) + 3.7 * Math.pow(d, 2) + 2.1 * d - 0.4)
			.build();
	public static final StatValue STAT_SPEED = StatValue.builder().base(0.44999998807907104D).add(0.3).add(0.3).add(0.3)
			.scale(0.25).showScale(43).build();

	/**
	 * @deprecated use {@link #STAT_HEALTH} instead
	 */
	@Deprecated
	public static final double BAD_HP = STAT_HEALTH.getBadValue();
	/**
	 * @deprecated use {@link #STAT_JUMP} instead
	 */
	@Deprecated
	public static final double BAD_JUMP = STAT_JUMP.getBadValue();
	/**
	 * @deprecated use {@link #STAT_HEALTH} instead
	 */
	@Deprecated
	public static final double BAD_SPEED = STAT_SPEED.getBadValue();
	/**
	 * @deprecated use {@link #STAT_HEALTH} instead
	 */
	@Deprecated
	public static final double EXELLENT_HP = STAT_HEALTH.getExcellentValue();
	/**
	 * @deprecated use {@link #STAT_JUMP} instead
	 */
	@Deprecated
	public static final double EXELLENT_JUMP = STAT_JUMP.getExcellentValue();
	/**
	 * @deprecated use {@link #STAT_HEALTH} instead
	 */
	@Deprecated
	public static final double EXELLENT_SPEED = STAT_HEALTH.getExcellentValue();

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

	private static double getJump(LivingEntity e) {
		return getBaseValue(e, Attributes.JUMP_STRENGTH);
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
			if (baby instanceof Horse horse) {
				var color = horse.getVariant();
				var markings = horse.getMarkings();
				var id = color.getId() + markings.getId() << 8;
				text.add(I18n.get("gui.act.invView.horse.variant") + ": " + getHorseColorName(color, markings) + " ("
						+ id + ")");
			}

			text.add(I18n.get("gui.act.invView.horse.jump") + ": "
					+ STAT_JUMP.getFormattedText(getJump(baby)));
			text.add(I18n.get("gui.act.invView.horse.speed") + ": "
					+ STAT_SPEED.getFormattedText(getBaseValue(baby, Attributes.MOVEMENT_SPEED))
					+ " m/s " + "(" + significantNumbers(getBaseValue(baby, Attributes.MOVEMENT_SPEED))
					+ " iu)");
			text.add(I18n.get("gui.act.invView.horse.health") + ": "
					+ STAT_HEALTH.getFormattedText((baby.getMaxHealth())) + " HP");
		}
		return text.toArray(String[]::new);
	}

	public static String significantNumbers(double d) {
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

	/**
	 * sum the 3 normalized values of jump/health/speed
	 *
	 * @param jump   the jump power
	 * @param health the health
	 * @param speed  the speed
	 * @return a score
	 */
	public double score(double jump, double health, double speed) {
		return STAT_JUMP.normalized(jump) + STAT_HEALTH.normalized(health) + STAT_SPEED.normalized(speed);
	}


	protected Quaternion getRotation(float yaw, float pitch) {
		Quaternion quaternion = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
		quaternion.mul(Vector3f.YP.rotationDegrees(-yaw));
		quaternion.mul(Vector3f.XP.rotationDegrees(pitch));
		return quaternion;
	}

	public void renderWorld(Iterable<Entity> entities, PoseStack matrices,
							Camera camera, float delta, MultiBufferSource source) {
		List<AbstractHorse> horses = new ArrayList<>();
		double bestScore = 0;
		double bestJump = 0;
		double bestSpeed = 0;
		double bestHealth = 0;

		var mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null) {
			return;
		}

		for (Entity entity : entities)
			if (entity instanceof AbstractHorse h) {

				double distance = player.getPosition(delta).distanceToSqr(h.getPosition(delta));

				if (distance > 4096.0D) {
					continue;
				}

				horses.add(h);

				double jump = getJump(h);
				double health = h.getMaxHealth();
				double speed = getBaseValue(h, Attributes.MOVEMENT_SPEED);
				double score = score(jump, health, speed);


				if (jump > bestJump) {
					bestJump = jump;
				}
				if (health > bestHealth) {
					bestHealth = health;
				}
				if (speed > bestSpeed) {
					bestSpeed = speed;
				}
				if (score > bestScore) {
					bestScore = score;
				}
			}
		Font textRenderer = mc.font;
		float opacity = mc.options.getBackgroundOpacity(0.25F);

		Component[] texts = new Component[4];

		for (AbstractHorse h : horses) {
			double jump = getJump(h);
			double health = h.getMaxHealth();
			double speed = getBaseValue(h, Attributes.MOVEMENT_SPEED);
			double score = score(jump, health, speed);

			texts[0] = Component.literal(STAT_JUMP.getFormattedText(jump, " b", jump >= bestJump));
			texts[1] = Component.literal(
					STAT_HEALTH.getFormattedText(health, " " + ChatFormatting.RED + UTF8_HEART, health >= bestHealth));
			texts[2] = Component.literal(STAT_SPEED.getFormattedText(speed, " m/s", speed >= bestSpeed));

			if (score >= bestScore) {
				texts[3] = Component.literal(ChatFormatting.YELLOW + "" + UTF8_STAR);
			} else {
				texts[3] = null;
			}

			float textHeight = h.getBbHeight() + 0.5F;

			Entity e = h;

			while (!e.getPassengers().isEmpty()) {
				e = e.getPassengers().get(0);
			}

			double textY = e.getY();

			matrices.pushPose();
			matrices.translate(h.getX() - camera.getPosition().x, textY - camera.getPosition().y + textHeight,
					h.getZ() - camera.getPosition().z);
			matrices.mulPose(getRotation(camera.getYRot(), camera.getXRot()));
			matrices.scale(-0.025F, -0.025F, 0.025F);
			Matrix4f matrix4f = matrices.last().pose();
			int background = (int) (opacity * 255.0F) << 24;
			int y = (h.hasCustomName() ? -(textRenderer.lineHeight + 4) : 0);
			for (Component text : texts) {
				if (text == null) {
					continue;
				}

				float x = (float) (-textRenderer.width(text) / 2);
				textRenderer.drawInBatch(text,
						x, (float) y, 0x22FFFFFF, false, matrix4f, source, true, background, 15728880);
				textRenderer.drawInBatch(text,
						x, (float) y, 0xffFFFFFF, false, matrix4f, source, false, 0, 15728880);

				y -= textRenderer.lineHeight + 2;
			}

			matrices.popPose();
		}
	}
}
