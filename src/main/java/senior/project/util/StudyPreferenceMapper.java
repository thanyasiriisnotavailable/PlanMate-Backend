package senior.project.util;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import senior.project.entity.StudyPreferenceDto;
import senior.project.entity.StudyPreference;
import senior.project.entity.User;

@Mapper(componentModel = "spring")
public interface StudyPreferenceMapper {

    @Mapping(target = "userUid", source = "user.uid")
    StudyPreference toEntity(StudyPreferenceDto dto, User user);

    @Mapping(source = "userUid", target = "userUid")
    StudyPreferenceDto toDto(StudyPreference entity);
}
