import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import de.dfki.mycbr.core.DefaultCaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Case;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.core.retrieval.Retrieval;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.core.similarity.SymbolFct;


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
			
			// add table function
			SymbolFct manuFct = manufacturerDesc.addSymbolFct("manuFct", true);
			manuFct.setSimilarity("BMW", "Audi", 0.60d);
			manuFct.setSimilarity("Audi", "VW", 0.20d);
			manuFct.setSimilarity("VW", "Ford", 0.40d);			

			// add cassebase
			DefaultCaseBase cb = p.createDefaultCB("myCaseBase");
			
			// add Case
			Instance i = car.addInstance("car1");
			i.addAttribute(manufacturerDesc,manufacturerDesc.getAttribute("BMW"));
			cb.addCase(i, "car1");
			
			// set up query and retrieval
			Retrieval r = new Retrieval(car);
			Instance q = r.getQuery();	
			q.addAttribute(manufacturerDesc.getName(),manufacturerDesc.getAttribute("Audi"));
			
			r.start();

			print(r);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private static void print(Retrieval r) {
		for (Map.Entry<Case, Similarity> entry: r.entrySet()) {
			System.out.println("\nSimilarity: " + entry.getValue().getValue()
					+ " to case: " + entry.getKey().getID());
		}
	}
}
