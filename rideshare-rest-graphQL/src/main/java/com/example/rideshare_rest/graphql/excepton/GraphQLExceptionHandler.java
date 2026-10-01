package com.example.rideshare_rest.graphql.excepton;

import com.example.rideshare_api_contract.exceptions.NoFreeSeatsException;
import com.example.rideshare_api_contract.exceptions.ResourceNotFoundException;
import com.netflix.graphql.types.errors.TypedGraphQLError;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Обработчик исключений для GraphQL DataFetcher'ов.
 * <p>
 * В REST API исключения преобразуются в HTTP-статусы (404, 409, 400).
 * В GraphQL нет HTTP-статусов для ошибок — все ответы приходят с HTTP 200,
 * а ошибки помещаются в массив "errors" в теле ответа.
 * <p>
 * Этот обработчик перехватывает доменные исключения и преобразует их
 * в типизированные GraphQL-ошибки с понятными сообщениями и классификацией.
 * <p>
 * DGS автоматически обнаруживает реализацию DataFetcherExceptionHandler
 * как Spring-компонент и подставляет её вместо обработчика по умолчанию.
 */
@Component
public class GraphQLExceptionHandler implements DataFetcherExceptionHandler {

    @Override
    public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(
            DataFetcherExceptionHandlerParameters handlerParameters) {

        Throwable exception = handlerParameters.getException();

        if (exception instanceof NoFreeSeatsException) {
            var error = TypedGraphQLError.newBadRequestBuilder()
                    .message(exception.getMessage())
                    .path(handlerParameters.getPath())
                    .build();

            return CompletableFuture.completedFuture(
                    DataFetcherExceptionHandlerResult.newResult()
                            .error(error)
                            .build());
        }

        if (exception instanceof ResourceNotFoundException) {
            var error = TypedGraphQLError.newConflictBuilder()
                    .message(exception.getMessage())
                    .path(handlerParameters.getPath())
                    .build();

            return CompletableFuture.completedFuture(
                    DataFetcherExceptionHandlerResult.newResult()
                            .error(error)
                            .build());
        }

        var error = TypedGraphQLError.newInternalErrorBuilder()
                .message("Внутренняя ошибка сервера")
                .path(handlerParameters.getPath())
                .build();

        return CompletableFuture.completedFuture(
                DataFetcherExceptionHandlerResult.newResult()
                        .error(error)
                        .build());
    }
}
