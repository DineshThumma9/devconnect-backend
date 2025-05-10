package com.pm.jujutsu.mappers;

import com.pm.jujutsu.dtos.PostRequestDTO;
import com.pm.jujutsu.dtos.PostResponseDTO;
import com.pm.jujutsu.model.Post;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(source = "id", target = "id", qualifiedByName = "objectIdToString")
    @Mapping(target = "ownerUsername", ignore = true)
    @Mapping(target = "ownerProfilePicUrl", ignore = true)
    PostResponseDTO toResponseEntity(Post post);

    Post toEntity(PostRequestDTO post);

    @Named("objectIdToString")
    default String objectIdToString(ObjectId objectId) {
        return objectId != null ? objectId.toHexString() : null;
    }
}