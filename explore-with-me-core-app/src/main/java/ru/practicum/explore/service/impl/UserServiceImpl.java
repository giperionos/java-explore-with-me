package ru.practicum.explore.service.impl;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.dto.NewUserRequest;
import ru.practicum.explore.dto.UserDto;
import ru.practicum.explore.dto.UserFilter;
import ru.practicum.explore.model.QUser;
import ru.practicum.explore.model.User;
import ru.practicum.explore.repository.UserRepository;
import ru.practicum.explore.service.UserService;
import ru.practicum.explore.service.exceptions.UserNotFoundException;
import ru.practicum.explore.service.mapper.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public UserDto addNewUser(NewUserRequest newUserRequest) {
        User userForSave = UserMapper.toUser(newUserRequest);
        return UserMapper.toUserDto(repository.save(userForSave));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByFilter(UserFilter filter) {

        int page = filter.getFrom() / filter.getSize();
        final PageRequest pageRequest = PageRequest.of(page, filter.getSize(), Sort.by("id").ascending());

        //если есть данные для поиска по id
        if (filter.getUserIds() != null && filter.getUserIds().size() > 0) {

            Predicate finalPredicate = QUser.user.id.in(filter.getUserIds());

            return repository.findAll(finalPredicate, pageRequest)
                    .stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        }

        //либо выбрать всех исходя из pageRequest
        return repository.findAll(pageRequest)
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUserById(Long userId) {
        User userForDelete = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        repository.delete(userForDelete);
    }
}
