package com.google.gwt.sample.stockwatcher.client;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Label;

public class StockInfo {
	public StockInfo(String symbol){
		this.symbol = symbol;
	}
	
	public StockInfo(String symbol, double price, double change){
		this.symbol = symbol;
		this.price = price;
		this.change = change;
	}
	
	private String symbol;
	public String GetSymbol(){return symbol;}
	public void SetSymbol(String newValue){symbol = newValue;}
	
	private double price;
	public double GetPrice(){return price;}
	public void SetPrice(double newPrice){price = newPrice;}
	
	private double change;
	public double GetChange(){return change;}
	public void SetChange(double newChange){change = newChange;}
	
	public double GetChangePercent(){
		return 100.0 * change / price;
	}
	
	private Label lblPrice, lblChange;
	public void SetLabels(Label newPrice, Label newChange){
		lblPrice = newPrice;
		lblChange = newChange;
	}
	
	public void FetchData(){
		//In reality do this async
		//NativeFetch(GetSymbol());(some od error?)
	    final double MAX_PRICE = 100.0; // $100.00
	    final double MAX_PRICE_CHANGE = 0.02; // +/- 2%

	    SetPrice(Random.nextDouble() * MAX_PRICE);
	    SetChange(price * MAX_PRICE_CHANGE * (Random.nextDouble() * 2.0 - 1.0));
	    
	    UpdateTable();
	}
	
	private void UpdateTable(){
    	String priceText = NumberFormat.getFormat("#,##0.00").format(GetPrice());
        NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
        String changeText = changeFormat.format(GetChange());
        String changePercentText = changeFormat.format(GetChangePercent());

        // Populate the Price and Change fields with new data.
        //tbl.setText(GetRow(), 1, priceText);
        //tbl.setText(GetRow(), 2, changeText + " (" + changePercentText + "%)");
        lblPrice.setText(priceText);
        lblChange.setText(changeText + " (" + changePercentText + "%)");
        
        String changeStyleName = "noChange";
        if (GetChangePercent() < -0.1f) {
          changeStyleName = "negativeChange";
        }
        else if (GetChangePercent() > 0.1f) {
          changeStyleName = "positiveChange";
        }

        lblChange.setStyleName(changeStyleName);
	}
	
	private native void NativeFetch(String s) /*-{
		var req = new XMLHttpRequest();
		req.addEventListener('readystatechange', function(){
			if(req.readyState == 4 && req.status == 200){
				var complex = JSON.parse(req.responseText);				
				var tag = this.@com.google.gwt.sample.stockwatcher.client.StockInfo::GetSymbol();
				if(tag == complex["t"]){
					this.@com.google.gwt.sample.stockwatcher.client.StockInfo::SetPrice(D)(parseFloat(complex["l"]));
					this.@com.google.gwt.sample.stockwatcher.client.StockInfo::SetChange(D)(parseFloat(complex["c"]));
					this.@com.google.gwt.sample.stockwatcher.client.StockInfo::UpdateTable();
				}
				else{
					$wnd.alert("Some error!");
				}				
			}
		}.bind(this));	
		req.open("GET", "http://finance.google.com/finance/info?client=ig&q=NASDAQ%3a"+s, true);
		req.send(null);	
	}-*/;
	
	private int row;
	public int GetRow(){return row;}
	public void SetRow(int newRow){row = newRow;}
	
}
