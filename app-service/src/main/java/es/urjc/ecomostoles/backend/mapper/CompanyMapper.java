package es.urjc.ecomostoles.backend.mapper;

import es.urjc.ecomostoles.backend.dto.CompanyDTO;
import es.urjc.ecomostoles.backend.model.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "password", ignore = true)
    CompanyDTO toDto(Company entity);
    Company toEntity(CompanyDTO dto);
}
