package com.paymybuddy.web.mapper;

import com.paymybuddy.domain.entity.Transaction;
import com.paymybuddy.web.dto.TransactionViewDto;
import org.mapstruct.Mapper;

/**
 * MapStruct automatically generates the implementation at compile time.
 * componentModel="spring" => mapper injectable via Spring (@Autowired / constructor injection).
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    // Here, the fields have the same names => MapStruct maps them automatically.
    TransactionViewDto toViewDto(Transaction transaction);
}
