package es.urjc.ecomostoles.backend.mapper;

import es.urjc.ecomostoles.backend.dto.CompanyDTO;
import es.urjc.ecomostoles.backend.model.Company;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    CompanyDTO toDto(Company entity);
    Company toEntity(CompanyDTO dto);
}
