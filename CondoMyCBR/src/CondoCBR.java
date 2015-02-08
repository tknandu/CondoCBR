import java.util.HashMap;
import java.util.LinkedList;
import de.dfki.mycbr.core.DefaultCaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.StringDesc;
import de.dfki.mycbr.io.CSVImporter;
import de.dfki.mycbr.core.similarity.StringFct;
import de.dfki.mycbr.core.similarity.config.StringConfig;


public class CondoCBR {
	
	static Project p;
	static Concept condo;
	static DefaultCaseBase cb;
	
	public static void readData(String fileName)
	{
		CSVImporter importer = new CSVImporter(fileName, condo);
		importer.readData();
		importer.checkData();
		importer.addMissingValues();
		importer.addMissingDescriptions();
		importer.doImport();
		importer.setCaseBase(cb);
		
		System.out.print("Importing case base...  ");
		while(importer.isImporting()){}
		System.out.println("Done");
		
	}
	
	public static void main(String[] args)
	{
		try {
			p = new Project();
			condo =  p.createTopConcept("Condo");
			cb = p.createDefaultCB("myCaseBase");
			
			readData("data/postProcessedCondoData.csv");
			System.out.println("No of cases read: "+cb.getCases().size());
			
			LinkedList<Instance> cases = (LinkedList<Instance>) cb.getCases();
			HashMap<String,AttributeDesc> attMap = cases.get(0).getConcept().getAllAttributeDescs();
	    
			// Defining similarity functions for each attribute
			
			// 1. Address
			StringDesc address = (StringDesc) attMap.get("Address");
			StringFct addressSim = new StringFct(p,StringConfig.NGRAM,address,"AddressSim");
			addressSim.setCaseSensitive(false);
			addressSim.setSymmetric(true);
			addressSim.setN(3); // trigram similarity
			System.out.println(cases.get(0).getAttForDesc(address));
			System.out.println(cases.get(1).getAttForDesc(address));
			System.out.println(addressSim.calculateSimilarity(cases.get(0).getAttForDesc(address), cases.get(1).getAttForDesc(address)));
			
			
			/*
			Iterator it = attMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        System.out.println(pairs.getKey() + " = " + (AttributeDesc)pairs.getValue());
	    }
			
			System.out.println(cases.get(0).getConcept().getAttributeDesc("Address"));
			*/
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
