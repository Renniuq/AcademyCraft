/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of the AcademyCraft mod.
* https://github.com/LambdaInnovation/AcademyCraft
* Licensed under GPLv3, see project root for more information.
*/
package cn.academy.vanilla.teleporter.skills;

import cn.academy.ability.api.Skill;
import cn.academy.ability.api.ctrl.ActionManager;
import cn.academy.ability.api.ctrl.SkillInstance;
import cn.academy.ability.api.ctrl.action.SkillSyncAction;
import cn.academy.core.client.ACRenderingHelper;
import cn.academy.core.client.sound.ACSounds;
import cn.academy.vanilla.generic.entity.EntityBloodSplash;
import cn.academy.vanilla.teleporter.entity.EntityMarker;
import cn.academy.vanilla.teleporter.util.TPSkillHelper;
import cn.lambdalib.util.generic.RandUtils;
import cn.lambdalib.util.generic.VecUtils;
import cn.lambdalib.util.helper.Color;
import cn.lambdalib.util.helper.Motion3D;
import cn.lambdalib.util.mc.EntitySelectors;
import cn.lambdalib.util.mc.Raytrace;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import static cn.lambdalib.util.generic.MathUtils.*;

/**
 * @author WeAthFolD
 */
public class FleshRipping extends Skill {

    public static final FleshRipping instance = new FleshRipping();

    private FleshRipping() {
        super("flesh_ripping", 3);
    }

    @Override
    public SkillInstance createSkillInstance(EntityPlayer player) {
        return new SkillInstance().addChild(new FRAction());
    }

    public static class FRAction extends SkillSyncAction {

        float exp;

        public FRAction() {
            super(-1);
        }

        @Override
        public void onStart() {
            super.onStart();

            exp = aData.getSkillExp(instance);

            if (isRemote) {
                startEffects();
            }
        }

        @Override
        public void onTick() {
            if (isRemote) {
                updateEffects();
            } else {
                if (!cpData.canPerform(getConsumption()))
                    ActionManager.abortAction(this);
            }
        }

        AttackTarget target;

        @Override
        public void writeNBTFinal(NBTTagCompound tag) {
            target = getAttackTarget();
            tag.setTag("x", target.toNBT());
        }

        @Override
        public void readNBTFinal(NBTTagCompound tag) {
            target = new AttackTarget((NBTTagCompound) tag.getTag("x"));
        }

        @Override
        public void onEnd() {
            if (target.target == null) {
                onAbort();
            } else {
                if (!isRemote) {
                    cpData.performWithForce(getOverload(), getConsumption());
                    TPSkillHelper.attack(player, instance, target.target, getDamage());
                    if (RandUtils.ranged(0, 1) < getDisgustProb()) {
                        player.addPotionEffect(new PotionEffect(Potion.confusion.id, 100));
                    }

                    setCooldown(instance, (int) lerpf(100, 60, exp));
                    aData.addSkillExp(instance, .005f);
                }
            }
        }

        @Override
        public void onAbort() {
            target = null;
        }

        @Override
        public void onFinalize() {
            if (isRemote) {
                endEffects();
            }
        }

        private float getDamage() {
            return lerpf(9, 15, exp);
        }

        private float getRange() {
            return lerpf(6, 14, exp);
        }

        private float getDisgustProb() {
            return .05f;
        }

        private float getConsumption() {
            return lerpf(247, 302, exp);
        }

        private float getOverload() {
            return lerpf(60, 50, exp);
        }

        // CLIENT
        @SideOnly(Side.CLIENT)
        EntityMarker marker;

        static final Color DISABLED_COLOR = new Color().setColor4i(74, 74, 74, 160),
                THREATENING_COLOR = new Color().setColor4i(185, 25, 25, 180);

        @SideOnly(Side.CLIENT)
        private void startEffects() {
            if (isLocal()) {
                marker = new EntityMarker(player.worldObj);
                marker.setPosition(player.posX, player.posY, player.posZ);
                marker.color = DISABLED_COLOR;

                player.worldObj.spawnEntityInWorld(marker);
            }
        }

        @SideOnly(Side.CLIENT)
        private void updateEffects() {
            if (isLocal()) {
                AttackTarget at = getAttackTarget();
                marker.setPosition(at.dest.xCoord, at.dest.yCoord, at.dest.zCoord);
                if (at.target == null) {
                    marker.color = DISABLED_COLOR;
                    marker.width = marker.height = 1.0f;
                } else {
                    marker.color = THREATENING_COLOR;
                    marker.width = at.target.width * 1.2f;
                    marker.height = at.target.height * 1.2f;
                }
            }
        }

        @SideOnly(Side.CLIENT)
        private void endEffects() {
            if (isLocal())
                marker.setDead();

            if (target != null && target.target != null) {
                ACSounds.playClient(player, "tp.guts", 0.6f);

                Entity e = target.target;
                int count = RandUtils.rangei(4, 6);
                while (count-- > 0) {
                    double y = e.posY + RandUtils.ranged(0, 1) * e.height;
                    if (e instanceof EntityPlayer)
                        y += ACRenderingHelper.getHeightFix((EntityPlayer) e);

                    double theta = RandUtils.ranged(0, Math.PI * 2);
                    double r = 0.5 * RandUtils.ranged(0.8 * e.width, e.width);

                    EntityBloodSplash splash = new EntityBloodSplash(player.worldObj);
                    splash.setPosition(e.posX + r * Math.sin(theta), y, e.posZ + r * Math.cos(theta));
                    player.worldObj.spawnEntityInWorld(splash);
                }
            }
        }

        private AttackTarget getAttackTarget() {
            double range = getRange();
            MovingObjectPosition trace = Raytrace.traceLiving(player, range, EntitySelectors.living());

            Entity target = null;
            Vec3 dest;

            if (trace != null) {
                target = trace.entityHit;
                dest = trace.hitVec;
            } else {
                dest = new Motion3D(player, true).move(range).getPosVec();
            }

            return new AttackTarget(dest, target);
        }

        class AttackTarget {
            public final Vec3 dest;
            public final Entity target;

            public AttackTarget(Vec3 _dest, Entity _target) {
                dest = _dest;
                target = _target;
            }

            public AttackTarget(NBTTagCompound tag) {
                this(VecUtils.vec(tag.getFloat("x"), tag.getFloat("y"), tag.getFloat("z")),
                        player.worldObj.getEntityByID(tag.getInteger("i")));
            }

            NBTTagCompound toNBT() {
                NBTTagCompound ret = new NBTTagCompound();
                ret.setFloat("x", (float) dest.xCoord);
                ret.setFloat("y", (float) dest.yCoord);
                ret.setFloat("z", (float) dest.zCoord);
                ret.setInteger("i", target == null ? 0 : target.getEntityId());

                return ret;
            }

        }

    }
}
