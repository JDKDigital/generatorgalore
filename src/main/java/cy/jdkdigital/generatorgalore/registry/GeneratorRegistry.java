package cy.jdkdigital.generatorgalore.registry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cy.jdkdigital.generatorgalore.GeneratorGalore;
import cy.jdkdigital.generatorgalore.util.GeneratorCreator;
import cy.jdkdigital.generatorgalore.util.GeneratorObject;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
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
        if (!lockFile.exists()) {
            FileUtils.write(lockFile, "This lock file means the standard generators have already been added and you can now do your own custom stuff to them.", StandardCharsets.UTF_8);
            setupDefaultFiles("/data/" + GeneratorGalore.MODID + "/generators", Paths.get(GeneratorUtil.GENERATORS.toString()));
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
                id = new ResourceLocation(GeneratorGalore.MODID, name);

                if (json.has("requiredMod") && !ModList.get().isLoaded(json.get("requiredMod").getAsString())) {
                    continue;
                }

                // TODO Validate required attributes

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
    }

    public static void setupDefaultFiles(String dataPath, Path targetPath) {
        List<Path> roots = List.of(ModList.get().getModFileById(GeneratorGalore.MODID).getFile().getFilePath());
        GeneratorGalore.LOGGER.info("[Generator Galore] Pulling defaults from: " + roots);

        if (roots.isEmpty()) {
            throw new RuntimeException("Failed to load defaults.");
        }

        for (Path modRoot : roots) {
            setupDefaultFiles(dataPath, targetPath, modRoot);
        }
    }

    public static void setupDefaultFiles(String dataPath, Path targetPath, Path modPath) {
        if (Files.isRegularFile(modPath)) {
            try(FileSystem fileSystem = FileSystems.newFileSystem(modPath)) {
                Path path = fileSystem.getPath(dataPath);
                if (Files.exists(path)) {
                    copyFiles(path, targetPath);
                }
            } catch (IOException e) {
                GeneratorGalore.LOGGER.error("Could not load source {}!!", modPath);
                e.printStackTrace();
            }
        } else if (Files.isDirectory(modPath)) {
            copyFiles(Paths.get(modPath.toString(), dataPath), targetPath);
        }
    }

    private static void copyFiles(Path source, Path targetPath) {
        try (Stream<Path> sourceStream = Files.walk(source)) {
            sourceStream.filter(f -> f.getFileName().toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            Files.copy(path, Paths.get(targetPath.toString(), path.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            GeneratorGalore.LOGGER.error("Could not copy file: {}, Target: {}", path, targetPath);
                        }
                    });
        } catch (IOException e) {
            GeneratorGalore.LOGGER.error("Could not stream source files: {}", source);
        }
    }
}
