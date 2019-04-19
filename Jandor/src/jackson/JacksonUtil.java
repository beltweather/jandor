package jackson;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import util.CardUtil;
import util.FileUtil;

public class JacksonUtil {

	public static <T> T readExternal(Class<T> klass, String filename) {
		return read(klass, FileUtil.getExternalResourcesFile(filename));
	}

	public static <T> T read(Class<T> klass, File file) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		SimpleModule module = new SimpleModule();
		module.addDeserializer(String.class, new StringCleanerDeserializer());
		mapper.registerModule(module);

		T data = null;
		try {
			data = mapper.readValue(file, klass);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static <T> T read(Class<T> klass, String json) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		SimpleModule module = new SimpleModule();
		module.addDeserializer(String.class, new StringCleanerDeserializer());
		mapper.registerModule(module);

		T data = null;
		try {
			data = mapper.readValue(json, klass);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public static <T> boolean writeExternal(T data, String filename) {
		return write(data, FileUtil.getExternalResourcesFile(filename));
	}

	public static <T> boolean write(T data, File file) {
		if(data == null) {
			return false;
		}

		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

		try {
			writer.writeValue(file, data);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static <T> T readAndWriteExternal(Class<T> klass, String fromFilename, String toFilename) {
		return readAndWrite(klass,
							FileUtil.getExternalResourcesFile(fromFilename),
							FileUtil.getExternalResourcesFile(toFilename));
	}

	public static <T> T readAndWrite(Class<T> klass, File fromFile, File toFile) {
		T data = read(klass, fromFile);
		write(data, toFile);
		return data;
	}

	public static void main(String[] args) {
		AllCardsJson cards = readExternal(AllCardsJson.class, FileUtil.RESOURCE_CARDS_JSONS);
		AllSetsJson sets = readExternal(AllSetsJson.class, FileUtil.RESOURCE_SETS_JSONS);

		cards.init(sets);

		writeExternal(cards, FileUtil.RESOURCE_CARDS_LESS_JSONS);
		writeExternal(sets, FileUtil.RESOURCE_SETS_LESS_JSONS);
	}

	public static class StringCleanerDeserializerWithWeirdCheck extends StringCleanerDeserializer {

		private static final long serialVersionUID = 1L;

		public StringCleanerDeserializerWithWeirdCheck() {
			super(true);
		}

	}

	public static class StringCleanerDeserializer extends StdDeserializer<String> {

		private static final long serialVersionUID = 1L;
		private boolean checkForWeird = false;

		public StringCleanerDeserializer() {
			this(false);
		}

		public StringCleanerDeserializer(boolean checkForWeird) {
			super((Class<?>) null);
			this.checkForWeird = checkForWeird;
		}

		@Override
		public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			String clean = CardUtil.clean(p.getText());
			if(checkForWeird && CardUtil.isWeirdName(clean)) {
				System.err.println("Found weird string: " + clean);
			}
			return clean;
		}

	}

	public static class UpperCaseDeserializer extends StdDeserializer<String> {

		private static final long serialVersionUID = 1L;

		public UpperCaseDeserializer() {
			super((Class<?>) null);
		}

		@Override
		public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			String s = p.getText();
			if(s != null) {
				return s.toUpperCase();
			}
			return s;
		}

	}

}
