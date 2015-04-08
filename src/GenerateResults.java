package at.smartBuyer.metadata;

import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class GenerateResults {	
	
	public void updateTotalFrequency(){
		
	}
	
	public void updateFrequency(){
		
	}
	
	public void searchLastSeenDate(){
		
	}
	
	public void calculateAppearanceRate(){
		
	}
	
	public void calculateAveragePrice(){
		
	}
	
	public int calculateMarketability(int oneweekquantity){
		int marketability;
		if(oneweekquantity >= 1000){
			marketability = 10;
		} else if(oneweekquantity >= 500){
			marketability = 9;
		} else if(oneweekquantity >= 200){
			marketability = 8;
		} else if(oneweekquantity >= 100){
			marketability = 7;
		} else if(oneweekquantity >= 80){
			marketability = 6;
		} else if(oneweekquantity >= 50){
			marketability = 5;
		} else if(oneweekquantity >= 20){
			marketability = 4;
		} else if(oneweekquantity >= 10){
			marketability = 3;
		} else if(oneweekquantity >= 5){
			marketability = 2;
		} else {
			marketability = 1;
		}
		return marketability;
		
	}
	
	public int calculateProfitability(List<BigDecimal> monthofprices){
		int pricequantity = monthofprices.size();
		int profitability;
		boolean uncertainty = false;
		
		if(pricequantity == 0 || pricequantity == 1){
			profitability = 1;
			return profitability;
		}
		
		if(pricequantity < 10){
			uncertainty = true;
		}
		
		SummaryStatistics pricecollection = new SummaryStatistics();
		
		for (BigDecimal price : monthofprices){
			Double doublevalue = price.doubleValue();
			pricecollection.addValue(doublevalue);
		}
		
		Double pricestandarddeviation = pricecollection.getStandardDeviation();
		Double meanprice = pricecollection.getMean();
		
		Double deviationfraction = pricestandarddeviation/meanprice;
		
		if(deviationfraction >= 0.8){
			profitability = 10;
		} else if(deviationfraction >= 0.70){
			profitability = 9;
		} else if(deviationfraction >= 0.55){
			profitability = 8;
		} else if(deviationfraction >= 0.45){
			profitability = 7;
		} else if(deviationfraction >= 0.35){
			profitability = 6;
		} else if(deviationfraction >= 0.30){
			profitability = 5;
		} else if(deviationfraction >= 0.22){
			profitability = 4;
		} else if(deviationfraction >= 0.12){
			profitability = 3;
		} else if(deviationfraction >= 0.07){
			profitability = 2;
		} else {
			profitability = 1;
		}
		
		if(uncertainty){
			if(profitability > 1){
				profitability =- 1;
			}
		}
		
		return profitability;
	}

}
