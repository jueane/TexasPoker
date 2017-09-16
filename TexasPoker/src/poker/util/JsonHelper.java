package poker.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JsonHelper {
	private static ObjectMapper objectMapper = new ObjectMapper();

	public static byte[] toJson(Object object) {
		try {
			return objectMapper.writeValueAsString(object).getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.err.println(e.getMessage());
		} catch (JsonProcessingException e) {
			System.err.println(e.getMessage());
		}
		return null;
	}

	@JsonIgnore
	public static <T> T parseFromJson(String buff, Class<T> t) {
		T obj = null;
		try {
			obj = objectMapper.readValue(buff, t);
		} catch (JsonParseException e1) {
			e1.printStackTrace();
		} catch (JsonMappingException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return obj;
	}

	@JsonIgnore
	public static <T> List<T> parseFromJsonToList(String buff, Class<T> t) {
		TypeFactory tf = TypeFactory.defaultInstance();
		List<T> list = null;
		try {
			list = objectMapper.readValue(buff, tf.constructCollectionType(ArrayList.class, t));
		} catch (JsonParseException e1) {
			e1.printStackTrace();
		} catch (JsonMappingException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return list;
	}

}
