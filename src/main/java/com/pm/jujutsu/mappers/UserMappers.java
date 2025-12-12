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
     @Mapping(target = "id", ignore = true)
     @Mapping(target = "hashedPassword", ignore = true)
     @Mapping(target = "followerIds", ignore = true)
     @Mapping(target = "followingIds", ignore = true)
     @Mapping(target = "subscribedProjectIds", ignore = true)
     @Mapping(target = "provider", ignore = true)
     User toEntity(UserRequestDTO userRequestDTO);

     @Mapping(source = "id", target = "id", qualifiedByName = "objectIdToString")
     @Mapping(target = "followers", ignore = true)
     @Mapping(target = "followings", ignore = true)
     @Mapping(target = "subscribedProjects", ignore = true)
     UserResponseDTO toResponseEntity(User user);

     @Named("objectIdToString")
     default String objectIdToString(ObjectId objectId) {
          return objectId != null ? objectId.toHexString() : null;
     }
}