package com.example.rideshare_rest.graphql.fetcher;

import com.example.rideshare_api_contract.dto.PagedResponse;
import com.example.rideshare_api_contract.dto.UserRequest;
import com.example.rideshare_api_contract.dto.UserResponse;
import com.example.rideshare_rest.graphql.types.CreateUserInputGql;
import com.example.rideshare_rest.graphql.types.PageInfoGql;
import com.example.rideshare_rest.graphql.types.UpdateUserInputGql;
import com.example.rideshare_rest.graphql.types.UserConnectionGql;
import com.example.rideshare_rest.service.UserService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * DataFetcher для операций с пользователями.
 * <p>
 * Обрабатывает корневые поля Query и Mutation, связанные с пользователями.
 * Вложенные поля (User.rides) обрабатываются в UserRidesDataFetcher.
 * <p>
 * Принцип разделения: один DataFetcher — одна группа связанных операций.
 * Это делает код более читаемым и тестируемым.
 */
@DgsComponent
public class UserDataFetcher {
    private final UserService userService;

    @Autowired
    public UserDataFetcher(UserService userService) {
        this.userService = userService;
    }

    /**
     * Получение пользователя по идентификатору.
     * Соответствует полю Query.user(id: ID!) в схеме.
     */
    @DgsQuery
    public UserResponse user(@InputArgument String id) {
        return userService.getUserById(Long.parseLong(id));
    }

    /**
     * Список пользователей с пагинацией.
     * Соответствует полю Query.users(page, size) в схеме.
     */
    @DgsQuery
    public UserConnectionGql users(
            @InputArgument Integer page,
            @InputArgument Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        PagedResponse<UserResponse> paged = userService.getAllUsers(pageNum, pageSize);
        return new UserConnectionGql(
                paged.content(),
                new PageInfoGql(paged.pageNumber(), paged.pageSize(), paged.totalPages(), paged.last()),
                (int) paged.totalElements()
        );
    }

    /**
     * Создание пользователя.
     * Соответствует полю Mutation.createUser(input) в схеме.
     */

    @DgsMutation
    public UserResponse createUser(@InputArgument CreateUserInputGql input) {
        UserRequest request = new UserRequest(
                input.firstName(),
                input.lastName(),
                input.email(),
                input.birthDate()
        );
        return userService.create(request);
    }

    /**
     * Обновление пользователя.
     * Соответствует полю Mutation.updateUser(id, input) в схеме.
     */
    @DgsMutation
    public UserResponse updateUser(@InputArgument String id, @InputArgument UpdateUserInputGql input) {
        UserRequest request = new UserRequest(
                input.firstName(),
                input.lastName(),
                input.email(),
                input.birthDate()
        );
        return userService.updateUser(Long.parseLong(id), request);
    }

    /**
     * Удаление пользователя.
     * Соответствует полю Mutation.deleteUser(id) в схеме.
     */
    @DgsMutation
    public boolean deleteUser(@InputArgument String id) {
        userService.delete(Long.parseLong(id));
        return true;
    }
}
