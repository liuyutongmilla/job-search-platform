package com.jobsearch.userservice.mapper;

import com.jobsearch.userservice.dto.ResumeDto;
import com.jobsearch.userservice.dto.UserDto;
import com.jobsearch.userservice.entity.AppUser;
import com.jobsearch.userservice.entity.ParseStatus;
import com.jobsearch.userservice.entity.Resume;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDto toDto(AppUser user) {
        return new UserDto(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getCity(),
                user.getExpectedSalary());
    }

    public static AppUser toNewEntity(UserDto dto) {
        AppUser user = new AppUser();
        applyEditableFields(dto, user);
        return user;
    }

    public static void applyEditableFields(UserDto dto, AppUser user) {
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPhone(dto.phone());
        user.setCity(dto.city());
        user.setExpectedSalary(dto.expectedSalary());
    }

    
    public static ResumeDto toDto(Resume resume) {
        boolean done = resume.getParseStatus() == ParseStatus.DONE;
        return new ResumeDto(
                resume.getResumeId(),
                resume.getUserId(),
                resume.getOriginalFilename(),
                resume.getParseStatus().name(),
                resume.getVersion(),
                done ? resume.getParsedJson() : null,
                resume.getParseError(),
                resume.getUploadedAt());
    }
}
