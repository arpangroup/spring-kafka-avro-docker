## Avro
Apache Avro is a data serialization system commonly used in big data applications (like Kafka, Hadoop) for its compact and efficient format. An Avro schema defines the structure of your data. Here’s how to create one from scratch.

## 1. Basic Avro Schema Structure
An Avro schema is defined in JSON format with the following components:
- **type:** The data type (e.g., `record`, `string`, int).
- **name:** The name of the record (if it’s a record type).
- **fields:** A list of fields (for records).

### Example
````json
{
  "type": "record",
  "name": "User",
  "namespace": "com.example",
  "fields": [
    { "name": "id", "type": "int" },
    { "name": "name", "type": "string" },
    { "name": "email", "type": ["null", "string"], "default": null },
    { "name": "age", "type": "int", "default": 0 }
  ]
}

````

### Explanation:
- **type:** `record` means it’s a structured data type with named fields.
- **name:** The name of the record (in this case, `User`).
- **namespace:** Optional, used to group related schemas.
- **fields:** A list of fields with their names, types, and optional defaults.
- **Union type:** `["null", "string"]` allows the field to be either `null` or a `string`.


## 2. Primitive Types in Avro
- **string:** Unicode character sequence.
- **int:** 32-bit integer.
- **long:** 64-bit integer.
- **float:** 32-bit floating point.
- **double:** 64-bit floating point.
- **boolean:** true or false.
- **null:** Represents a null value.

## 3. Complex Types in Avro
- **record:** A named group of fields (like a struct).
- **enum:** A set of named values.
- **array:** A list of values of a specified type.
- **map:** A set of key-value pairs (keys are strings).
- **union:** A value that can be one of several types.

````json
{
  "type": "record",
  "name": "Order",
  "fields": [
    { "name": "orderId", "type": "string" },
    { "name": "items", "type": {
        "type": "array",
        "items": "string"
      }
    },
    { "name": "status", "type": {
        "type": "enum",
        "name": "OrderStatus",
        "symbols": ["PENDING", "SHIPPED", "DELIVERED", "CANCELLED"]
      }
    },
    { "name": "metadata", "type": {
        "type": "map",
        "values": "string"
      }
    }
  ]
}

````


## Maven Dependency for Java (Avro)
````xml
<dependency>
    <groupId>org.apache.avro</groupId>
    <artifactId>avro</artifactId>
    <version>1.11.3</version>
</dependency>
````

## Java Example
````java
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.BinaryEncoder;

import java.io.ByteArrayOutputStream;

public class AvroExample {
    public static void main(String[] args) throws Exception {
        // Load schema
        Schema schema = new Schema.Parser().parse(new File("user.avsc"));

        // Create a record
        GenericRecord user = new GenericRecordBuilder(schema)
            .set("id", 1)
            .set("name", "John Doe")
            .set("email", "john@example.com")
            .set("age", 30)
            .build();

        // Serialize the record
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(schema);
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        writer.write(user, encoder);
        encoder.flush();

        byte[] serializedData = out.toByteArray();
        System.out.println("Serialized data: " + Arrays.toString(serializedData));
    }
}
````

## Python Example (Using avro-python3)
````python
import avro.schema
import avro.io
import io

# Load schema from a file or string
schema = avro.schema.parse(open("user.avsc", "r").read())

# Example data
data = {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "age": 30
}

# Serialize data
bytes_writer = io.BytesIO()
encoder = avro.io.BinaryEncoder(bytes_writer)
datum_writer = avro.io.DatumWriter(schema)
datum_writer.write(data, encoder)

# Get serialized data
serialized_data = bytes_writer.getvalue()

# Deserialize data
bytes_reader = io.BytesIO(serialized_data)
decoder = avro.io.BinaryDecoder(bytes_reader)
datum_reader = avro.io.DatumReader(schema)
deserialized_data = datum_reader.read(decoder)

print(deserialized_data)

````



# References:
1. [What is AVRO Format and why it's used?](https://youtu.be/c4Z6A-DPL8E?si=Mqq65vfn8N7eZRM1)
2. [What is Columnar Database? | Data Engineer Roadmap](https://youtu.be/dKENkPQVRlQ?si=K9g9oogavKudJk7F)
