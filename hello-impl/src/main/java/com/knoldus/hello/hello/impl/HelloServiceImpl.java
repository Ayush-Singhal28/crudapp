package com.knoldus.hello.hello.impl;

import akka.Done;
import akka.NotUsed;
import akka.japi.Pair;
import akka.serialization.Serialization;
import com.knoldus.hello.hello.api.*;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.xml.soap.Detail;

import com.knoldus.hello.hello.impl.HelloCommand.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

/**
 * Implementation of the HelloService.
 */
public class HelloServiceImpl implements HelloService {

    private final PersistentEntityRegistry persistentEntityRegistry;
    ExternalService externalService;

    @Setter
    String username;

    @Getter
    @Setter
    Details userDetails;

    List<Details> detailsList = new ArrayList<>();

    @Inject
    public HelloServiceImpl(PersistentEntityRegistry persistentEntityRegistry, ExternalService externalService) {
        this.persistentEntityRegistry = persistentEntityRegistry;
        persistentEntityRegistry.register(HelloEntity.class);
        this.externalService = externalService;
    }

    @Override
    public ServiceCall<NotUsed, String> hello(String id) {
        return request -> {
            // Look up the hello world entity for the given ID.
            PersistentEntityRef<HelloCommand> ref = persistentEntityRegistry.refFor(HelloEntity.class, id);
            // Ask the entity the Hello command.
            return ref.ask(new Hello(id));
        };

    }

    @Override
    public ServiceCall<GreetingMessage, Done> useGreeting(String id) {
        return request -> {
            // Look up the hello world entity for the given ID.
            PersistentEntityRef<HelloCommand> ref = persistentEntityRegistry.refFor(HelloEntity.class, id);
            // Tell the entity to use the greeting message specified.
            return ref.ask(new UseGreetingMessage(request.message));
        };

    }


    @Override
    public ServiceCall<NotUsed, String> getTeamName(String teamName) {
        return request -> {
            return CompletableFuture.completedFuture(teamName);
        };
    }

    @Override
    public ServiceCall<Details, UserResult> userDetails() {
        return request -> {
            Details user1 = Details.builder().firstName("ayush").lastName("singhal").dateOfBirth("20/3/1994").build();

            detailsList.add(user1);
            Details user2 = Details.builder().firstName("vaibhav").lastName("arora").dateOfBirth("20/3/1994").build();
            detailsList.add(user2);
            username = request.getFirstName();
            return CompletableFuture.completedFuture(UserResult.builder()
                    .firstName(request.getFirstName())
                    .loyaltyNumber(new Random().nextInt(100))
                    .build());
        };
    }

    @Override
    public ServiceCall<NotUsed, String> getUserName() {
        return request -> {
            return CompletableFuture.completedFuture(username);
        };
    }

    @Override
    public ServiceCall<Details, UserResult> updateUserDetails() {
        return request -> {
            String name = "ayush";
            if (name.equals(request.getFirstName())) {
                username = "vaibhav";
                return CompletableFuture.completedFuture(UserResult.builder().firstName(username).loyaltyNumber(2).build());
            } else {
                return CompletableFuture.completedFuture(UserResult.builder().firstName(request.getFirstName()).loyaltyNumber(2).build());
            }
        };
    }

    @Override
    public ServiceCall<NotUsed, List<Details>> deleteUser() {
        return request -> {


            detailsList.remove(0);
            return CompletableFuture.completedFuture(detailsList);
        };
    }

    @Override
    public ServiceCall<NotUsed, List<Details>> listOfUser() {
        return request -> {
            return CompletableFuture.completedFuture(detailsList);
        };
    }

    @Override
    public ServiceCall<NotUsed, ExternalResponse> getExternalData() {
        return request ->
            externalService.getExposedUrl().invoke().thenApply(Function.identity());

    }


    @Override
    public Topic<com.knoldus.hello.hello.api.HelloEvent> helloEvents() {
        // We want to publish all the shards of the hello event
        return TopicProducer.taggedStreamWithOffset(HelloEvent.TAG.allTags(), (tag, offset) ->

                // Load the event stream for the passed in shard tag
                persistentEntityRegistry.eventStream(tag, offset).map(eventAndOffset -> {

                    // Now we want to convert from the persisted event to the published event.
                    // Although these two events are currently identical, in future they may
                    // change and need to evolve separately, by separating them now we save
                    // a lot of potential trouble in future.
                    com.knoldus.hello.hello.api.HelloEvent eventToPublish;

                    if (eventAndOffset.first() instanceof HelloEvent.GreetingMessageChanged) {
                        HelloEvent.GreetingMessageChanged messageChanged = (HelloEvent.GreetingMessageChanged) eventAndOffset.first();
                        eventToPublish = new com.knoldus.hello.hello.api.HelloEvent.GreetingMessageChanged(
                                messageChanged.getName(), messageChanged.getMessage()
                        );
                    } else {
                        throw new IllegalArgumentException("Unknown event: " + eventAndOffset.first());
                    }

                    // We return a pair of the translated event, and its offset, so that
                    // Lagom can track which offsets have been published.
                    return Pair.create(eventToPublish, eventAndOffset.second());
                })
        );
    }
}
