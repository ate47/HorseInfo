package fr.atesab.horseInfo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(version="1.0.1",name="HorseInfo", modid = "horseinfo")
public class ModMain {
	@Mod.Instance("horseinfo")
    public static ModMain instance;
    @EventHandler
	public void preInit(FMLPreInitializationEvent ev){
		MinecraftForge.EVENT_BUS.register(instance);
    	FMLCommonHandler.instance().bus().register(instance);
	}
	@SubscribeEvent
	public void RenderOverlay(RenderGameOverlayEvent ev){
        Minecraft mc = Minecraft.getMinecraft();
		if(ev.getType().equals(RenderGameOverlayEvent.ElementType.DEBUG)){
			if(mc.player.getRidingEntity() instanceof AbstractHorse){
				AbstractHorse baby=(AbstractHorse)mc.player.getRidingEntity();
    			drawInventory(mc, ev.getResolution().getScaledWidth(), ev.getResolution().getScaledHeight(), 
    					getEntityData(baby),
    				baby);
    		}else{
				RayTraceResult obj=mc.objectMouseOver;
				if(obj!=null && obj.typeOfHit.equals(RayTraceResult.Type.ENTITY) && obj.entityHit instanceof EntityLivingBase){
					drawInventory(mc, ev.getResolution().getScaledWidth(), ev.getResolution().getScaledHeight(), getEntityData((EntityLivingBase)obj.entityHit),(EntityLivingBase)obj.entityHit);
				}
    		}
		}
	}
	public static String[] getEntityData(EntityLivingBase entity){
		List<String> text= new ArrayList<String>();
		text.add(ChatFormatting.AQUA+entity.getDisplayName().getFormattedText());
		if(entity instanceof AbstractHorse){
			AbstractHorse baby=(AbstractHorse)entity;
			text.add(I18n.format("gui.act.invView.horse.jump")+  " : "+getJumpFormattedText(getRoudedDouble(GetHorseMaxJump(baby),4))+" "
					+ "("+getRoudedDouble(baby.getHorseJumpStrength(),4)+" iu)");
			text.add(I18n.format("gui.act.invView.horse.speed")+ " : "+getSpeedFormattedText(getRoudedDouble(baby.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * 43,3))+" m/s "
					+ "("+getRoudedDouble(baby.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue(),4)+" iu)");
			text.add(I18n.format("gui.act.invView.horse.health")+" : "+getHpFormattedText(((int)(baby.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue())*10)/20)+" HP");
		}
		return text.toArray(new String[text.size()]);
	}
	public static double getRoudedDouble(double n, int sign) {
		return Double.valueOf(String.format("%."+sign+"G", n)).doubleValue();
	}
    public static double exellentSpeed = 13;//max: 14.1?
    public static double badSpeed = 9;		//min: ~7?
    public static double exellentJump = 5;	//max: 5.5?
    public static double badJump = 2.75;	//min: 1.2
    public static double exellentHP = 14;	//max: 15
    public static double badHP = 10;		//min: 7.5
    
    public static String getHpFormattedText(double hp){
    	String a=ChatFormatting.GREEN.toString();;
    	if(hp>exellentHP)a=ChatFormatting.GOLD.toString();
    	if(hp<badHP)a=ChatFormatting.RED.toString();
    	return a+hp;
    }
    public static String getJumpFormattedText(double jump){
    	String a=ChatFormatting.GREEN.toString();;
    	if(jump>exellentJump)a=ChatFormatting.GOLD.toString();
    	if(jump<badJump)a=ChatFormatting.RED.toString();
    	return a+jump;
    }
    public static String getSpeedFormattedText(double speed){
    	String a=ChatFormatting.GREEN.toString();;
    	if(speed>exellentSpeed)a=ChatFormatting.GOLD.toString();
    	if(speed<badSpeed)a=ChatFormatting.RED.toString();
    	return a+speed;
    }
    public static double GetHorseMaxJump(AbstractHorse horse)
    {
    	double yVelocity = horse.getHorseJumpStrength();
    	double jumpHeight = 0;
    	while (yVelocity > 0){
    		jumpHeight += yVelocity;
    		yVelocity -= 0.08;
    		yVelocity *= 0.98;
    	}
    	return jumpHeight;
    }
	public static double square(double a){
		return a*a;
	}
	public static double getDistance(Entity et1,Entity et2){
		return getDistance(et1.posX, et1.posY, et1.posZ, et2.posX, et2.posY, et2.posZ);
	}
	public static double getDistance(double posX,double posY,double posZ,double posX2,double posY2,double posZ2){
		return Math.sqrt(square(posX-posX2)+square(posZ-posZ2)+square(posZ-posZ2));
	}
	public static void drawInventory(Minecraft mc,int posX,int posY,String[] addText,EntityLivingBase entity){
		int l=addText.length;
		if(l==0)return;
		int sizeX=0;
		int sizeY=0;
		int itemSize=20;
		if(mc.fontRenderer.FONT_HEIGHT*2+2>itemSize)itemSize=mc.fontRenderer.FONT_HEIGHT*2+2;
		for (int i = 0; i < addText.length; i++) {
			sizeY+=mc.fontRenderer.FONT_HEIGHT+1;
			int a = mc.fontRenderer.getStringWidth(addText[i])+10;
			if(a>sizeX)sizeX=a;
		}
		if(entity!=null){
			sizeX+=100;
			if(sizeY<100)sizeY=100;
		}
		ScaledResolution sr=new ScaledResolution(mc);
		posX+=5;
		posY+=5;
		if(posX+sizeX>sr.getScaledWidth())posX-=sizeX+10;
		if(posY+sizeY>sr.getScaledHeight())posY-=sizeY+10;
		int posY1=posY+5;
		for (int i = 0; i < addText.length; i++){
			mc.fontRenderer.drawStringWithShadow(addText[i], posX+5, posY1, Color.WHITE.getRGB());
			posY1+=(mc.fontRenderer.FONT_HEIGHT+1);
		}
		if(entity!=null){
			GlStateManager.color(1.0F, 1.0F, 1.0F);
			GuiInventory.drawEntityOnScreen(posX+sizeX-55, posY+105, 50, 50, 0, entity);
		}
	}
}
