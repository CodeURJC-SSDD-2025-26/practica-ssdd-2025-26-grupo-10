package es.urjc.ecomostoles.backend.mapper;

import es.urjc.ecomostoles.backend.dto.OfferDTO;
import es.urjc.ecomostoles.backend.model.Offer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CompanyMapper.class})
public interface OfferMapper {
    @org.mapstruct.Mapping(target = "owned", ignore = true)
    OfferDTO toDto(Offer entity);

    @org.mapstruct.Mapping(target = "image", ignore = true)
    Offer toEntity(OfferDTO dto);
}
