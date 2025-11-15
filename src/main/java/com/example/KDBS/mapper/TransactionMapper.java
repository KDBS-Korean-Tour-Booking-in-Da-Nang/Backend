package com.example.KDBS.mapper;

import com.example.KDBS.dto.response.TransactionResponse;
import com.example.KDBS.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TransactionMapper {
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "avatar", source = "user.avatar")
    TransactionResponse toTransactionResponse(Transaction transaction);
}
