package com.ev.pcs.config;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.vertx.ext.consul.ServiceOptions;
import io.vertx.mutiny.ext.consul.ConsulClient;
import io.vertx.ext.consul.ConsulClientOptions;
import io.vertx.mutiny.core.Vertx;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

@ApplicationScoped
public class Registration {

    @ConfigProperty(name = "consul.host") String host;
    @ConfigProperty(name = "consul.port") int port;

    @ConfigProperty(name = "quarkus.application.name", defaultValue = "reservation-service") String service_name;
    @ConfigProperty(name = "quarkus.http.port", defaultValue = "9090") int service_port;

    @ConfigProperty(name = "HOSTNAME", defaultValue = "localhost") String containerId;

    /**
     * Register our service in Consul.
     *
     * Note: this method is called on a worker thread, and so it is allowed to block.
     */

    private ConsulClient client;
    private String serviceId;

    public void init(@Observes StartupEvent ev, Vertx vertx) {

        // Get the local IP address dynamically
        String address = containerId;

//        // Get the local IP address dynamically
//        String address;
//        try {
//            address = InetAddress.getLocalHost().getHostAddress();
//        } catch (UnknownHostException e) {
//            throw new RuntimeException("Failed to get the local IP address", e);
//        }

//        if(Objects.equals(address, "fedora")){
//            address="localhost";
//        }


        client = ConsulClient.create(vertx, new ConsulClientOptions().setHost(host).setPort(port));

        serviceId = "reservation-service-" + containerId + "-" + service_port;

        System.out.println("registreeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeed: "+serviceId);

        client.registerServiceAndAwait(
                new ServiceOptions().setPort(service_port).setAddress(address).setName(service_name).setId(serviceId));
    }

    public void onShutdown(@Observes ShutdownEvent ev) {
        if (client != null && serviceId != null) {
            client.deregisterServiceAndAwait(serviceId);
        }
    }
}