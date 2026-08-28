package cy.jdkdigital.generatorgalore.registry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.init.ModBlockEntityTypes;
import cy.jdkdigital.generatorgalore.util.GeneratorCreator;
import cy.jdkdigital.generatorgalore.util.GeneratorObject;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;
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
    public static Map<ResourceLocation, GeneratorObject> generators = new LinkedHashMap<>();

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
            ResourceLocation id = null;
            GeneratorObject generator = null;

            try {
                var parser = new JsonParser();
                reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                json = parser.parse(reader).getAsJsonObject();
                var name = file.getName().replace(".json", "");
                id = ResourceLocation.fromNamespaceAndPath(GeneratorGalore.MODID, name);

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

        Path source = modFile.findResource(dataPath.split("/"));
        if (!Files.exists(source)) {
            GeneratorGalore.LOGGER.error("Could not find default generator files at {} in {}", dataPath, modFile.getFilePath());
            return false;
        }
        return copyFiles(source, targetPath, override);
    }

    private static boolean copyFiles(Path source, Path targetPath, boolean override) {
        List<Path> sourceFiles;
        try (Stream<Path> sourceStream = Files.walk(source)) {
            sourceFiles = sourceStream.filter(f -> f.getFileName().toString().endsWith(".json")).toList();
        } catch (IOException e) {
            GeneratorGalore.LOGGER.error("Could not stream source files: {}", source);
            GeneratorGalore.LOGGER.error(e.getLocalizedMessage());
            return false;
        }

        boolean success = true;
        for (Path path : sourceFiles) {
            Path target = Paths.get(targetPath.toString(), path.getFileName().toString());
            try {
                if (override) {
                    Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.copy(path, target);
                }
            } catch (FileAlreadyExistsException e) {
            } catch (IOException e) {
                GeneratorGalore.LOGGER.error("Could not copy file: {}, Target: {}", path, target, e);
                success = false;
            }
        }
        return success;
    }
}
