package cy.jdkdigital.generatorgalore.common.item;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.util.GeneratorObject;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class UpgradeItem extends Item
{
    private final String previousTier;
    private final GeneratorObject generator;

    public UpgradeItem(Properties properties, String previousTier, GeneratorObject generator) {
        super(properties);
        this.previousTier = previousTier;
        this.generator = generator;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide()) {
            BlockState state = context.getLevel().getBlockState(context.getClickedPos());
            ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
            if (blockId != null && blockId.getNamespace().equals(GeneratorGalore.MODID) && blockId.getPath().equals(previousTier + "_generator")) {
                GeneratorUtil.replaceGenerator(context.getLevel(), context.getClickedPos(), generator);
                context.getItemInHand().shrink(1);
                return InteractionResult.CONSUME;
            }
        }
        return super.useOn(context);
    }
}
