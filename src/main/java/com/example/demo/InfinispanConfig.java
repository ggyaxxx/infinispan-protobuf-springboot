package com.example.demo;


import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.configuration.StringConfiguration;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfinispanConfig {

    @Value("${infinispan.host}")
    private String host;

    @Value("${infinispan.port}")
    private int port;

    @Value("${infinispan.username}")
    private String username;

    @Value("${infinispan.password}")
    private String password;

    @Value("${infinispan.use-auth}")
    private boolean useAuth;



    @Bean
    public RemoteCacheManager remoteCacheManager() {

        String xml = """
        <distributed-cache name="books" mode="SYNC" statistics="true">
            <encoding>
                <key media-type="text/plain"/>
                <value media-type="application/x-protostream"/>
            </encoding>
        </distributed-cache>
        """;

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder
                .addServer()
                .host("127.0.0.1")
                .port(11222)
                .security()
                .authentication()
                .username("admin")
                .password("admin")
                .realm("default")
                .saslMechanism("SCRAM-SHA-256");

        builder.addContextInitializer(new BookStoreSchemaImpl());

        RemoteCacheManager manager = new RemoteCacheManager(builder.build());

        // cache interna preinstallata in Data Grid per registrare i file .proot/definizioneschema/errori compilazione (.errors)
        RemoteCache<String, String> meta = manager.getCache("___protobuf_metadata");

        //crea la cache se non esiste utilizzando la definizione xml della cache
        manager.administration().getOrCreateCache("books", new StringConfiguration(xml));

        // crea un'istanza dello schema
        BookStoreSchema schema = new BookStoreSchemaImpl();

        // registra il file .proto nel server
        meta.put(schema.getProtoFileName(), schema.getProtoFile());

        String errors = meta.get(".errors");
        if (errors != null) {
            throw new IllegalStateException("Schema contains errors:\n" + errors);
        }

        return manager;
    }

}
