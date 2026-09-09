package cy.jdkdigital.generatorgalore.registry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.init.ModBlockEntityTypes;
import cy.jdkdigital.generatorgalore.util.GeneratorCreator;
import cy.jdkdigital.generatorgalore.util.GeneratorObject;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.neoforgespi.locating.IModFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class GeneratorRegistry
{
    public static Map<Identifier, GeneratorObject> generators = new LinkedHashMap<>();

    public static void discoverGenerators() {
        try {
            discoverGeneratorFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void discoverGeneratorFiles() throws IOException {
        File lockFile = new File(GeneratorUtil.LOCK_FILE.toString(), "defaults.lock");
        boolean firstRun = !lockFile.exists();
        boolean copied = setupDefaultFiles("data/" + GeneratorGalore.MODID + "/generator", Paths.get(GeneratorUtil.GENERATORS.toString()), firstRun);

        if (firstRun && copied) {
            FileUtils.write(lockFile, "This lock file means the standard generator have already been added and you can now do your own custom stuff to them.", StandardCharsets.UTF_8);
        }

        var files = GeneratorUtil.GENERATORS.toFile().listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".json"));
        if (files == null)
            return;

        for (var file : files) {
            JsonObject json;
            InputStreamReader reader = null;
            Identifier id = null;
            GeneratorObject generator = null;

            try {
                var parser = new JsonParser();
                reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                json = parser.parse(reader).getAsJsonObject();
                var name = file.getName().replace(".json", "");
                id = Identifier.fromNamespaceAndPath(GeneratorGalore.MODID, name);

                if (json.has("requiredMod") && !ModList.get().isLoaded(json.get("requiredMod").getAsString())) {
                    continue;
                }

                generator = GeneratorCreator.create(id, json);

                reader.close();
            } catch (Exception e) {
                GeneratorGalore.LOGGER.error("An error occurred while creating generator with id {}", id, e);
            } finally {
                IOUtils.closeQuietly(reader);
            }

            if (generator != null) {
                GeneratorGalore.LOGGER.debug("adding generator " + generator.getId());
                generators.put(generator.getId(), generator);
            } else {
                GeneratorGalore.LOGGER.error("failed to load generator " + id);
            }
        }

//        ModBlockEntityTypes.registerGeneratorBlockEntities();
    }

    public static boolean setupDefaultFiles(String dataPath, Path targetPath, boolean override) {
        IModFile modFile = ModList.get().getModFileById(GeneratorGalore.MODID).getFile();
        GeneratorGalore.LOGGER.debug("Loading generator files from " + dataPath + " to " + targetPath);

        String prefix = dataPath.endsWith("/") ? dataPath : dataPath + "/";
        JarContents contents = modFile.getContents();
        Map<String, byte[]> defaults = new LinkedHashMap<>();
        contents.visitContent((name, resource) -> {
            if (name.startsWith(prefix) && name.endsWith(".json") && name.indexOf('/', prefix.length()) < 0) {
                try {
                    defaults.put(name.substring(prefix.length()), resource.readAllBytes());
                } catch (IOException e) {
                    GeneratorGalore.LOGGER.error("Could not read default generator file: {}", name, e);
                }
            }
        });

        if (defaults.isEmpty()) {
            GeneratorGalore.LOGGER.error("Could not find default generator files at {} in {}", dataPath, modFile.getFilePath());
            return false;
        }
        return copyFiles(defaults, targetPath, override);
    }

    private static boolean copyFiles(Map<String, byte[]> defaults, Path targetPath, boolean override) {
        boolean success = true;
        for (Map.Entry<String, byte[]> entry : defaults.entrySet()) {
            Path target = Paths.get(targetPath.toString(), entry.getKey());
            try {
                if (override || !Files.exists(target)) {
                    Files.write(target, entry.getValue());
                }
            } catch (IOException e) {
                GeneratorGalore.LOGGER.error("Could not copy file: {}, Target: {}", entry.getKey(), target, e);
                success = false;
            }
        }
        return success;
    }
}
