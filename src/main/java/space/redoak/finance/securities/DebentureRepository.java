package space.redoak.finance.securities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;


public interface DebentureRepository extends Repository<DebentureEntity, Long> {

    Page<DebentureEntity> findAll(Pageable pageRequest);
    
}
