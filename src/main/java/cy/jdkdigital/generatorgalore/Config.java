package cy.jdkdigital.generatorgalore;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Config
{
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CLIENT_CONFIG;
    public static final Client CLIENT = new Client(CLIENT_BUILDER);
    public static final ForgeConfigSpec SERVER_CONFIG;
    public static final General SERVER = new General(SERVER_BUILDER);

    static {
        CLIENT_CONFIG = CLIENT_BUILDER.build();
        SERVER_CONFIG = SERVER_BUILDER.build();
    }

    public static class Client
    {
        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("Client");

            builder.pop();
        }
    }

    public static class General
    {
        public final ForgeConfigSpec.IntValue tickRate;

        public General(ForgeConfigSpec.Builder builder) {
            builder.push("General");

            tickRate = builder
                    .comment("Generator tickrate. Increase if you're having performance issues")
                    .defineInRange("tickRate", 5, 1, 64);

            builder.pop();
        }
    }
}