package es.urjc.ecomostoles.backend.mapper;

import es.urjc.ecomostoles.backend.dto.OfferDTO;
import es.urjc.ecomostoles.backend.model.Offer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CompanyMapper.class})
public interface OfferMapper {
    OfferDTO toDto(Offer entity);
    Offer toEntity(OfferDTO dto);
}
