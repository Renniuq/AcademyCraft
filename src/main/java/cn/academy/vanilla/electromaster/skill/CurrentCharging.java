/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of the AcademyCraft mod.
* https://github.com/LambdaInnovation/AcademyCraft
* Licensed under GPLv3, see project root for more information.
*/
package cn.academy.vanilla.electromaster.skill;

import cn.academy.ability.api.Skill;
import cn.academy.ability.api.ctrl.ActionManager;
import cn.academy.ability.api.ctrl.SkillInstance;
import cn.academy.ability.api.ctrl.SyncAction;
import cn.academy.ability.api.ctrl.action.SkillSyncAction;
import cn.academy.ability.api.data.AbilityData;
import cn.academy.ability.api.data.CPData;
import cn.academy.core.client.ACRenderingHelper;
import cn.academy.core.client.sound.ACSounds;
import cn.academy.core.client.sound.FollowEntitySound;
import cn.academy.support.EnergyBlockHelper;
import cn.academy.support.EnergyItemHelper;
import cn.academy.vanilla.electromaster.client.effect.ArcPatterns;
import cn.academy.vanilla.electromaster.entity.EntityArc;
import cn.academy.vanilla.electromaster.entity.EntitySurroundArc;
import cn.academy.vanilla.electromaster.entity.EntitySurroundArc.ArcType;
import cn.lambdalib.util.helper.Motion3D;
import cn.lambdalib.util.mc.Raytrace;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

import static cn.lambdalib.util.generic.MathUtils.*;

/**
 * Current charging
 * @author WeAthFolD
 */
public class CurrentCharging extends Skill {

    static final String SOUND = "em.charge_loop";
    public static final CurrentCharging instance = new CurrentCharging();
    
