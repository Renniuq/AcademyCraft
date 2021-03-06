/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of the AcademyCraft mod.
* https://github.com/LambdaInnovation/AcademyCraft
* Licensed under GPLv3, see project root for more information.
*/
package cn.academy.core.client.ui;

import cn.academy.ability.api.Category;
import cn.academy.ability.api.Skill;
import cn.academy.ability.api.context.ContextManager;
import cn.academy.ability.api.data.AbilityData;
import cn.academy.ability.api.data.CPData;
import cn.academy.core.ModuleCoreClient;
import cn.academy.core.client.Resources;
import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegInitCallback;
import cn.lambdalib.util.client.auxgui.AuxGui;
import cn.lambdalib.util.client.auxgui.AuxGuiHandler;
import cn.lambdalib.util.client.font.IFont;
import cn.lambdalib.util.client.font.IFont.FontOption;
import cn.lambdalib.util.helper.Color;
import cn.lambdalib.util.key.KeyHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * The overall debug console. Use BACKSPACE to switch between states.
 * @author WeAthFolD
 */
@Registrant
public class DebugConsole extends AuxGui {
    
    private static class Text {
        final String text;
        final FontOption option;
        
        public Text(String _text, float _size, int _color) {
            text = _text;
            option = new FontOption(_size, new Color(_color));
        }
        
        public Text(String _text, float _size) {
            this(_text, _size, 0xffffffff);
        }
        
        public Text(String _text) {
            this(_text, 10);
        }
    }
    
    static DebugConsole INSTANCE;

    @RegInitCallback
    public static void init() {
        AuxGuiHandler.register(INSTANCE = new DebugConsole());
        ModuleCoreClient.keyManager.addKeyHandler("debug_console", Keyboard.KEY_F4, new KeyHandler() {
            @Override
            public void onKeyDown() {
                State[] states = State.values();
                INSTANCE.state = states[(INSTANCE.state.ordinal() + 1) % states.length];
            }
        });
    }

    enum State { NONE, NORMAL, SHOW_EXP }

    State state = State.NONE;
    
    private DebugConsole() {}
    
    @Override
    public boolean isForeground() {
        return false;
    }

    @Override
    public void draw(ScaledResolution sr) {
        if(state == State.NONE)
            return;
        
        List<Text> texts = new ArrayList<>();
        texts.add(new Text("AcademyCraft developer info"));
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        
        AbilityData aData = AbilityData.get(player);
        CPData cpData = CPData.get(player);

        switch (state) {
        case NORMAL:
            if (!aData.hasCategory()) {
                texts.add(new Text("Ability not acquired"));
            } else {
                texts.add(new Text(aData.getCategory().getName()));

                texts.add(new Text("Level " + aData.getLevel()));
                texts.add(new Text(String.format("CP:       %.0f/%.0f(%.1f+%.1f)", cpData.getCP(), cpData.getMaxCP(), cpData.getRawMaxCP(), cpData.getAddMaxCP())));
                texts.add(new Text(String.format("Overload: %.0f/%.0f(%.1f+%.1f)", cpData.getOverload(), cpData.getMaxOverload(), cpData.getRawMaxOverload(), cpData.getAddMaxOverload())));
                texts.add(new Text("CPData.canUseAbility: " + cpData.canUseAbility()));
                texts.add(new Text("CPData.activated: " + cpData.isActivated()));
                texts.add(new Text("CPData.addMaxCP: " + cpData.getAddMaxCP()));
                texts.add(new Text("CPData.interfering: " + cpData.isInterfering()));
            }
            break;
        case SHOW_EXP:
            texts.add(new Text("Skill status"));
            if (aData.hasCategory()) {
                Category cat = aData.getCategory();
                for(Skill s : cat.getSkillList()) {
                    StringBuilder sb = new StringBuilder(s.getName());
                    for(int i = 0; i < 30 - s.getName().length(); ++i)
                        sb.append(' ');
                    if(aData.isSkillLearned(s)) {
                        sb.append(String.format("%.1f", aData.getSkillExp(s) * 100)).append('%');
                    } else {
                        sb.append("[not learned]");
                    }

                    texts.add(new Text(sb.toString()));
                }
            }
            break;
        }
        
        iter(texts, 10.5, 10.5, 0.2);
        iter(texts, 10, 10, 1);
    }

    private void iter(List<Text> texts, double x, double y, double lumMul) {
        IFont font = Resources.font();
        for(Text text : texts) {
            Color crl = text.option.color;
            double pr = crl.r, pg = crl.g, pb = crl.b;
            crl.r *= lumMul;
            crl.g *= lumMul;
            crl.b *= lumMul;

            font.draw(text.text, x, y, text.option);

            crl.r = pr;
            crl.g = pg;
            crl.b = pb;

            y += text.option.fontSize;
        }
    }

}
