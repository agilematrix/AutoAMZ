package core;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openqa.selenium.By;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



import core.CacheEngine;

public class UtilitiesFactory {
	
	protected static Map<String,String> dataMap = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
	protected static Map<String,String> objMap = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
	CacheEngine<String,String> cache = new CacheEngine<String, String>(1, 1, 190);
	public String getData(String data) {
		return dataMap.get(data);
	}
	public void cacheObjRepository() {
		String key = "";
		String value = "";
		Class<? extends UtilitiesFactory> thisClass =  this.getClass();
		Field[] fields = thisClass.getFields();
		for (int i = 0; i < fields.length; i++) {
			try{
				key = fields[i].get(this).toString();	
				value = fields[i].getName();
				//The following if block was added to deal with Object repository entries that have 
				//different variable names, but the same variable values 
				if(objMap.containsKey(key)){
					value = value+" | "+objMap.get(key);
				}
				objMap.put(key, value);
				cache.put(key, value);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	

	/**
	 * get test data from TestData.xml
	 * @param data String - A String containing the key to the item that is in the projects test data.
	 * @return Returns a String containing the test data or null if the item is not found.
	 */


	public String getDataOrDefault(String verificationText) {
		String data = getData(verificationText);
		return data == null ? verificationText : data;
	}
	
	/**
	 * Gets the variable name associated with a locator definition in the Object repository. 
	 * If there are multiple entries that have the same definition but have different names, 
	 * each of the names will be returned separated by the pipe "|" character.
	 * @param locator - Selenium "By" object. The value should take the form of a
	 *            variable name from the project Object Repository.
	 * @return Returns a String containing the variable name associated with the By locator.
	 */
	public String getObjName(By locator) {
		return objMap.get(locator.toString());
	}
	

	public void loadXMLData() {
		String key = "";
		String value = "";
		try {
			
			System.out.println("------------- Caching Test Data! File :" +"testDataFileName"+ " ------------");
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document document = docBuilder.parse(new File("resources/"+"testDataFileName"));
			NodeList nodeList = document.getElementsByTagName("*");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					int noOfChildNodes = node.getChildNodes().getLength();
					for (int j = 0; j < noOfChildNodes; j++) {
						if (node.getChildNodes().item(j).getNodeType() == Node.ELEMENT_NODE
								&& node.getChildNodes().item(j).getNodeType() != Node.TEXT_NODE
								&& node.getChildNodes().item(j).getChildNodes().getLength() <= 1)
						{
							key = node.getChildNodes().item(j).getNodeName().trim();
							value = node.getChildNodes().item(j).getTextContent().trim();
							dataMap.put(node.getNodeName()+"."+key, value);
							cache.put(key, value);
						}
					}
				}
			}

		}
		catch(FileNotFoundException fe) {
			System.err.println("WARNING: No test data XML found to cache.");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
