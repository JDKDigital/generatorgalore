package cy.jdkdigital.generatorgalore.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import java.util.HashMap;
import java.util.Map;

public class ModTags
{
    public static Map<ResourceLocation, TagKey<Item>> itemTagCache = new HashMap<>();
    public static Map<ResourceLocation, TagKey<Fluid>> fluidTagCache = new HashMap<>();

    public static TagKey<Item> getItemTag(ResourceLocation resourceLocation) {
        if (!itemTagCache.containsKey(resourceLocation)) {
            itemTagCache.put(resourceLocation, ItemTags.create(resourceLocation));
        }
        return itemTagCache.get(resourceLocation);
    }

    public static TagKey<Fluid> getFluidTag(ResourceLocation resourceLocation) {
        if (!fluidTagCache.containsKey(resourceLocation)) {
            fluidTagCache.put(resourceLocation, FluidTags.create(resourceLocation));
        }
        return fluidTagCache.get(resourceLocation);
    }
}
