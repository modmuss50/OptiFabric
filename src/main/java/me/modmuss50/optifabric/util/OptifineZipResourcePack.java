package me.modmuss50.optifabric.util;

import com.google.gson.JsonObject;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ZipResourcePack;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

public class OptifineZipResourcePack extends ZipResourcePack {
	public OptifineZipResourcePack(File file) {
		super(file);
	}

	@Override
	public String getName() {
		return "Optifine Internal Resources";
	}

	@Override
	public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException {
		JsonObject pack = new JsonObject();
		pack.addProperty("pack_format", 5);
		pack.addProperty("description", "Added by OptiFabric\n" + Formatting.RED.toString() + "Do not disable");
		pack.add("pack", new JsonObject());

		if (!pack.has(metaReader.getKey())) {
			return null;
		}

		return metaReader.fromJson(pack);
	}

	public static Supplier<ResourcePack> getSupplier(File file) {
		return () -> new OptifineZipResourcePack(file);
	}
}
