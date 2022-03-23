package eu.europa.ec.itb.csv.web;

import eu.europa.ec.itb.csv.validation.FileManager;
import eu.europa.ec.itb.validation.commons.web.BaseFileController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class FileControllerTest {

    private FileController createFileController() throws Exception {
        var fileController = new FileController();
        var fileManagerField = BaseFileController.class.getDeclaredField("fileManager");
        fileManagerField.setAccessible(true);
        var fileManager = mock(FileManager.class);
        doReturn("ITB-UUID.csv").when(fileManager).getInputFileName(any());
        fileManagerField.set(fileController, fileManager);
        return fileController;
    }

    @Test
    void testGetInputFileName() throws Exception {
        var result = createFileController().getInputFileName("UUID");
        assertEquals("ITB-UUID.csv", result);
    }

}
