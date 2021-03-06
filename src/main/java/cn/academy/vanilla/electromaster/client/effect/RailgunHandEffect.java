/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of the AcademyCraft mod.
* https://github.com/LambdaInnovation/AcademyCraft
* Licensed under GPLv3, see project root for more information.
*/
package cn.academy.vanilla.electromaster.client.effect;

import cn.academy.core.client.Resources;
import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.util.client.renderhook.PlayerRenderHook;
import cn.lambdalib.util.client.shader.GLSLMesh;
import cn.lambdalib.util.client.shader.ShaderSimple;
import cn.lambdalib.util.deprecated.MeshUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL20;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author WeAthFolD
 */
public class RailgunHandEffect extends PlayerRenderHook {
    
    static final int PER_FRAME = 40, COUNT = 40;
    ResourceLocation[] textures;
    GLSLMesh mesh;
    
    public RailgunHandEffect() {
        textures = Resources.getEffectSeq("arc_burst", COUNT);
        mesh = new GLSLMesh();
        mesh = MeshUtils.createBillboard(mesh, -1, -1, 1, 1);
    }

    @Override
    public void renderHand(boolean firstPerson) {
        if(RenderUtils.isInShadowPass()) return;
        
        long dt = getDeltaTime();
        if(dt >= PER_FRAME * COUNT) {
            dispose();
            return;
        }
        
        int frame = (int) (dt / PER_FRAME);
        
        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.0f);
        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        ShaderSimple.instance().useProgram();
        glPushMatrix();
        if(firstPerson) {
            glTranslated(.26, -.12, -.24);
            glScalef(.4f, .4f, 1f);
        } else {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            glTranslated(0, 0.2, -1);
            glRotated(-player.rotationPitch, 1, 0, 0);
        }
        RenderUtils.loadTexture(textures[frame]);
        mesh.draw(ShaderSimple.instance());
        glPopMatrix();
        GL20.glUseProgram(0);
        glAlphaFunc(GL_GEQUAL, 0.1f);
        glEnable(GL_CULL_FACE);
    }
    
}
