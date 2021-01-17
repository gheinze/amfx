package space.redoak.finance.securities;

import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author glenn
 */
@Getter @Setter
@Entity
@Table(name = "sm_debenture_vw")
public class DebentureEntity implements Serializable {

    private static final Long serialVersionUID = 1L;

    @Id
    private Integer instrumentId;
    private String symbol;
    private String descr;
    private LocalDate issueDte;
    private LocalDate maturityDte;
    private Float percentage;
    private Float effectiveRate;
    private Float closePrice;
    private Integer volumeTraded;
    private LocalDate readDte;
    private Integer underlyingInstrumentId;
    private String underlyingSymbol;
    private Float underlyingClosePrice;
    private LocalDate underlyingReadDte;
    private Float conversionPrice;
    private Float conversionRate;
    private Float converted;
    private String prospectus;
    private String comments;

}