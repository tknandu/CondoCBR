import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

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


public class CondoCBRRegression {
	
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
			

			System.out.println(condo.getAttributeDescs());
			LinkedList<Instance> cases = (LinkedList<Instance>) cb.getCases();
			HashMap<String,AttributeDesc> attMap = cases.get(0).getConcept().getAllAttributeDescs();
	    
			// Defining similarity functions for each attribute
			
			// 1. Address
			StringDesc address = (StringDesc) attMap.get("Address");
			//StringFct addressSim = (StringFct) address.getFct("default function");
			StringFct addressSim = address.addStringFct(StringConfig.NGRAM, "default function", true);
			addressSim.setCaseSensitive(false);
			addressSim.setSymmetric(true);
			addressSim.setN(3); // trigram similarity
			
			// 2. Boro-Block-Lot
			StringDesc lot = (StringDesc) attMap.get("Boro-Block-Lot");
			StringFct lotSim = (StringFct) lot.getFct("default function");
			lotSim.setCaseSensitive(false);
			lotSim.setSymmetric(true);

			// 3. BuildingClassification
            
			SymbolDesc buildingClass = (SymbolDesc) attMap.get("BuildingClassification");
			SymbolFct buildSim = (SymbolFct) buildingClass.getFct("default function");
			buildSim.setSymmetric(true);
			buildSim.setSimilarity("R2-CONDOMINIUM", "R4-CONDOMINIUM", (2.0/14.0));
			buildSim.setSimilarity("R2-CONDOMINIUM", "R9-CONDOMINIUM", (7.0/14.0));
			buildSim.setSimilarity("R2-CONDOMINIUM", "RR-CONDOMINIUM", (14.0/14.0));
			buildSim.setSimilarity("R4-CONDOMINIUM", "R9-CONDOMINIUM", (5.0/14.0));
			buildSim.setSimilarity("R4-CONDOMINIUM", "RR-CONDOMINIUM", (12.0/14.0));
			buildSim.setSimilarity("R9-CONDOMINIUM", "RR-CONDOMINIUM", (7.0/14.0));
			
			// 5.Neighborhood
			//Need to check if this is fine or needs to be ngram
			StringDesc neighborhood = (StringDesc) attMap.get("Neighborhood");
			StringFct neighborhoodSim = (StringFct) neighborhood.getFct("default function");
			neighborhoodSim.setCaseSensitive(false);
			neighborhoodSim.setSymmetric(true);
			
			// 6.Total Units
			//Upper Bound
			//If the number of units are slightly more, we penalize more heavily
			IntegerDesc totalUnits = (IntegerDesc) attMap.get("TotalUnits");
			IntegerFct  totalUnitSim = (IntegerFct) totalUnits.getFct("default function");
			totalUnitSim.setSymmetric(false);
			totalUnitSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			totalUnitSim.setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
			totalUnitSim.setFunctionTypeR(NumberConfig.POLYNOMIAL_WITH);
			totalUnitSim.setFunctionParameterL(1.0);
			totalUnitSim.setFunctionParameterR(2.0);
			
			//7.Year Built
			IntegerDesc yearBuilt = (IntegerDesc) attMap.get("YearBuilt");
			IntegerFct yearBuiltSim = (IntegerFct) yearBuilt.getFct("default function");
			yearBuiltSim.setSymmetric(false);
			yearBuiltSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			yearBuiltSim.setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
			yearBuiltSim.setFunctionTypeR(NumberConfig.CONSTANT);
			yearBuiltSim.setFunctionParameterL(1.0);
			yearBuiltSim.setFunctionParameterR(1.0);
			
			//8.AreaSqFt
			//rename to area in square feet
			FloatDesc areaSqFt = (FloatDesc) attMap.get("AreaSqFt");
			FloatFct  areaSqFtSim = (FloatFct) areaSqFt.getFct("default function");
			areaSqFtSim.setSymmetric(false);
			areaSqFtSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			areaSqFtSim.setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
			areaSqFtSim.setFunctionTypeR(NumberConfig.POLYNOMIAL_WITH);
			areaSqFtSim.setFunctionParameterL(5.0);
			areaSqFtSim.setFunctionParameterR(3.0);
			
			//9.GrossIncomePerSqFt
			FloatDesc grossIncomePerSqFt = (FloatDesc) attMap.get("GrossIncomePerSqFt");
			FloatFct  grossIncomePerSqFtSim = (FloatFct) grossIncomePerSqFt.getFct("default function");
			grossIncomePerSqFtSim.setSymmetric(false);
			grossIncomePerSqFtSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			grossIncomePerSqFtSim.setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
			grossIncomePerSqFtSim.setFunctionTypeR(NumberConfig.CONSTANT);
			grossIncomePerSqFtSim.setFunctionParameterL(2.0);
			grossIncomePerSqFtSim.setFunctionParameterR(1.0);

