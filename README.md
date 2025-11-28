# Spring Boot + Red Hat Data Grid / Infinispan
## Creazione programmatica di una cache con Protobuf (ProtoStream)

Questo progetto è ready-to-use:  una volta modificato il puntamento all'istanza Data Grid
nel file properties `application.properties`

```properties
infinispan.host=127.0.0.1
infinispan.port=11222

infinispan.username=admin
infinispan.password=admin

infinispan.use-auth=true
```

all'avvio:

* registrerà automaticamente lo schema `.proto` sul server
* inserirà una cache al secondo con un timer interno (non sarà necessario effettuare alcuna curl e tutto avverrà automaticamente)


Questo progetto mostra come:

1. Definire modelli Java annotati con **ProtoStream** (`Book`, `Author`, `Language`)
2. Generare automaticamente lo **schema Protobuf**
3. Registrare lo schema nel server Data Grid (`___protobuf_metadata`)
4. **Creare una cache** programmaticamente con configurazione custom (XML/JSON)
5. Usare la cache da un client Spring Boot

È pensato come riferimento per colleghi che devono creare nuove cache lato client.

---

#  1. Prerequisiti

- Red Hat Data Grid 8.5 / Infinispan 15 con Hot Rod attivo (porta 11222)
- Un utente con permessi admin (default: `admin / admin`)
- Java 21+
- Maven 3.x

Configurazione nel file `application.properties`:

```properties
infinispan.host=127.0.0.1
infinispan.port=11222
infinispan.username=admin
infinispan.password=admin
infinispan.use-auth=true
```

Le dipendenze principali sono in pom.xml e usano il BOM Data Grid:

```properties
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.infinispan</groupId>
      <artifactId>infinispan-bom</artifactId>
      <version>15.0.19.Final-redhat-00001</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```
---

#  2. Struttura del progetto

| File                                        | Descrizione                                               |
| ------------------------------------------- | --------------------------------------------------------- |
| `DemoApplication.java`                      | Entry-point Spring Boot                                   |
| `InfinispanConfig.java`                     | Configura RemoteCacheManager, registra schema, crea cache |
| `Book.java`, `Author.java`, `Language.java` | Modelli annotati ProtoStream                              |
| `BookStoreSchema.java`                      | schema Protobuf aggregato                                 |
| `BookStoreSchemaImpl.java`                  | generato automaticamente                                  |
| `BooksCacheProducer.java`                   | esempio di uso della cache `books`                        |

---
#  3. Modelli Protostream
Questa è l'interfaccia che effettivamente genererà lo schema. Le classi associate (Book,Author,Language) devono 
essere annotate come nel codice allegato affinché i campi vengano riconosciuti ed inclusi.

Esempio schema:
```java


@ProtoSchema(
includeClasses = {
Book.class,
Author.class,
Language.class
},
schemaFileName = "books.proto",
schemaFilePath = "proto/",
schemaPackageName = "books"
)
public interface BookStoreSchema extends GeneratedSchema {}
```

Le classi Java includono:

```java
@ProtoField(number = ...)

@ProtoFactory

@ProtoEnumValue (per enum)
```
---
#  4. Registrazione dello schema Protobuf

La classe InfinispanConfig contiene il blocco di codice necessario alla registrazione dello schema 
da parte di HotRod. 
Questo blocco:

* crea il file .proto lato server
* abilita la serializzazione/deserializzazione dei tipi Java

```java
RemoteCache<String, String> meta = manager.getCache("___protobuf_metadata");

BookStoreSchema schema = new BookStoreSchemaImpl();

meta.put(schema.getProtoFileName(), schema.getProtoFile());

String errors = meta.get(".errors");
if (errors != null) {
    throw new IllegalStateException("Schema contains errors:\n" + errors);
}
```

La cache può quindi essere creata programmaticamente, sempre nel Bean InfinispanConfig, che permette
di lasciare i metodi di logica di business più puliti e concentrate solo nel Bean di configurazione la gestione
della creazione Cache.

Data quindi la definizione della Cache (risiedente in un file di properties o anche nel codice stesso)

```xml
        String xml = """
        <distributed-cache name="books" mode="SYNC" statistics="true">
            <encoding>
                <key media-type="text/plain"/>
                <value media-type="application/x-protostream"/>
            </encoding>
        </distributed-cache>
        """;
```

```java
// cache interna preinstallata in Data Grid per registrare i file .proot/definizioneschema/errori compilazione (.errors)
RemoteCache<String, String> meta = manager.getCache("___protobuf_metadata");
        
//crea la cache se non esiste utilizzando la definizione xml della cache
manager.administration().getOrCreateCache("books", new StringConfiguration(xml));
        
// crea un'istanza dello schema
BookStoreSchema schema = new BookStoreSchemaImpl();
        
// registra il file .proto nel server
meta.put(schema.getProtoFileName(), schema.getProtoFile());

```
---
# 5. Producer

Con questo settaggio, il producer della cache dovrà occuparsi solo 
della creazione dell'istanza della cache e della put/get (BooksCacheProducer)

```java
            Author author = new Author("Isaac", "Asimov");

            Book book = new Book(
                    UUID.randomUUID().toString(),
                    "Foundation",
                    "Classic Sci-Fi Novel",
                    1951,
                    Collections.singletonList(author),
                    Language.ENGLISH
            );

            booksCache.put(book.getId(), book);
```


