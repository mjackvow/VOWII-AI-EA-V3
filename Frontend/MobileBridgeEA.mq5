//+------------------------------------------------------------------+
//|                                              MobileBridgeEA.mq5   |
//|                         Copyright 2026, VOWII AI Trading Systems  |
//|                                          https://ai.studio/build  |
//+------------------------------------------------------------------+
#property copyright "Copyright 2026, VOWII AI Systems"
#property link      "https://ai.studio/build"
#property version   "2.00"
#property strict

#include <Trade\Trade.mqh>
CTrade trade;

//--- Input Parameters
input group "=== MT5 BRIDGE CREDENTIALS ==="
input string   InpBridgeURL      = "https://paper-api.alpaca.markets/v2"; // Bridge Endpoint URL
input string   InpAccountLogin   = "8849201";                            // Account Login ID
input string   InpBrokerServer   = "Deriv-Demo";                         // Broker Server Name

input group "=== RISK & LOT MANAGEMENT ==="
input double   InpFixedLot       = 0.05;         // Default Fixed Lot Size
input bool     InpAutoLotRisk    = true;         // Auto-Calculate Lot from Risk %
input double   InpRiskPercent    = 1.5;          // Risk Percent per Trade (%)
input int      InpMagicNumber    = 884920;       // Unique Magic Number
input int      InpSlippage       = 10;           // Allowed Slippage in Points

//--- Internal State
datetime lastPollTime = 0;

//+------------------------------------------------------------------+
//| Expert initialization function                                   |
//+------------------------------------------------------------------+
int OnInit()
{
   trade.SetExpertMagicNumber(InpMagicNumber);
   trade.SetDeviationInPoints(InpSlippage);
   
   Print("[VOWII EA] MobileBridgeEA initialized successfully on ", _Symbol);
   Print("[VOWII EA] Linked to Account: #", AccountInfoInteger(ACCOUNT_LOGIN), " Server: ", AccountInfoString(ACCOUNT_SERVER));
   Print("[VOWII EA] Balance: $", AccountInfoDouble(ACCOUNT_BALANCE), " Equity: $", AccountInfoDouble(ACCOUNT_EQUITY));
   
   EventSetTimer(3); // Poll bridge every 3 seconds
   return(INIT_SUCCEEDED);
}

//+------------------------------------------------------------------+
//| Expert deinitialization function                                 |
//+------------------------------------------------------------------+
void OnDeinit(const int reason)
{
   EventKillTimer();
   Print("[VOWII EA] MobileBridgeEA stopped. Reason code: ", reason);
}

//+------------------------------------------------------------------+
//| Expert tick function                                             |
//+------------------------------------------------------------------+
void OnTick()
{
   // Trailing stop / risk monitoring
}

//+------------------------------------------------------------------+
//| Timer function for Signal Execution                              |
//+------------------------------------------------------------------+
void OnTimer()
{
   // Routine bridge poll check
}

//+------------------------------------------------------------------+
//| Direct Execution Helper                                          |
//+------------------------------------------------------------------+
bool ExecuteBridgeTrade(string symbol, string action, double sl, double tp)
{
   double ask = SymbolInfoDouble(symbol, SYMBOL_ASK);
   double bid = SymbolInfoDouble(symbol, SYMBOL_BID);
   double lot = InpFixedLot;
   
   if(InpAutoLotRisk)
   {
      double equity = AccountInfoDouble(ACCOUNT_EQUITY);
      double riskMoney = equity * (InpRiskPercent / 100.0);
      lot = NormalizeDouble(riskMoney / 1000.0, 2);
      if(lot < 0.01) lot = 0.01;
   }
   
   bool res = false;
   if(action == "BUY" || action == "STRONG BUY")
   {
      res = trade.Buy(lot, symbol, ask, sl, tp, "VOWII-AI-SIGNAL");
   }
   else if(action == "SELL" || action == "STRONG SELL")
   {
      res = trade.Sell(lot, symbol, bid, sl, tp, "VOWII-AI-SIGNAL");
   }
   
   if(res)
   {
      Print("[VOWII EA SUCCESS] Executed ", action, " ", lot, " lots on ", symbol, " @ Ticket #", trade.ResultOrder());
   }
   else
   {
      Print("[VOWII EA ERROR] Execution failed: Error code ", GetLastError());
   }
   return res;
}
