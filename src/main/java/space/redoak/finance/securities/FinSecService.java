package space.redoak.finance.securities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 *
 * @author glenn
 */
@Service
public class FinSecService {

    @Autowired
    private DebentureRepository debentureRepo;

    
    public Page<DebentureEntity> getDebentures(Pageable pageRequest) {
        return debentureRepo.findAll(pageRequest);
    }

}
