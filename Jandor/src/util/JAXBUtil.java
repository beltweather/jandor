package util;

import java.io.File;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JAXBUtil {
	
	private JAXBUtil() {}
	
	public static void print(Object obj) {
		marshal(obj, System.out);
	}
	
	public static void marshal(Object obj, File file) {
		
		try {
			
			JAXBContext jaxbContext;
			jaxbContext = JAXBContext.newInstance(obj.getClass());
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(obj, file);

		} catch (JAXBException e) {
			e.printStackTrace();
		}

	}
	
	public static void marshal(Object obj, OutputStream out) {
		
		try {
			
			JAXBContext jaxbContext;
			jaxbContext = JAXBContext.newInstance(obj.getClass());
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(obj, out);

		} catch (JAXBException e) {
			e.printStackTrace();
		}

	}

	public static void marshal(Object obj, StringWriter writer) {
		
		try {
			
			JAXBContext jaxbContext;
			jaxbContext = JAXBContext.newInstance(obj.getClass());
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(obj, writer);

		} catch (JAXBException e) {
			e.printStackTrace();
		}

	}
	
	public static Object unmarshal(File file) {
		String name = file.getName();
		
		Class klass = null;
		try {
			klass = Class.forName("session." + name.split("-")[0]);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		
		if(klass == null) {
			return null;
		}
		
		try {
			
			JAXBContext jaxbContext = JAXBContext.newInstance(klass);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Object obj = jaxbUnmarshaller.unmarshal(file);
			return obj;

		} catch (JAXBException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static Object unmarshal(Class klass, String xmlString) {
		if(klass == null || xmlString == null) {
			return null;
		}
		
		StringReader reader = new StringReader(xmlString);
		
		try {
			
			JAXBContext jaxbContext = JAXBContext.newInstance(klass);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Object obj = jaxbUnmarshaller.unmarshal(reader);
			reader.close();
			return obj;

		} catch (JAXBException e) {
			e.printStackTrace();
		}

		return null;
	}
}
