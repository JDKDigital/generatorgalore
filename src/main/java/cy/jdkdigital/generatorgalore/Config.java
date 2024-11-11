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

        public General(ModConfigSpec.Builder builder) {
            builder.push("General");

            tickRate = builder
                    .comment("Generator tickrateaka aka how often should the generators tick. Default is once every 5 ticks. Increase if you're having performance issues")
                    .defineInRange("tickRate", 5, 1, 64);

            builder.pop();
        }
    }
}