/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of the AcademyCraft mod.
* https://github.com/LambdaInnovation/AcademyCraft
* Licensed under GPLv3, see project root for more information.
*/
package cn.academy.ability.api.ctrl.action;

import cn.academy.ability.api.Controllable;
import cn.academy.ability.api.cooldown.CooldownData;
import cn.academy.ability.api.ctrl.SyncAction;
import cn.academy.ability.api.data.AbilityData;
import cn.academy.ability.api.data.CPData;
import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.s11n.network.NetworkS11n.NetworkS11nType;
import net.minecraft.world.World;

/**
 * A simple wrapper that setup the commonly used data and sandbox methods for Skill SyncActions.
 * @author WeAthFolD
 */
@Registrant
@NetworkS11nType
public class SkillSyncAction extends SyncAction {
    
    public AbilityData aData;
    public CPData cpData;
    public World world;

    public SkillSyncAction() {
        super(-1);
    }
    
    public SkillSyncAction(int interval) {
        super(interval);
    }
    
    @Override
    public void onStart() {
        aData = AbilityData.get(player);
        cpData = CPData.get(player);
        world = player.worldObj;
    }
    
    /**
     * Add cooldown to a skill if the currently the SyncAction is local.
     */
    public void setCooldown(Controllable c, int time) {
        CooldownData.of(player).set(c, time);
    }
    
}
