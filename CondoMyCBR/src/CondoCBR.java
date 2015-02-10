import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

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
import de.dfki.mycbr.core.retrieval.Retrieval;
import de.dfki.mycbr.core.similarity.FloatFct;
import de.dfki.mycbr.core.similarity.IntegerFct;
import de.dfki.mycbr.core.similarity.Similarity;
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
			
			readData("data/processedCondo_latest.csv");
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
            // Sim measure
			StringDesc lot = (StringDesc) attMap.get("Boro-Block-Lot");
			StringFct lotSim = new StringFct(p,StringConfig.EQUALITY,lot,"LotSim");
			lotSim.setCaseSensitive(false);
			lotSim.setSymmetric(true);
			lot.addFct(lotSim);


			// 3. BuildingClassification
            
			SymbolDesc buildingClass = (SymbolDesc) attMap.get("BuildingClassification");
			SymbolFct buildSim = buildingClass.addSymbolFct("buildSim", true);
			buildSim.setSymmetric(true);
			buildSim.setSimilarity("R2-CONDOMINIUM", "R4-CONDOMINIUM", (2.0/14.0));
			buildSim.setSimilarity("R2-CONDOMINIUM", "R9-CONDOMINIUM", (7.0/14.0));
			buildSim.setSimilarity("R2-CONDOMINIUM", "RR-CONDOMINIUM", (14.0/14.0));
			buildSim.setSimilarity("R4-CONDOMINIUM", "R9-CONDOMINIUM", (5.0/14.0));
			buildSim.setSimilarity("R4-CONDOMINIUM", "RR-CONDOMINIUM", (12.0/14.0));
			buildSim.setSimilarity("R9-CONDOMINIUM", "RR-CONDOMINIUM", (7.0/14.0));
			
			// 4. CondoSection
			//Drop this
			/*
			StringDesc condoSection = (StringDesc) attMap.get("CondoSection");
			StringFct condoSectionSim = new StringFct(p,StringConfig.EQUALITY,lot,"CondoSectionSim");
			condoSectionSim.setCaseSensitive(false);
			condoSectionSim.setSymmetric(true);
			condoSection.addFct(condoSectionSim);
			*/
			
			// 5.Neighborhood
			StringDesc neighborhood = (StringDesc) attMap.get("Neighborhood");
			StringFct neighborhoodSim = new StringFct(p,StringConfig.NGRAM,neighborhood,"NeighborhoodSim");
			neighborhoodSim.setCaseSensitive(false);
			neighborhoodSim.setSymmetric(true);
			neighborhood.addFct(neighborhoodSim);
			
			// 6.Total Units
			//Upper Bound
			//If the number of units are slightly more, we penalize more heavily
			IntegerDesc totalUnits = (IntegerDesc) attMap.get("TotalUnits");
			IntegerFct  totalUnitSim = new IntegerFct(p,totalUnits,"TotalUnitsSim");
			totalUnitSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			totalUnitSim.setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
			totalUnitSim.setFunctionTypeR(NumberConfig.POLYNOMIAL_WITH);
			totalUnitSim.setFunctionParameterL(1.0);
			totalUnitSim.setFunctionParameterR(2.0);
			totalUnits.addFct(totalUnitSim);
			
			//7.Year Built
			IntegerDesc yearBuilt = (IntegerDesc) attMap.get("YearBuilt");
			IntegerFct  yearBuiltSim = new IntegerFct(p,yearBuilt,"YearBuiltSim");
			yearBuiltSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			yearBuiltSim.setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
			yearBuiltSim.setFunctionTypeR(NumberConfig.CONSTANT);
			yearBuiltSim.setFunctionParameterL(10);
			yearBuiltSim.setFunctionParameterR(1.0);
			yearBuilt.addFct(yearBuiltSim);
			
			//8.AreaSqFt
			//rename to area in square feet
			FloatDesc areaSqFt = (FloatDesc) attMap.get("AreaSqFt");
			FloatFct  areaSqFtSim = new FloatFct(p,areaSqFt,"AreaSqFtSim");
			areaSqFtSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			areaSqFtSim.setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
			areaSqFtSim.setFunctionTypeR(NumberConfig.POLYNOMIAL_WITH);
			areaSqFtSim.setFunctionParameterL(5.0);
			areaSqFtSim.setFunctionParameterR(3.0);
			areaSqFt.addFct(areaSqFtSim);
			
			//9.GrossIncomePerSqFt
			FloatDesc grossIncomePerSqFt = (FloatDesc) attMap.get("GrossIncomePerSqFt");
			FloatFct  grossIncomePerSqFtSim = new FloatFct(p,grossIncomePerSqFt,"GrossIncomePerSqFtSim");
			grossIncomePerSqFtSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			grossIncomePerSqFtSim.setFunctionTypeL(NumberConfig.CONSTANT);
			grossIncomePerSqFtSim.setFunctionTypeR(NumberConfig.CONSTANT);
			grossIncomePerSqFtSim.setFunctionParameterL(1.0);
			grossIncomePerSqFtSim.setFunctionParameterR(1.0);
			grossIncomePerSqFt.addFct(grossIncomePerSqFtSim);

			//10.ExpensePerSqFt
			FloatDesc expensePerSqFt = (FloatDesc) attMap.get("ExpensePerSqFt");
			FloatFct  expensePerSqFtSim = new FloatFct(p,expensePerSqFt,"ExpensePerSqFtSim");
			expensePerSqFtSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			expensePerSqFtSim.setFunctionTypeL(NumberConfig.CONSTANT);
			expensePerSqFtSim.setFunctionTypeR(NumberConfig.CONSTANT);
			expensePerSqFtSim.setFunctionParameterL(1.0);
			expensePerSqFtSim.setFunctionParameterR(1.0);
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
			
			
			
			// set up query and retrieval
			Retrieval r = new Retrieval(condo, cb);
			Instance q = r.getQueryInstance();	
			q.addAttribute(yearBuilt.getName(),1990);
			//q.addAttribute(mileage.getName(), 0.2);
			r.start();

			print(r,yearBuilt);
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
	
	private static void print(Retrieval r, AttributeDesc d) {
		for (Entry<Instance, Similarity> entry: r.entrySet()) {
			System.out.println("\nSimilarity: " + entry.getValue().getValue()
					+ " to case: " + entry.getKey().getAttForDesc(d));
		}
	}
	
}
