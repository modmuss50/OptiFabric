package me.modmuss50.optifabric.patcher;

import net.fabricmc.tinyremapper.IMappingProvider;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RemapUtils {

	public static IMappingProvider getTinyRemapper(File mappings, String from, String to) {
		return TinyUtils.createTinyMappingProvider(mappings.toPath(), from, to);
	}

	public static void mapJar(Path output, Path input, File mappings, List<Path> libraries, String from, String to) throws IOException {
		mapJar(output, input, getTinyRemapper(mappings, from, to), libraries);
	}

	public static void mapJar(Path output, Path input, IMappingProvider mappings, List<Path> libraries) throws IOException {
		Files.deleteIfExists(output);

		TinyRemapper remapper = TinyRemapper.newRemapper().withMappings(mappings).renameInvalidLocals(true).rebuildSourceFilenames(true).build();

		try {
			OutputConsumerPath outputConsumer = new OutputConsumerPath(output);
			outputConsumer.addNonClassFiles(input);
			remapper.readInputs(input);

			for (Path path : libraries) {
				remapper.readClassPath(path);
			}

			remapper.apply(outputConsumer);
			outputConsumer.close();
			remapper.finish();
		} catch (Exception e) {
			remapper.finish();
			throw new RuntimeException("Failed to remap jar", e);
		}
	}

}
