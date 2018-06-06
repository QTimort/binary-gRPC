import fr.diguiet.grpc.common.utils.TimestampUtils;
import fr.diguiet.grpc.fileserver.DatabaseFile;
import fr.diguiet.grpc.fileserver.exception.FileServerException;
import fr.diguiet.grpc.fileserver.IFileServer;
import fr.diguiet.grpc.common.utils.BytesUtils;
import fr.diguiet.grpc.common.utils.FileUtils;
import fr.diguiet.grpc.fileserver.LMDBFileServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class FileServer {
    private static final String TEST_DB_FOLDER_NAME = "test";
    private final LMDBFileServer.Builder dbBuilder = LMDBFileServer.newInstance();
    private final UUID TEST_UID = UUID.fromString("5a851439-4138-4755-ae35-f073e2dcf977");

    private static File getTestDbParentDirectory() throws IOException {
        return (FileUtils.createFolderIfNotPresent(FileServer.TEST_DB_FOLDER_NAME, FileUtils.getCurrentWorkingDirectory()));
    }

    private void setupBuilder() throws IOException {
        this.dbBuilder.setParentDbDirectory(FileServer.getTestDbParentDirectory());
    }

    public static LMDBFileServer.Builder getNewSetupBuilder() throws IOException {
        return (LMDBFileServer.newInstance().setParentDbDirectory(FileServer.getTestDbParentDirectory()));
    }

    @Test
    public void fileServer() throws IOException {
        this.setupBuilder();
        try (IFileServer fileServer = this.dbBuilder.build()) {
            fileServer.openOrCreate();
            final UUID id = UUID.randomUUID();
        } catch (FileServerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAvailableSpace() throws IOException {
        this.setupBuilder();
        try (IFileServer fileServer = (LMDBFileServer) this.dbBuilder.build()) {
            fileServer.openOrCreate();
            System.out.println("Available bytes: " + fileServer.getAvailableUsableByte());
        } catch (FileServerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void invalidPath() throws IOException {
        this.setupBuilder();
        final File file = new File(FileUtils.getCurrentWorkingDirectory(), "pom.xml");
        final LMDBFileServer.Builder builder = this.dbBuilder;
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.setParentDbDirectory(file));
    }

    @Test
    public void testWebNet() throws IOException {
        this.setupBuilder();
        IFileServer fileServer = this.dbBuilder.build();
        try {
            fileServer.openOrCreate();
            fileServer = null;
            System.gc();
        } catch (FileServerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void fileServerRemoveExpired() throws IOException {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        this.setupBuilder();
        try (IFileServer fileServer = this.dbBuilder.build()) {
            fileServer.openOrCreate();
            fileServer.removeExpired();
        } catch (FileServerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void uploadDownload() throws IOException {
        this.setupBuilder();
        Logger logger = LoggerFactory.getLogger(this.getClass());
        final UUID id = UUID.randomUUID();
        final ByteBuffer bytes = BytesUtils.getRandom(4096);
        final ByteBuffer bytes2 = BytesUtils.getRandom(4096);
        try (IFileServer fileServer = this.dbBuilder.build()) {
            fileServer.openOrCreate();
            fileServer.upload(id, TimestampUtils.nowPlusSeconds(10), BytesUtils.toByteArray(bytes));
            DatabaseFile databaseFile = fileServer.getFile(id);
            Assertions.assertNotNull(databaseFile);
            Assertions.assertNotNull(databaseFile.getData());
            Assertions.assertNotNull(databaseFile.getDatabaseFileMetadata());
            Assertions.assertTrue(Arrays.equals(BytesUtils.getCheckSum(bytes), BytesUtils.getCheckSum(databaseFile.getData())));
            Assertions.assertTrue(Arrays.equals(BytesUtils.getCheckSum(bytes), databaseFile.getDatabaseFileMetadata().getChecksum()));
            Assertions.assertEquals(BytesUtils.toByteArray(bytes).length, databaseFile.getData().length);
            Assertions.assertEquals(BytesUtils.toByteArray(bytes).length, databaseFile.getDatabaseFileMetadata().getDataLength());
            Assertions.assertEquals(id, databaseFile.getDatabaseFileMetadata().getId());

            fileServer.upload(id, TimestampUtils.nowPlusSeconds(10), BytesUtils.toByteArray(bytes2));
            final ByteBuffer bytestwo = ByteBuffer.wrap(BytesUtils.merge(BytesUtils.toByteArray(bytes), BytesUtils.toByteArray(bytes2)));
            databaseFile = fileServer.getFile(id);
            Assertions.assertNotNull(databaseFile);
            Assertions.assertNotNull(databaseFile.getData());
            Assertions.assertNotNull(databaseFile.getDatabaseFileMetadata());
            Assertions.assertTrue(Arrays.equals(BytesUtils.getCheckSum(bytestwo), BytesUtils.getCheckSum(databaseFile.getData())));
            Assertions.assertTrue(Arrays.equals(BytesUtils.getCheckSum(bytestwo), databaseFile.getDatabaseFileMetadata().getChecksum()));
            Assertions.assertEquals(bytestwo.array().length, databaseFile.getData().length);
            Assertions.assertEquals(bytestwo.array().length, databaseFile.getDatabaseFileMetadata().getDataLength());
            Assertions.assertEquals(id, databaseFile.getDatabaseFileMetadata().getId());

            fileServer.delete(id);
            Assertions.assertNull(fileServer.getFile(id));
            Assertions.assertNull(fileServer.download(id));
            Assertions.assertNull(fileServer.getFileMetaData(id));
        } catch (FileServerException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testFileExpiration() throws IOException, FileServerException, InterruptedException {
        this.setupBuilder();
        Logger logger = LoggerFactory.getLogger(this.getClass());
        final UUID id = UUID.randomUUID();
        final byte[] bytes = BytesUtils.toByteArray(BytesUtils.getRandom(4096));
        try (IFileServer fileServer = this.dbBuilder.build()) {
            fileServer.openOrCreate();
            fileServer.upload(id, TimestampUtils.nowPlusSeconds(10), bytes);
            final DatabaseFile databaseFile0 = fileServer.getFile(id);
            Assertions.assertNotNull(databaseFile0);
            fileServer.removeExpired();
            final DatabaseFile databaseFile = fileServer.getFile(id);
            Assertions.assertNotNull(databaseFile);
            Thread.sleep(1000 * 10);
            final DatabaseFile databaseFile2 = fileServer.getFile(id);
            Assertions.assertNull(databaseFile2);
            fileServer.removeExpired();
            final DatabaseFile databaseFile3 = fileServer.getFile(id);
            Assertions.assertNull(databaseFile3);
        }
    }
}
