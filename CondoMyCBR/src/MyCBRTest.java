import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;

import de.dfki.mycbr.core.DefaultCaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.model.*;
import de.dfki.mycbr.core.retrieval.Retrieval;
import de.dfki.mycbr.core.similarity.FloatFct;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.core.similarity.SymbolFct;
import de.dfki.mycbr.core.similarity.SpecialFct;
import de.dfki.mycbr.core.similarity.config.DistanceConfig;
import de.dfki.mycbr.core.similarity.config.NumberConfig;

public class MyCBRTest {


	public static void main (String[] args) {
		
		try {
			Project p = new Project();
			
			// Create Concept Car
			Concept car = p.createTopConcept("Car");
			
			// add symbol attribute
			HashSet<String> manufacturers = new HashSet<String>();
			String[] manufacturersArray = { "BMW", "Audi", "VW", "Ford",
					"Mercedes", "SEAT", "FIAT" };
			manufacturers.addAll(Arrays.asList(manufacturersArray));
			SymbolDesc manufacturerDesc = new SymbolDesc(car,"manufacturer",manufacturers);
			StringDesc carMake = new StringDesc(car,"Make"); 
			FloatDesc mileage = new FloatDesc(car, "Mileage", 0, 1);
			
			FloatFct floatFct = mileage.addFloatFct("Mileage", true);
			DistanceConfig df=DistanceConfig.DIFFERENCE;
			floatFct.setDistanceFct(df);
			floatFct.setFunctionTypeL(NumberConfig.CONSTANT);
			floatFct.setFunctionTypeR(NumberConfig.CONSTANT);
			floatFct.setFunctionParameterL(0.6);
			floatFct.setFunctionParameterR(1);
			
			// add table function
			SymbolFct manuFct = manufacturerDesc.addSymbolFct("manuFct", true);
			manuFct.setSimilarity("BMW", "Audi", 0.60d);
			manuFct.setSimilarity("Audi", "VW", 0.20d);
			manuFct.setSimilarity("VW", "Ford", 0.40d);
			
			//SpecialFct carMakeFct = carMake.

			// add cassebase
			DefaultCaseBase cb = p.createDefaultCB("myCaseBase");
			
			// add Case
			Instance i = car.addInstance("car1");
			i.addAttribute(manufacturerDesc,manufacturerDesc.getAttribute("Audi"));
			i.addAttribute(mileage, 0.3);
			cb.addCase(i);
			
			// set up query and retrieval
			Retrieval r = new Retrieval(car, cb);
			Instance q = r.getQueryInstance();	
			q.addAttribute(manufacturerDesc.getName(),manufacturerDesc.getAttribute("VW"));
			q.addAttribute(mileage.getName(), 0.2);
			r.start();

			print(r);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private static void print(Retrieval r) {
		for (Entry<Instance, Similarity> entry: r.entrySet()) {
			System.out.println("\nSimilarity: " + entry.getValue().getValue()
					+ " to case: " + entry.getKey());
		}
	}
}