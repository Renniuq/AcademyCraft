/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of the AcademyCraft mod.
* https://github.com/LambdaInnovation/AcademyCraft
* Licensed under GPLv3, see project root for more information.
*/
package cn.academy.ability.client.render;

import cn.academy.core.client.Resources;
import cn.lambdalib.multiblock.RenderBlockMultiModel;
import cn.lambdalib.util.deprecated.TileEntityModelCustom;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

public class RenderDeveloperAdvanced extends RenderBlockMultiModel {
    
    public RenderDeveloperAdvanced() {
        super(
            new TileEntityModelCustom(Resources.getModel("developer_advanced")), 
            Resources.getTexture("models/developer_advanced"));
        this.scale = 0.5f;
        this.rotateY = 180f;
    }
    
    @Override
    public void drawAtOrigin(TileEntity te) {
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        super.drawAtOrigin(te);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

}
