package jackson;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JacksonUtil {

	private static final String allCardsFilePath = "X:/Users/Jon/Jandor/Jandor-Data/Resources/AllCards.json";
	private static final String allCardsLessFilePath = "X:/Users/Jon/Jandor/Jandor-Data/Resources/AllCards-less.json";
	private static final String allSetsFilePath = "X:/Users/Jon/Jandor/Jandor-Data/Resources/AllSets.json";
	private static final String allSetsLessFilePath = "X:/Users/Jon/Jandor/Jandor-Data/Resources/AllSets-less.json";

	public static <T> T read(Class<T> klass, String fromFilePath) {
		File file = new File(fromFilePath);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		T data = null;
		try {
			data = mapper.readValue(file, klass);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static <T> boolean write(T data, String toFilePath) {
		if(data == null) {
			return false;
		}

		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

		try {
			writer.writeValue(new File(toFilePath), data);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static <T> T readAndWrite(Class<T> klass, String fromFilePath, String toFilePath) {
		T data = read(klass, fromFilePath);
		write(data, toFilePath);
		return data;
	}

	public static void main(String[] args) {
		AllCardsJson cards = read(AllCardsJson.class, allCardsFilePath);
		AllSetsJson sets = read(AllSetsJson.class, allSetsFilePath);

		cards.init(sets);

		write(cards, allCardsLessFilePath);
		write(sets, allSetsLessFilePath);
	}

}
