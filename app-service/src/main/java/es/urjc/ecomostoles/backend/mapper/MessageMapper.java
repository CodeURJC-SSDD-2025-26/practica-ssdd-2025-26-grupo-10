package es.urjc.ecomostoles.backend.mapper;

import es.urjc.ecomostoles.backend.dto.MessageDTO;
import es.urjc.ecomostoles.backend.model.Message;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CompanyMapper.class})
public interface MessageMapper {
    MessageDTO toDto(Message entity);
    Message toEntity(MessageDTO dto);
}
