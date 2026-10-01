//package com.example.rideshare_rest.glue;
//
//import com.example.rideshare_api_contract.dto.*;
//import com.example.rideshare_rest.service.BookingService;
//import com.example.rideshare_rest.service.RideService;
//import com.example.rideshare_rest.service.UserService;
//import io.cucumber.java.en.And;
//import io.cucumber.java.en.Given;
//import io.cucumber.java.en.Then;
//import io.cucumber.java.en.When;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//public class BookingCreatingGlue {
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private RideService rideService;
//
//    @Autowired
//    private BookingService bookingService;
//
//    private UserResponse driver;
//    private UserResponse passenger;
//    private RideResponse ride;
//    private BookingResponse createdBooking;
//
//    @Given("Создан пользователь водитель и пользователь пассажир и создана поездка для бронирования")
//    public void createDriverAndPassengerAndRide() {
//        UserRequest driverRequest = new UserRequest("Petr", "Petrov", "driver@test.com",
//                LocalDate.of(2000, 1, 14));
//        driver = userService.create(driverRequest);
//
//        UserRequest passengerRequest = new UserRequest(
//                "Ivan", "Ivanov", "passenger@test.com",
//                LocalDate.of(2005, 1, 14));
//        passenger = userService.create(passengerRequest);
//
//        RideRequest rideRequest = new RideRequest(driver.getId(), "Moscow", "Ivanovo",
//                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(8), 4, 4,
//                RideStatus.ACTIVE, 1500);
//        ride = rideService.create(rideRequest);
//    }
//
//    @When("Пользователь нажимает кнопку \"Забронировать\" на нужной поездке")
//    public void clickOnBooking() {
//        BookingRequest request = new BookingRequest(
//                ride.getId(),
//                passenger.getId(),
//                BookingStatus.PENDING,
//                1
//        );
//        createdBooking = bookingService.createBooking(request);
//    }
//
//    @Then("Возвращается статус код = {string}")
//    public void checkStatusCode(String statusCode) {
//        assertNotNull(createdBooking, "Бронирование должно быть успешно создано");
//        assertEquals("201", statusCode);
//    }
//    /*
//    // окружение
//    // начальное состояние
//    // id тест кейса
//    // название тест кейса
//    // описание тест кейса
//    // шаги выполнения тест кейса
//    // ожидаемый результат
//    // фактический результат
//    // статус
//    */
//
//    /*
//    Чек лист
//    в иконке корзины число добавленных товаров
//
//     */
//
//    @And("Возвращается id = {string}")
//    public void checkBookingId(String expectedBookingId) {
//        assertNotNull(createdBooking.getId(), "У созданного бронирования должен быть сгенерирован ID");
//        if (!expectedBookingId.contains("<") && !expectedBookingId.isBlank()) {
//            assertEquals(Long.parseLong(expectedBookingId), createdBooking.getId());
//        }
//    }
//}