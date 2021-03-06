/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of the AcademyCraft mod.
* https://github.com/LambdaInnovation/AcademyCraft
* Licensed under GPLv3, see project root for more information.
*/
package cn.academy.crafting.api;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.networkcall.s11n.InstanceSerializer;
import cn.lambdalib.networkcall.s11n.RegSerializable;
import cn.lambdalib.s11n.network.NetworkS11n;
import cn.lambdalib.s11n.network.NetworkS11n.ContextException;
import cn.lambdalib.s11n.network.NetworkS11n.NetS11nAdaptor;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;

import java.util.ArrayList;
import java.util.List;

/**
 * Recipe holder of ImagFusor.
 * @author WeAthFolD
 */
@Registrant
public class ImagFusorRecipes {

    public static ImagFusorRecipes INSTANCE = new ImagFusorRecipes();
    
    private List<IFRecipe> recipeList = new ArrayList<>();
    
    public void addRecipe(ItemStack consume, int liquid, ItemStack output) {
        addRecipe(new IFRecipe(consume, liquid, output));
    }
    
    public void addRecipe(IFRecipe recipe) {
        for(IFRecipe r : recipeList) {
            if(r.matches(recipe.consumeType)) {
                throw new RuntimeException("Can't register multiple recipes for same item " + recipe.consumeType.getItem() + 
                        "(#" + recipe.consumeType.getItemDamage() + ")!!");
            }
        }
        
        recipeList.add(recipe);
        recipe.id = recipeList.size() - 1;
    }
    
    public IFRecipe getRecipe(ItemStack input) {
        for(IFRecipe r : recipeList) {
            if(r.matches(input))
                return r;
        }
        return null;
    }
    
    public List<IFRecipe> getAllRecipe() {
        return recipeList;
    }

    public static class IFRecipe {
        
        int id;
        public final ItemStack consumeType;
        public final int consumeLiquid;
        public final ItemStack output;
        
        public IFRecipe(ItemStack stack, int liq, ItemStack _output) {
            consumeType = stack;
            consumeLiquid = liq;
            output = _output;
        }
        
        public boolean matches(ItemStack input) {
            return consumeType.getItem() == input.getItem() && consumeType.getItemDamage() == input.getItemDamage();
        }
        
        public int getID() {
            return id;
        }
        
    }
    
    static {
        NetworkS11n.addDirect(IFRecipe.class, new NetS11nAdaptor<IFRecipe>() {
            @Override
            public void write(ByteBuf buf, IFRecipe obj) {
                buf.writeInt(obj.id);
            }
            @Override
            public IFRecipe read(ByteBuf buf) throws ContextException {
                return INSTANCE.recipeList.get(buf.readInt());
            }
        });
    }
    
}
