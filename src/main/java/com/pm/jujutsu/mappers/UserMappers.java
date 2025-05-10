package com.pm.jujutsu.mappers;


import com.pm.jujutsu.dtos.UserRequestDTO;
import com.pm.jujutsu.dtos.UserResponseDTO;
import com.pm.jujutsu.model.User;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


@Mapper(componentModel = "spring")
public interface UserMappers {
     User toEntity(UserRequestDTO userRequestDTO);

     @Mapping(source = "id", target = "id", qualifiedByName = "objectIdToString")
     UserResponseDTO toResponseEntity(User user);

     @Named("objectIdToString")
     default String objectIdToString(ObjectId objectId) {
          return objectId != null ? objectId.toHexString() : null;
     }
}