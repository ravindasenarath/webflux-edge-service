package com.example.vehicleapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class VehicleApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(VehicleApiApplication.class, args);
	}

	@Bean
	WebClient webClient(WebClient.Builder builder){
		return builder.build();
	}

	@Bean
	RouterFunction<ServerResponse> vehicles(VehicleService service){

		return route()
				.GET("/vehicles/names", new HandlerFunction<ServerResponse>() {
					@Override
					public Mono<ServerResponse> handle(ServerRequest request) {
						var names = service.getVehicles()
								.map(Vehicle::model);
						return ServerResponse.ok().body(names, String.class);
					}
				})
				.build();
	}

}

@Service
record VehicleService(WebClient client) {

	public Flux<Vehicle> getVehicles(){
		return client
				.get()
				.uri("http://localhost:8080/vehicles/")
				.retrieve()
				.bodyToFlux(Vehicle.class)
				.retryWhen(Retry.backoff(3, Duration.ofSeconds(2)));
	}

}

record Vehicle(Integer id, String model){};
