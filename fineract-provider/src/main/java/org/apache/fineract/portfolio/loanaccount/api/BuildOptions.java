package org.apache.fineract.portfolio.loanaccount.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class BuildOptions
{
  public String checkReceipt(String receipt)
    throws MalformedURLException, IOException
  {
    String postUrl = "http://localhost/stkpush/api/receipt.php";
    
    URL url = new URL(postUrl);
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();

    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json");
    
    String input = "{\"receiptNumber\":\"" + receipt.replaceAll("\\W", "") + "\"}";
    System.out.println("Receipt Input");
    System.out.println(input);
    
    OutputStream os = conn.getOutputStream();
    os.write(input.getBytes());
    os.flush();
    
    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    
    String output = br.readLine();
    
    return output;
  }
}

/* Location:
 * Qualified Name:     org.apache.fineract.portfolio.loanaccount.api.BuildOptions
 * Java Class Version: 8 (52.0)
 * JD-Core Version:    0.7.1
 */