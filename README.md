# The project
The goal was to create a **proof of concept** of a **Blob** binary data type, which can be **sent** or **retrieved** with any service using the **gRPC** framework.
The project must be executed using **Java 8 x64**. It has been tested on Windows 10 x64 but should also work on other platform.

#### What is a Blob ?
- A data type containing binary data.
- Used in the communication between a **client** and a **device**.

#### What is a Database File ?
- A data type like a **Blob** except it contains **Json metadata** information such as the creation, expiration date, the length of the data, etc.
- Used in the communication between a **device** and the **file server**.

## Parts
- The Client
    - **Consumes** RPC services of a device
- The Device: 
    - **Provides** RPC services for the clients
    - Holds a **file server** instance to Upload / Update / Delete / Download Blob
- The File Server
    - Holds temporary **database files** 
    
## Services
- Binary Upload
    - **Create** a Blob
    - **Upload** a Blob
    - **Delete** a Blob
- Binary Download
    - Get **Blob info**
    - Get **Blob chunk**
    - **Delete** Blob
- Image analysis
    - **Analyze** the greyscale levels of an image
- Image generator
    - **Generate** a MandelBrot fractal

## Dependencies
- log4j12: Logger
- junit-jupiter: Unit test
- grpc: Java google implementation of the RPC protocol
- lmdbjava: Java LMDB API implementation
- jackson: Serialize and deserialize Json
- expiringmap: Thread-safe map implementation that expires entries

#### Why did I choose LMDB (Lightning Memory-Mapped Database) ?
http://www.lmdb.tech/
- NoSQL (Key / Value)
- Supports multiple processes & threads
- Embedded (no setup needed)
- Cross-platform 
- Language free
- Fast & simple (written in C)
   
## Todo
- Make **LMDBFileServer** catch internal exception and throw them back as **FileServerException**
- Support **SSL**
- Add more **unit test** for the file server, Client and device and other classes
- Improve **error management** between service provider and consumer
- Improve **javadoc**
- Make **Maven** automatically build the whole project
- Add more / Improve **logging**
- Add client **User interface**
- Replace Objects.requireNonNull with @NonNull annotation
- Replace gRPC Error message with the standard gRPC error management system 
- Add header to files