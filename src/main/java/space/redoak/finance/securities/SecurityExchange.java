package space.redoak.finance.securities;

/**
 *
 * @author glenn
 */
public enum SecurityExchange {

    TSX("Toronto Stock Exchange");

    private final String exchangeName;

    private SecurityExchange(String name) {
        this.exchangeName = name;
    }

    public String getExchangeName() {
        return exchangeName;
    }

}
