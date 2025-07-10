package cy.jdkdigital.generatorgalore;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config
{
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec CLIENT_CONFIG;
    public static final Client CLIENT = new Client(CLIENT_BUILDER);
    public static final ModConfigSpec SERVER_CONFIG;
    public static final General SERVER = new General(SERVER_BUILDER);

    static {
        CLIENT_CONFIG = CLIENT_BUILDER.build();
        SERVER_CONFIG = SERVER_BUILDER.build();
    }

    public static class Client
    {
        public Client(ModConfigSpec.Builder builder) {
            builder.push("Client");

            builder.pop();
        }
    }

    public static class General
    {
        public final ModConfigSpec.IntValue tickRate;
        public final ModConfigSpec.BooleanValue increasedConsumption;

        public General(ModConfigSpec.Builder builder) {
            builder.push("General");

            tickRate = builder
                    .comment("Generator tickrateaka aka how often should the generator tick. Default is once every 5 ticks. Increase if you're having performance issues")
                    .defineInRange("tickRate", 5, 1, 64);

            increasedConsumption = builder
                    .comment("Make 8x and 64x increase the consumption rate and not just the production rate. This will be default on in 1.22")
                    .define("increasedConsumption", false);

            builder.pop();
        }
    }
}