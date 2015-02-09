import java.util.HashMap;
import java.util.LinkedList;
import de.dfki.mycbr.core.DefaultCaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.FloatDesc;
import de.dfki.mycbr.core.model.StringDesc;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.core.model.IntegerDesc;
import de.dfki.mycbr.io.CSVImporter;
import de.dfki.mycbr.core.similarity.FloatFct;
import de.dfki.mycbr.core.similarity.IntegerFct;
import de.dfki.mycbr.core.similarity.StringFct;
import de.dfki.mycbr.core.similarity.SymbolFct;
import de.dfki.mycbr.core.similarity.config.DistanceConfig;
import de.dfki.mycbr.core.similarity.config.NumberConfig;
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
			

			System.out.println(condo.getAttributeDescs());
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
			
			// 2. Boro-Block-Lot

			StringDesc lot = (StringDesc) attMap.get("Boro-Block-Lot");
			StringFct lotSim = new StringFct(p,StringConfig.EQUALITY,lot,"LotSim");
			lotSim.setCaseSensitive(false);
			lotSim.setSymmetric(true);
			lot.addFct(lotSim);


			// 3. BuildingClassification

			SymbolDesc buildingClass = (SymbolDesc) attMap.get("BuildingClassification");
			SymbolFct buildSim = buildingClass.addSymbolFct("buildSim", true);
			buildSim.setSymmetric(true);
			buildSim.setSimilarity("R2-CONDOMINIUM", "R4-CONDOMINIUM", 0.60d);
			
			// 4. CondoSection
			StringDesc condoSection = (StringDesc) attMap.get("CondoSection");
			StringFct condoSectionSim = new StringFct(p,StringConfig.EQUALITY,lot,"CondoSectionSim");
			condoSectionSim.setCaseSensitive(false);
			condoSectionSim.setSymmetric(true);
			condoSection.addFct(condoSectionSim);
			
			// 5.Neighborhood

			StringDesc neighborhood = (StringDesc) attMap.get("Neighborhood");
			StringFct neighborhoodSim = new StringFct(p,StringConfig.EQUALITY,neighborhood,"NeighborhoodSim");
			neighborhoodSim.setCaseSensitive(false);
			neighborhoodSim.setSymmetric(true);
			neighborhood.addFct(neighborhoodSim);
			
			// 6.Total Units
			IntegerDesc totalUnits = (IntegerDesc) attMap.get("TotalUnits");
			IntegerFct  totalUnitSim = new IntegerFct(p,totalUnits,"TotalUnitsSim");
			totalUnitSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			totalUnitSim.setFunctionTypeL(NumberConfig.CONSTANT);
			totalUnitSim.setFunctionTypeR(NumberConfig.CONSTANT);
			totalUnitSim.setFunctionParameterL(0.5);
			totalUnitSim.setFunctionParameterR(0.5);
			totalUnits.addFct(totalUnitSim);
			
			//7.Year Built
			IntegerDesc yearBuilt = (IntegerDesc) attMap.get("YearBuilt");
			IntegerFct  yearBuiltSim = new IntegerFct(p,yearBuilt,"YearBuiltSim");
			yearBuiltSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			yearBuiltSim.setFunctionTypeL(NumberConfig.CONSTANT);
			yearBuiltSim.setFunctionTypeR(NumberConfig.CONSTANT);
			yearBuiltSim.setFunctionParameterL(0.5);
			yearBuiltSim.setFunctionParameterR(0.5);
			yearBuilt.addFct(yearBuiltSim);
			
			//8.GrossSqFt
			IntegerDesc grossSqFt = (IntegerDesc) attMap.get("GrossSqFt");
			IntegerFct  grossSqFtSim = new IntegerFct(p,grossSqFt,"GrossSqFtSim");
			grossSqFtSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			grossSqFtSim.setFunctionTypeL(NumberConfig.CONSTANT);
			grossSqFtSim.setFunctionTypeR(NumberConfig.CONSTANT);
			grossSqFtSim.setFunctionParameterL(0.5);
			grossSqFtSim.setFunctionParameterR(0.5);
			grossSqFt.addFct(grossSqFtSim);
			
			//9.GrossIncomePerSqFt
			FloatDesc grossIncomePerSqFt = (FloatDesc) attMap.get("GrossIncomePerSqFt");
			FloatFct  grossIncomePerSqFtSim = new FloatFct(p,grossIncomePerSqFt,"GrossIncomePerSqFtSim");
			grossIncomePerSqFtSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			grossIncomePerSqFtSim.setFunctionTypeL(NumberConfig.CONSTANT);
			grossIncomePerSqFtSim.setFunctionTypeR(NumberConfig.CONSTANT);
			grossIncomePerSqFtSim.setFunctionParameterL(0.5);
			grossIncomePerSqFtSim.setFunctionParameterR(0.5);
			grossIncomePerSqFt.addFct(grossIncomePerSqFtSim);

			//10.ExpensePerSqFt
			FloatDesc expensePerSqFt = (FloatDesc) attMap.get("ExpensePerSqFt");
			FloatFct  expensePerSqFtSim = new FloatFct(p,expensePerSqFt,"ExpensePerSqFtSim");
			expensePerSqFtSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			expensePerSqFtSim.setFunctionTypeL(NumberConfig.CONSTANT);
			expensePerSqFtSim.setFunctionTypeR(NumberConfig.CONSTANT);
			expensePerSqFtSim.setFunctionParameterL(0.5);
			expensePerSqFtSim.setFunctionParameterR(0.5);
			expensePerSqFt.addFct(expensePerSqFtSim);
			
			//11.NetOperatingIncome
			IntegerDesc netOperatingIncome = (IntegerDesc) attMap.get("NetOperatingIncome");
			IntegerFct  netOperatingIncomeSim = new IntegerFct(p,netOperatingIncome,"netOperatingIncomeSim");
			netOperatingIncomeSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			netOperatingIncomeSim.setFunctionTypeL(NumberConfig.CONSTANT);
			netOperatingIncomeSim.setFunctionTypeR(NumberConfig.CONSTANT);
			netOperatingIncomeSim.setFunctionParameterL(0.5);
			netOperatingIncomeSim.setFunctionParameterR(0.5);
			netOperatingIncome.addFct(netOperatingIncomeSim);
			
			//12.FullMarketValue
			IntegerDesc fullMarketValue = (IntegerDesc) attMap.get("FullMarketValue");
			IntegerFct  fullMarketValueSim = new IntegerFct(p,fullMarketValue,"FullMarketValueSim");
			fullMarketValueSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			fullMarketValueSim.setFunctionTypeL(NumberConfig.CONSTANT);
			fullMarketValueSim.setFunctionTypeR(NumberConfig.CONSTANT);
			fullMarketValueSim.setFunctionParameterL(0.5);
			fullMarketValueSim.setFunctionParameterR(0.5);
			fullMarketValue.addFct(fullMarketValueSim);
			
			//13.MarketValuePerSqFt
			FloatDesc marketValuePerSqFt = (FloatDesc) attMap.get("MarketValuePerSqFt");
			FloatFct  marketValuePerSqFtSim = new FloatFct(p,marketValuePerSqFt,"MarketValuePerSqFtSim");
			marketValuePerSqFtSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			marketValuePerSqFtSim.setFunctionTypeL(NumberConfig.CONSTANT);
			marketValuePerSqFtSim.setFunctionTypeR(NumberConfig.CONSTANT);
			marketValuePerSqFtSim.setFunctionParameterL(0.5);
			marketValuePerSqFtSim.setFunctionParameterR(0.5);
			marketValuePerSqFt.addFct(marketValuePerSqFtSim);
			
			// other pairs sim ?
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
