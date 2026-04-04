package in.bushansirgur.moneymanager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.*;
import java.io.*;

@Service
public class MarketService {

    @Value("${finnhub.api.key}")
    private String apiKey;

    private String cachedData;
    private long lastFetchTime = 0;

    public String getMarketNews() {
        try {
            long now = System.currentTimeMillis();

            if (cachedData != null && (now - lastFetchTime) < 600000) {
                return cachedData;
            }

            URL url = new URL("https://finnhub.io/api/v1/news?category=general&token=" + apiKey);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            cachedData = response.toString();
            lastFetchTime = now;

            return cachedData;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}