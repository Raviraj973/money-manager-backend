package in.bushansirgur.moneymanager.dto;
import in.bushansirgur.moneymanager.dto.AIIntent;

public class AIIntent {

    private boolean needsUserData;
    private boolean needsMarketData;
    private boolean needsNewsData;

    public boolean isNeedsUserData() {
        return needsUserData;
    }

    public void setNeedsUserData(boolean needsUserData) {
        this.needsUserData = needsUserData;
    }

    public boolean isNeedsMarketData() {
        return needsMarketData;
    }

    public void setNeedsMarketData(boolean needsMarketData) {
        this.needsMarketData = needsMarketData;
    }

    public boolean isNeedsNewsData() {
        return needsNewsData;
    }

    public void setNeedsNewsData(boolean needsNewsData) {
        this.needsNewsData = needsNewsData;
    }
}