			//10.ExpensePerSqFt
			FloatDesc expensePerSqFt = (FloatDesc) attMap.get("ExpensePerSqFt");
			FloatFct  expensePerSqFtSim = (FloatFct) expensePerSqFt.getFct("default function");
			expensePerSqFtSim.setSymmetric(false);
			expensePerSqFtSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			expensePerSqFtSim.setFunctionTypeL(NumberConfig.CONSTANT);
			expensePerSqFtSim.setFunctionTypeR(NumberConfig.POLYNOMIAL_WITH);
			expensePerSqFtSim.setFunctionParameterL(1.0);
			expensePerSqFtSim.setFunctionParameterR(2.0);

			//11.NetOperatingIncome
			IntegerDesc netOperatingIncome = (IntegerDesc) attMap.get("NetOperatingIncome");
			IntegerFct  netOperatingIncomeSim = (IntegerFct) netOperatingIncome.getFct("default function");
			netOperatingIncomeSim.setSymmetric(false);
			netOperatingIncomeSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			netOperatingIncomeSim.setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
			netOperatingIncomeSim.setFunctionTypeR(NumberConfig.CONSTANT);
			netOperatingIncomeSim.setFunctionParameterL(2.0);
			netOperatingIncomeSim.setFunctionParameterR(1.0);
			
			//12.FullMarketValue
			//To be dropped
			IntegerDesc fullMarketValue = (IntegerDesc) attMap.get("FullMarketValue");
			IntegerFct  fullMarketValueSim = (IntegerFct) fullMarketValue.getFct("default function");
			fullMarketValueSim.setSymmetric(false);
			fullMarketValueSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			fullMarketValueSim.setFunctionTypeL(NumberConfig.CONSTANT);
			fullMarketValueSim.setFunctionTypeR(NumberConfig.CONSTANT);
			fullMarketValueSim.setFunctionParameterL(1.0);
			fullMarketValueSim.setFunctionParameterR(1.0);
			
			//13.MarketValuePerSqFt
			//Needs to be set(in the case of recommendation)
			FloatDesc marketValuePerSqFt = (FloatDesc) attMap.get("MarketValuePerSqFt");
			FloatFct  marketValuePerSqFtSim = (FloatFct) marketValuePerSqFt.getFct("default function");
			marketValuePerSqFtSim.setSymmetric(false);
			marketValuePerSqFtSim.setDistanceFct(DistanceConfig.DIFFERENCE);
			marketValuePerSqFtSim.setFunctionTypeL(NumberConfig.CONSTANT);
			marketValuePerSqFtSim.setFunctionTypeR(NumberConfig.CONSTANT);
			marketValuePerSqFtSim.setFunctionParameterL(1.0);
			marketValuePerSqFtSim.setFunctionParameterR(1.0);
			
			// set up query and retrieval
			Retrieval r = new Retrieval(condo, cb);
			Instance q = r.getQueryInstance();	
			q.addAttribute(address,"AVENUE" );
			//q.addAttribute(lot,"1-0007-7501");
			//q.addAttribute(buildingClass,"R9-CONDOMINIUM");
			//q.addAttribute(neighborhood,"FINANCIAL");
			//q.addAttribute(totalUnits,42);
			//q.addAttribute(yearBuilt, 1920);
			//q.addAttribute(areaSqFt, 850);
			//q.addAttribute(grossIncomePerSqFt,36.51);
			//q.addAttribute(expensePerSqFt,9.2);
			//q.addAttribute(netOperatingIncome, 990000);
			r.start();
			print(r,address,marketValuePerSqFt);

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void print(Retrieval r, AttributeDesc d,AttributeDesc priceDescriptor) {
		
		    HashMap<Instance,Similarity> map = (HashMap<Instance, Similarity>) r;
	        ValueComparatorRegression bvc =  new ValueComparatorRegression(map);
	        TreeMap<Instance,Similarity> sorted_map = new TreeMap<Instance,Similarity>(bvc);
	        sorted_map.putAll(map);
	        int K=4;
	        int index=0;
	        double predictedPrice=0.0;
	        for (Entry<Instance, Similarity> entry: sorted_map.entrySet()) {
	        	System.out.println("\nSimilarity: " + entry.getValue().getValue()
	        			+ " to case: " + entry.getKey().getAttForDesc(d).getValueAsString());
	        	double entryPrice=Double.parseDouble(entry.getKey().getAttForDesc(priceDescriptor).getValueAsString());
	        	predictedPrice+=entryPrice/(K+0.0);
	        	index+=1;
	        	if(index>=K)
	        	{
	        		break;
	        	}
	        }
	        System.out.println("Regressed per square feet price:"+predictedPrice);
		
	}
	
}

class ValueComparatorRegression implements Comparator<Instance> {

    Map<Instance, Similarity> base;
    public ValueComparatorRegression(Map<Instance, Similarity> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    public int compare(Instance a, Instance b) {
        if (base.get(a).getValue() >= base.get(b).getValue()) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}
