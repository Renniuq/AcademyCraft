/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of the AcademyCraft mod.
* https://github.com/LambdaInnovation/AcademyCraft
* Licensed under GPLv3, see project root for more information.
*/
package cn.academy.ability.api.registry;

import cn.academy.ability.api.Category;
import cn.academy.ability.api.CategoryManager;
import cn.academy.ability.api.registry.CategoryRegistration.RegCategory;
import cn.lambdalib.annoreg.base.RegistrationFieldSimple;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this anno to register category on the fly.
 * @author WeAthFolD
 */
@RegistryTypeDecl
public class CategoryRegistration extends RegistrationFieldSimple<RegCategory, Category> {
    
    public CategoryRegistration() {
        super(RegCategory.class, "AC_Category");
        setLoadStage(LoadStage.PRE_INIT);
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RegCategory {}

    @Override
    protected void register(Category value, RegCategory anno, String field)
            throws Exception {
        CategoryManager.INSTANCE.register(value);
    }
    
}
