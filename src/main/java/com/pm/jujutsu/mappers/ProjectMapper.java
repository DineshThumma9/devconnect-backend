package com.pm.jujutsu.mappers;

import com.pm.jujutsu.dtos.ProjectRequestDTO;
import com.pm.jujutsu.dtos.ProjectResponseDTO;
import com.pm.jujutsu.model.Project;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(source = "id", target = "id", qualifiedByName = "objectIdToString")
    @Mapping(target = "currentContributors", ignore = true)
    @Mapping(target = "ownerUsername", ignore = true)
    @Mapping(target = "ownerProfilePicUrl", ignore = true)
    @Mapping(target = "media", ignore = true)
    @Mapping(source = "ownerId", target = "ownerId", qualifiedByName = "objectIdToString")
    ProjectResponseDTO toResponseEntity(Project project);

    @Mapping(source = "ownerId", target = "ownerId", qualifiedByName = "stringToObjectId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentContributorIds", ignore = true)
    @Mapping(target = "pastContributorIds", ignore = true)
    @Mapping(target = "status", constant = "active")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "media", ignore = true)
    Project toEntity(ProjectRequestDTO project);

    @Named("objectIdToString")
    default String objectIdToString(ObjectId objectId) {
        return objectId != null ? objectId.toHexString() : null;
    }

    @Named("stringToObjectId")
    default ObjectId stringToObjectId(String id) {
        return id != null && !id.isEmpty() ? new ObjectId(id) : null;
    }

    @Named("objectIdListToStringList")
    default List<String> objectIdListToStringList(List<ObjectId> objectIds) {
        return objectIds != null ?
                objectIds.stream()
                        .map(id -> id != null ? id.toHexString() : null)
                        .collect(Collectors.toList()) :
                null;
    }
}