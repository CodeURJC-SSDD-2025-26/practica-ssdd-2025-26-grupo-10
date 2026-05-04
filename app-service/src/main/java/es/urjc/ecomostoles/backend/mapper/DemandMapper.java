package es.urjc.ecomostoles.backend.mapper;

import es.urjc.ecomostoles.backend.dto.DemandDTO;
import es.urjc.ecomostoles.backend.model.Demand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CompanyMapper.class})
public interface DemandMapper {
    DemandDTO toDto(Demand entity);
    Demand toEntity(DemandDTO dto);
}
