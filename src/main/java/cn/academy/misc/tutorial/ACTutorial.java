/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of the AcademyCraft mod.
* https://github.com/LambdaInnovation/AcademyCraft
* Licensed under GPLv3, see project root for more information.
*/
package cn.academy.misc.tutorial;

import cn.academy.core.client.Resources;
import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.util.generic.RegistryUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Registrant
public class ACTutorial {

    public enum Tag {
        CRAFT, SMELT, VIEW;

        public final ResourceLocation icon = Resources.getTexture(
                "guis/icons/icon_" + this.name().toLowerCase());
    }

    public static final boolean SHOW_ALL = false;

    public final String id;

    private Condition condition = Conditions.alwaysTrue();
    private boolean defaultInstalled = true;

    private List<IPreviewHandler> previewHandlers = new ArrayList<>();

    public ACTutorial(String id) {
        this.id=id;
    }

    public ACTutorial setCondition(Condition condition) {
        defaultInstalled = false;
        this.condition=condition;
        return this;
    }

    public ACTutorial addPreview(IPreviewHandler ...handlers) {
        previewHandlers.addAll(Arrays.asList(handlers));
        return this;
    }

    public List<IPreviewHandler> getPreview() {
        return previewHandlers;
    }

    @SideOnly(Side.CLIENT)
    public String getContent() {
        final String unknown = "![title]\nUNKNOWN \n![brief]\n![content]\n ";
        try {
            String lang = Minecraft.getMinecraft().gameSettings.language;
            InputStream stream = RegistryUtils.getResourceStream(location(lang));
            if (stream == null) { // Make en_US the default fallback
                stream = RegistryUtils.getResourceStream(location("en_US"));
            }
            if (stream == null) {
                return unknown;
            } else {
                return IOUtils.toString(new InputStreamReader(stream, Charset.forName("UTF-8").newDecoder()));
            }
        } catch (NullPointerException|IOException e) {
            return unknown;
        }
    }

    /**
     * Note that this method currently requires IO and is inefficient. Don't call it too often.
     */
    @SideOnly(Side.CLIENT)
    public String getTitle() {
        String raw = getContent();
        int i1 = raw.indexOf("![title]"),
                i2 = raw.indexOf("![brief]");
        return raw.substring(i1+8, i2).trim();
    }

    private ResourceLocation location(String lang) {
        return new ResourceLocation("academy:tutorials/" + lang + "/" + id + ".md");
    }

    public boolean isActivated(EntityPlayer player) {
        if (SHOW_ALL)
            return true;
        return this.condition.test(player);
    }

    public boolean isDefaultInstalled() {
        return defaultInstalled;
    }

}