    private CurrentCharging() {
        super("charging", 1);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public SkillInstance createSkillInstance(EntityPlayer player) {
        return new SkillInstance() {
            @Override
            public void onStart() {
                ItemStack stack = getPlayer().getCurrentEquippedItem();
                
                SyncAction action;
                if(stack == null) {
                    action = new ActionChargeBlock();
                } else {
                    action = new ActionChargeItem();
                }
                
                this.addChild(action);
            }
        };
    }
    
    private static float getChargingSpeed(float exp) {
        return lerpf(10, 30, exp);
    }
    
    private static float getExpIncr(boolean effective) {
        if (effective)
            return 0.0001f;
        else
            return 0.00003f;
    }

    private static float getConsumption(float exp) {
        return lerpf(6, 14, exp);
    }

    private static float getOverload(float exp) {
        return lerpf(65, 48, exp);
    }
    
    public static class ActionChargeBlock extends SyncAction {
        
        static final double DISTANCE = 15.0;

        float exp;

        AbilityData aData;
        CPData cpData;

        public ActionChargeBlock() {
            super(-1);
        }
        
        @Override
        public void onStart() {
            aData = AbilityData.get(player);
            cpData = CPData.get(player);

            cpData.perform(getOverload(exp), 0);
            exp = aData.getSkillExp(instance);
            
            if(isRemote)
                startEffects();
        }
        
        @Override
        public void onTick() {
            // Perform raytrace 
            MovingObjectPosition pos = Raytrace.traceLiving(player, DISTANCE);
            
            boolean good = false;
            if(pos != null && pos.typeOfHit == MovingObjectType.BLOCK) {
                TileEntity tile = player.worldObj.getTileEntity(pos.blockX, pos.blockY, pos.blockZ);
                if(EnergyBlockHelper.isSupported(tile)) {
                    good = true;

                    if(!isRemote) {
                        float charge = getChargingSpeed(exp);
                        EnergyBlockHelper.charge(tile, charge, true);
                    }
                }
            }

            aData.addSkillExp(instance, getExpIncr(good));
            if (!cpData.perform(0, getConsumption(exp))) {
                ActionManager.endAction(this);
            }
            
            if(isRemote) {
                updateEffects(pos, good);
            }
        }
        
        @Override
        public void onEnd() {
            if(isRemote)
                endEffects();
        }
        
        @Override
        public void onAbort() {
            if(isRemote)
                endEffects();
        }
        
        //CLIENT
        @SideOnly(Side.CLIENT)
        EntityArc arc;
        
        @SideOnly(Side.CLIENT)
        EntitySurroundArc surround;
        
        @SideOnly(Side.CLIENT)
        FollowEntitySound sound;
        
        @SideOnly(Side.CLIENT)
        private void startEffects() {
            player.worldObj.spawnEntityInWorld(arc = new EntityArc(player, ArcPatterns.chargingArc));
            arc.lengthFixed = false;
            arc.hideWiggle = 0.8;
            arc.showWiggle = 0.2;
            arc.texWiggle = 0.8;
            
            player.worldObj.spawnEntityInWorld(
                surround = new EntitySurroundArc(player.worldObj, player.posX, player.posY, player.posZ, 1, 1)
                .setArcType(ArcType.NORMAL));
            
            sound = new FollowEntitySound(player, SOUND).setLoop();
            ACSounds.playClient(sound);
        }
        
        @SideOnly(Side.CLIENT)
        private void updateEffects(MovingObjectPosition res, boolean isGood) {
            double x, y, z;
            if(res != null) {
                x = res.hitVec.xCoord;
                y = res.hitVec.yCoord;
                z = res.hitVec.zCoord;
                if(res.typeOfHit == MovingObjectType.ENTITY) {
                    y += res.entityHit.getEyeHeight();
                }
            } else {
                Motion3D mo = new Motion3D(player, true).move(DISTANCE);
                x = mo.px;
                y = mo.py;
                z = mo.pz;
            }
            arc.setFromTo(player.posX, player.posY + ACRenderingHelper.getHeightFix(player), player.posZ, x, y, z);
            
            if(isGood) {
                surround.updatePos(res.blockX + 0.5, res.blockY, res.blockZ + 0.5);
                surround.draw = true;
            } else {
                surround.draw = false;
            }
        }
        
        @SideOnly(Side.CLIENT)
        private void endEffects() {
            if(surround != null) surround.setDead();
            if(arc != null) arc.setDead();
            if(sound != null) sound.stop();
        }
        
    }
    
    // TODO: Add hand render effect
    public static class ActionChargeItem extends SkillSyncAction {

        float exp;

        public ActionChargeItem() {
            super(-1);
        }
        
        @Override
        public void onStart() {
            super.onStart();

            exp = aData.getSkillExp(instance);

            if(isRemote)
                startEffects();
        }
        
        @Override
        public void onTick() {
            ItemStack stack = player.getCurrentEquippedItem();
            float cp = getConsumption(exp);

            if(stack != null && cpData.perform(0, cp)) {
                float amt = getChargingSpeed(exp);
                
                boolean good = EnergyItemHelper.isSupported(stack);
                if(good)
                    EnergyItemHelper.charge(stack, amt, false);
                
                aData.addSkillExp(instance, getExpIncr(good));
            } else {
                ActionManager.endAction(this);
            }
        }
        
        @Override
        public void onFinalize() {
            if(isRemote)
                endEffects();
        }
        
        @SideOnly(Side.CLIENT)
        FollowEntitySound sound;
        
        @SideOnly(Side.CLIENT)
        EntitySurroundArc surround;
        
        @SideOnly(Side.CLIENT)
        private void startEffects() {
            ACSounds.playClient(sound = new FollowEntitySound(player, SOUND).setLoop());
            surround = new EntitySurroundArc(player);
            surround.setArcType(ArcType.THIN);
            world.spawnEntityInWorld(surround);
        }    
        
        @SideOnly(Side.CLIENT)
        private void endEffects() {
            sound.stop();
            surround.setDead();
        }
    }

}
