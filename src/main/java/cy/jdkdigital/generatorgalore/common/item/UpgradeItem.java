package cy.jdkdigital.generatorgalore.common.item;

import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.util.GeneratorObject;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

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
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (blockId.getNamespace().equals(GeneratorGalore.MODID) && blockId.getPath().equals(previousTier + "_generator")) {
                GeneratorUtil.replaceGenerator(context.getLevel(), context.getClickedPos(), generator);
                if (!context.getPlayer().isCreative()) {
                    context.getItemInHand().shrink(1);
                }
                context.getPlayer().swing(context.getHand());
                return InteractionResult.CONSUME;
            }
        }
        return super.useOn(context);
    }
}
