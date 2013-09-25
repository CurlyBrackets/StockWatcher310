package com.google.gwt.sample.stockwatcher.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class StockWatcher extends ArrayList implements EntryPoint{
	private int stuff;
	private String otherstuff;
	private TextBox morestuff;
	
	
	private TreeMap<String, StockInfo> stocks;
	private static final int REFRESH_INTERVAL = 5000;
	
	private final StockServiceAsync stockService = GWT.create(StockService.class);
	
	public StockWatcher(){
		stuff = 0xff543;
		otherstuff = "hope this actually makes the merge conflict";
		morestuff = new TextBox();
		
		stocks = new TreeMap<String, StockInfo>();
	}

	private LoginInfo loginInfo = null;
	private VerticalPanel vpLogin = new VerticalPanel();
	private Label lblLogin = new Label("Please sign into your google account to access the StockWatcher application.");
	private Anchor aSignin = new Anchor("Sign in"),
				   aSignout= new Anchor("Sign out");
	
	@Override
	public void onModuleLoad() {
		LoginServiceAsync loginService = GWT.create(LoginService.class);
		loginService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>(){
			public void onFailure(Throwable error){
				handleError(error);
			}
			
			public void onSuccess(LoginInfo result){
				loginInfo = result;
				if(loginInfo.isLoggedIn())
					loadStockWatcher();
				else
					loadLogin();
			}
		});
		//loadStockWatcher();
	}
	
	private void loadLogin(){
		aSignin.setHref(loginInfo.getLoginUrl());
		vpLogin.add(lblLogin);
		vpLogin.add(aSignin);
		RootPanel.get("stocklist").add(vpLogin);
	}

	private void loadStockWatcher() {
		aSignout.setHref(loginInfo.getLogoutUrl());
		
		tbStocks.setText(0, 0, "Symbol");
		tbStocks.setText(0, 1, "Price");
		tbStocks.setText(0, 2, "Change");
		tbStocks.setText(0, 3, "Remove");
		
		tbStocks.getRowFormatter().addStyleName(0, "watchListHeader");
		tbStocks.addStyleName("watchList");
		tbStocks.getCellFormatter().addStyleName(0, 1, "watchListNumericColumn");
	    tbStocks.getCellFormatter().addStyleName(0, 2, "watchListNumericColumn");
	    tbStocks.getCellFormatter().addStyleName(0, 3, "watchListRemoveColumn");
	    tbStocks.setCellPadding(6);
		
		hpAdd.add(teSymbol);
		hpAdd.add(butAdd);
		hpAdd.addStyleName("addPanel");
		
		vpMain.add(aSignout);
		vpMain.add(tbStocks);
		vpMain.add(hpAdd);
		vpMain.add(lblLastUpdate);
		vpMain.add(aSignout);
		
		RootPanel.get("stocklist").add(vpMain);
		
		teSymbol.setFocus(true);
		
		butAdd.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				AddStock();
			}
		});
		
		teSymbol.addKeyUpHandler(new KeyUpHandler(){

			@Override
			public void onKeyUp(KeyUpEvent evnt) {
				if(evnt.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					AddStock();
			}
			
		});
		
		Timer timer = new Timer(){
			@Override
			public void run() {
				for(Entry<String,StockInfo> kvp:stocks.entrySet())
					kvp.getValue().FetchData();
				lblLastUpdate.setText("Last update : "  + DateTimeFormat.getMediumDateTimeFormat().format(new Date()));
			}
		};
		timer.scheduleRepeating(REFRESH_INTERVAL);
		
		loadStocks();
	}
	
	private void AddStock(){
		final String symbol = teSymbol.getText().trim().toUpperCase();
		if(!symbol.matches("^([\\w\\.]{1,10})$")){
			Window.alert("'"+symbol+"' is not a valid symbol!");
			teSymbol.selectAll();
			return;
		}
		
		teSymbol.setText("");
		
		AddStockAsync(symbol);
	}
	
	private void AddStockAsync(final String symbol){
	    stockService.addStock(symbol, new AsyncCallback<Void>() {
	        public void onFailure(Throwable error) {
	        	handleError(error);
	        }
	        public void onSuccess(Void ignore) {
	          DisplayStock(symbol);
	        }
	      });
	}
	
	//Add stock to table
	private void DisplayStock(final String symbol){
		//final String symbol = 

		
		if(stocks.containsKey(symbol)){
			Window.alert("Already added key");
			return;
		}
			
		//verify tag
		
		//on verify return add stock
		stocks.put(symbol, new StockInfo(symbol));
		
		int row = tbStocks.getRowCount();
		tbStocks.setText(row, 0, symbol);
		stocks.get(symbol).SetRow(row);
		
		Label lblPrice = new Label(),
			  lblChange= new Label();
		tbStocks.setWidget(row, 1, lblPrice);
		tbStocks.setWidget(row, 2, lblChange);
		stocks.get(symbol).SetLabels(lblPrice, lblChange);
		
		Button b = new Button("X");
		b.addStyleDependentName("remove");
		b.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent evnt) {
				RemoveStock(symbol);
			}
		});
		tbStocks.setWidget(row, 3, b);
		
		stocks.get(symbol).FetchData();
	}
	
	private void RemoveStock(final String symbol){
	    stockService.removeStock(symbol, new AsyncCallback<Void>() {
		    public void onFailure(Throwable error) {
		    	handleError(error);		    	
		    }
		    public void onSuccess(Void ignore) {
		    	UndisplayStock(symbol);
		    }
	    });
	}
	
	private void UndisplayStock(String symbol){
		int row = stocks.get(symbol).GetRow();
		tbStocks.removeRow(row);
		
		for(Entry<String, StockInfo> kvp : stocks.entrySet()){
			if(kvp.getValue().GetRow() >= row)
				kvp.getValue().SetRow(kvp.getValue().GetRow()-1);
		}
		
		stocks.remove(symbol);
	}
	
	
	private void loadStocks(){
		stockService.getStocks(new AsyncCallback<String[]>(){  
			public void onFailure(Throwable error) {
				handleError(error);
			}  
			public void onSuccess(String[] symbols) {  
				 for(String sym:symbols)
					 DisplayStock(sym);
			}  
		});
	}
	
	private void handleError(Throwable error) {
	    Window.alert(error.getMessage());
	    if (error instanceof NotLoggedInException) {
	      Window.Location.replace(loginInfo.getLogoutUrl());
	    }
	  }
}
