/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of the AcademyCraft mod.
* https://github.com/LambdaInnovation/AcademyCraft
* Licensed under GPLv3, see project root for more information.
*/
package cn.academy.support.rf;

import cn.academy.crafting.ModuleCrafting;
import cn.academy.energy.ModuleEnergy;
import cn.academy.support.BlockConverterBase;
import cn.academy.support.EnergyBlockHelper;
import cn.lambdalib.annoreg.core.RegWithName;
import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegBlock;
import cn.lambdalib.annoreg.mc.RegInitCallback;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

@Registrant
public class RFSupport {
    
    /** The convert rate (1IF = <CONV_RATE> RF) */
    public static final double CONV_RATE = 4;
    
    @RegBlock(item = BlockConverterBase.Item.class)
    @RegWithName("rf_input")
    public static Block rfInput = new BlockRFInput();
    
    @RegBlock(item = BlockConverterBase.Item.class)
    @RegWithName("rf_output")
    public static Block rfOutput = new BlockRFOutput();
    
    // Convert macros, dividing by hand is error-prone
    /**
     * Converts RF to equivalent amount of IF.
     */
    public static double rf2if(int rfEnergy) {
        return rfEnergy / CONV_RATE;
    }
    /**
     * Converts IF to equivalent amount of RF.
     */
    public static int if2rf(double ifEnergy) {
        return (int) (ifEnergy * CONV_RATE);
    }

    @RegInitCallback
    public static void init() {
        EnergyBlockHelper.register(new RFProviderManager());
        EnergyBlockHelper.register(new RFReceiverManager());
        
        GameRegistry.addRecipe(new ItemStack(rfInput), "abc", " d ",
                'a', ModuleEnergy.energyUnit, 'b', ModuleCrafting.machineFrame,
                'c', ModuleCrafting.constPlate, 'd', ModuleCrafting.convComp);
        
        GameRegistry.addRecipe(new ItemStack(rfOutput), "abc", " d ",
                'a', ModuleEnergy.energyUnit, 'b', ModuleCrafting.machineFrame,
                'c', ModuleCrafting.resoCrystal, 'd', ModuleCrafting.convComp);
        
        GameRegistry.addRecipe(new ItemStack(rfInput), "X",'X',new ItemStack(rfOutput));
        GameRegistry.addRecipe(new ItemStack(rfOutput), "X",'X',new ItemStack(rfInput));
    }
    
}
