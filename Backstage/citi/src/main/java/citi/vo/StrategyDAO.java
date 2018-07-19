package citi.vo;

public class StrategyDAO {
    private String strategyID;
    private String MerchantID;
    private Double full;
    private Double priceAfter;
    private Double points;

    public StrategyDAO(String strategyID, String merchantID, Double full, Double priceAfter, Double points) {
        this.strategyID = strategyID;
        MerchantID = merchantID;
        this.full = full;
        this.priceAfter = priceAfter;
        this.points = points;
    }

    public String getStrategyID() {
        return strategyID;
    }

    public String getMerchantID() {
        return MerchantID;
    }

    public Double getFull() {
        return full;
    }

    public Double getPriceAfter() {
        return priceAfter;
    }

    public Double getPoints() {
        return points;
    }

}
