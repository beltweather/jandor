package util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.SerializationUtils;

import canvas.IRenderable;
import deck.RenderableList;

public class SerializationUtil {
	
	private SerializationUtil() {}
	
	/** Read the object from Base64 string. */
	public static Object fromString(String s) {
		try {
			byte [] data = Base64.decodeBase64(s.getBytes());
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		    Object o  = ois.readObject();
		    ois.close();
		    return o;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/** Write the object to a Base64 string. */
	public static String toString(Serializable o) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream( baos );
			oos.writeObject( o );
			oos.close();
			return new String(Base64.encodeBase64(baos.toByteArray())); 
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Object fromBytes(byte[] bytes) {
		
		/*try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
		    Object o  = ois.readObject();
		    ois.close();
		    return o;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;*/
		
		return SerializationUtils.deserialize(bytes);
	}
	
	public static byte[] toBytes(Serializable o) {
		return SerializationUtils.serialize(o);
		
		/*try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream( baos );
			oos.writeObject( o );
			oos.close();
			return baos.toByteArray(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;*/
		
	}

}
