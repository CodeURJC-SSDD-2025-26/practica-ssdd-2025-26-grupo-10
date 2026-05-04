package es.urjc.ecomostoles.backend.mapper;

import es.urjc.ecomostoles.backend.dto.AgreementDTO;
import es.urjc.ecomostoles.backend.model.Agreement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CompanyMapper.class})
public interface AgreementMapper {

    @Mapping(source = "offer.id", target = "offerId")
    @Mapping(source = "demand.id", target = "demandId")
    AgreementDTO toDto(Agreement entity);

    @Mapping(source = "offerId", target = "offer.id")
    @Mapping(source = "demandId", target = "demand.id")
    Agreement toEntity(AgreementDTO dto);
}
