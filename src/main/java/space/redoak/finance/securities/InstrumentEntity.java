package space.redoak.finance.securities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author glenn
 */
@Getter
@Setter
@Entity
@Table(name = "sm_quote_vw")
public class InstrumentEntity implements Serializable {

    private static final Long serialVersionUID = 1L;

    @Id
    private Integer instrumentId;
    private String symbol;
    private String descr;
    private LocalDate readDte;
    private Float closePrice;
    private Float strikePrice;
    private String comments;
    

    @Override
    public String toString() {
        return StringUtils.rightPad(symbol, 8) + " :  " + descr;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.instrumentId);
        hash = 29 * hash + Objects.hashCode(this.symbol);
        hash = 29 * hash + Objects.hashCode(this.descr);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InstrumentEntity other = (InstrumentEntity) obj;
        if (!Objects.equals(this.instrumentId, other.instrumentId)) {
            return false;
        }
        return true;
    }

            
}
