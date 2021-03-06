/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of the AcademyCraft mod.
* https://github.com/LambdaInnovation/AcademyCraft
* Licensed under GPLv3, see project root for more information.
*/
package cn.academy.vanilla.electromaster.entity;

import cn.academy.core.AcademyCraft;
import cn.academy.core.event.ConfigModifyEvent;
import cn.academy.vanilla.ModuleVanilla;
import cn.academy.vanilla.electromaster.client.renderer.RendererCoinThrowing;
import cn.academy.vanilla.electromaster.item.ItemCoin;
import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegEntity;
import cn.lambdalib.annoreg.mc.RegEventHandler;
import cn.lambdalib.annoreg.mc.RegEventHandler.Bus;
import cn.lambdalib.annoreg.mc.RegInitCallback;
import cn.lambdalib.s11n.network.NetworkS11n;
import cn.lambdalib.s11n.network.NetworkS11n.ContextException;
import cn.lambdalib.s11n.network.NetworkS11n.NetS11nAdaptor;
import cn.lambdalib.util.entityx.EntityAdvanced;
import cn.lambdalib.util.entityx.MotionHandler;
import cn.lambdalib.util.entityx.handlers.Rigidbody;
import cn.lambdalib.util.generic.RandUtils;
import cn.lambdalib.util.helper.EntitySyncer.SyncType;
import cn.lambdalib.util.helper.EntitySyncer.Synchronized;
import cn.lambdalib.util.mc.PlayerUtils;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;

/**
 * 
 * @author KSkun
 */
@Registrant
@RegEntity
@RegEntity.HasRender
public class EntityCoinThrowing extends EntityAdvanced {

    public static boolean PLAY_HEADS_OR_TAILS;

    static {
        NetworkS11n.addDirect(EntityCoinThrowing.class, new NetS11nAdaptor<EntityCoinThrowing>() {
            @Override
            public void write(ByteBuf buf, EntityCoinThrowing obj) {
                NetworkS11n.serializeWithHint(buf, obj.player, EntityPlayer.class);
            }

            @Override
            public EntityCoinThrowing read(ByteBuf buf) throws ContextException {
                return ItemCoin.getPlayerCoin(NetworkS11n.deserializeWithHint(buf, EntityPlayer.class));
            }
        });
    }

    double yOffset = 0.6;
    
    private class KeepPosition extends MotionHandler<EntityCoinThrowing> {

        public KeepPosition() {}

        @Override
        public void onUpdate() {
            if(EntityCoinThrowing.this.player != null) {
                posX = player.posX;
                posZ = player.posZ;
                if((posY < player.posY && motionY < 0) || ticksExisted > MAXLIFE) {
                    finishThrowing();
                }
            }
            
            maxHt = Math.max(maxHt, posY);
        }

        @Override
        public String getID() {
            return "kip";
        }

        @Override
        public void onStart() {}
        
    }
    
    @SideOnly(Side.CLIENT)
    @RegEntity.Render
    public static RendererCoinThrowing renderer;
    
    private static final int MAXLIFE = 120;
    private static final double INITVEL = 0.92;
    
    //private EntitySyncer syncer;
    
    //@Synchronized(SyncType.ONCE)
    private float initHt;
    private double maxHt;
    
    //@Synchronized(SyncType.ONCE)
    public EntityPlayer player;
    
    public ItemStack stack;
    public Vec3 axis;
    public boolean isSync = false;
    
    public EntityCoinThrowing(World world) {
        super(world);
        isSync = true;
        setup();
    }
    
    public EntityCoinThrowing(EntityPlayer player, ItemStack is) {
        super(player.worldObj);
        this.stack = is;
        this.player = player;
        this.initHt = (float) player.posY;
        setPosition(player.posX, player.posY, player.posZ);
        this.motionY = player.motionY;
        setup();
        this.ignoreFrustumCheck = true;
    }
    
    @Override
    public void onUpdate() {
        if (worldObj.isRemote && isSync)
            setDead();
        //if(!worldObj.isRemote || isSync)
            //syncer.update();
        //System.out.println(initHt + " " + player + " " + worldObj.isRemote);
        super.onUpdate();
    }
    
    private void setup() {
        Rigidbody rb = new Rigidbody();
        rb.gravity = 0.06;
        this.addMotionHandler(rb);
        this.addMotionHandler(new KeepPosition());
        this.motionY += INITVEL;
        axis = Vec3.createVectorHelper(.1 + rand.nextDouble(), rand.nextDouble(), rand.nextDouble());
        this.setSize(0.2F, 0.2F);
    }
    
    void finishThrowing() {
        //try merge
        if(!worldObj.isRemote && !player.capabilities.isCreativeMode) {
            ItemStack equipped = player.getCurrentEquippedItem();
            if(equipped == null) {
                player.setCurrentItemOrArmor(0, new ItemStack(ModuleVanilla.coin));
            } else if(equipped.getItem() == ModuleVanilla.coin && equipped.stackSize < equipped.getMaxStackSize()) {
                ++equipped.stackSize;
                player.inventory.inventoryChanged = true;
            } else if(PlayerUtils.mergeStackable(player.inventory, new ItemStack(
                    ModuleVanilla.coin)) == 0) {
                ;
            } else {
                //if fail...
                worldObj.spawnEntityInWorld(new EntityItem(worldObj, player.posX, player.posY
                    + yOffset, player.posZ, new ItemStack(ModuleVanilla.coin)));
            }
        }
        if (worldObj.isRemote && PLAY_HEADS_OR_TAILS) {
            player.addChatComponentMessage(new ChatComponentTranslation(
                "ac.headsOrTails." + RandUtils.nextInt(2)));
        }
        setDead();
    }
    
    public double getProgress() {
        if(motionY > 0) { //Throwing up
            return (INITVEL - motionY) / INITVEL * 0.5;
        } else {
            return Math.min(1.0, 0.5 + ((maxHt - posY) / (maxHt - initHt)) * 0.5);
        }
    }
    
    @Override
    public void entityInit() {
        //syncer = new EntitySyncer(this);
        
        //syncer.init();
    }
    
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        setDead();
        worldObj.spawnEntityInWorld(new EntityItem(worldObj, posX, posY, posZ, new ItemStack(
                ModuleVanilla.coin)));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound p_70037_1_) {
        
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound p_70014_1_) {
        
    }

    @RegEventHandler(Bus.Forge)
    public static class EventListener {
        @RegInitCallback
        public static void init() {
            PLAY_HEADS_OR_TAILS = AcademyCraft.config.getBoolean("headsOrTails",
                "generic", false, "Show heads or tails after throwing a coin.");
        }

        @SubscribeEvent
        public void onConfigModified(ConfigModifyEvent e) {
            PLAY_HEADS_OR_TAILS = AcademyCraft.config.getBoolean("headsOrTails",
                "generic", false, "Show heads or tails after throwing a coin.");
        }
    }

}
