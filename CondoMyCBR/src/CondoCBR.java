import de.dfki.mycbr.core.DefaultCaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.io.CSVImporter;


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
			
			System.out.println(condo.getAttributeDescs());
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
