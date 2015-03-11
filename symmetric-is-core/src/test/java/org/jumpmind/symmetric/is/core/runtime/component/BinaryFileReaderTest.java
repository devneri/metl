package org.jumpmind.symmetric.is.core.runtime.component;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jumpmind.symmetric.is.core.model.Component;
import org.jumpmind.symmetric.is.core.model.FlowStep;
import org.jumpmind.symmetric.is.core.model.FlowVersion;
import org.jumpmind.symmetric.is.core.model.ComponentVersion;
import org.jumpmind.symmetric.is.core.model.Resource;
import org.jumpmind.symmetric.is.core.model.Folder;
import org.jumpmind.symmetric.is.core.model.Setting;
import org.jumpmind.symmetric.is.core.runtime.Message;
import org.jumpmind.symmetric.is.core.runtime.StartupMessage;
import org.jumpmind.symmetric.is.core.runtime.flow.IMessageTarget;
import org.jumpmind.symmetric.is.core.runtime.resource.IResourceFactory;
import org.jumpmind.symmetric.is.core.runtime.resource.ResourceFactory;
import org.jumpmind.symmetric.is.core.runtime.resource.localfile.LocalFileResource;
import org.jumpmind.symmetric.is.core.utils.TestUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class BinaryFileReaderTest {

    private static IResourceFactory resourceFactory;
    private static FlowStep readerFlowStep;
    private static final String FILE_PATH = "build/files/";
    private static final String FILE_NAME = "binary_test.bin";
    private static final String FILE_DATA = "This is a binary file to be read.";

    @BeforeClass
    public static void setup() throws Exception {

        resourceFactory = new ResourceFactory();
        createTestFileToRead();
        readerFlowStep = createReaderFlowStep();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testBinarytReaderFlowFromStartupMsg() throws Exception {

        BinaryFileReader reader = new BinaryFileReader();
        reader.setFlowStep(readerFlowStep);
        reader.start(null, resourceFactory);
        Message msg = new StartupMessage();
        MessageTarget msgTarget = new MessageTarget();
        reader.handle(msg, msgTarget);

        assertEquals(1, msgTarget.getTargetMessageCount());
        assertArrayEquals((byte[]) msgTarget.getMessage(0).getPayload(),
                FILE_DATA.getBytes());
    }

    private static void createTestFileToRead() throws Exception {
        createTestDirectory();
        byte[] data = FILE_DATA.getBytes();
        FileOutputStream outStream = new FileOutputStream(FILE_PATH + FILE_NAME);
        outStream.write(data);
        outStream.close();
    }

    private static void createTestDirectory() {
        File dir = new File(FILE_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    private static FlowStep createReaderFlowStep() {

        Folder folder = TestUtils.createFolder("Test Folder");
        FlowVersion flow = TestUtils.createFlowVersion("TestFlow", folder);
        Component component = TestUtils.createComponent(BinaryFileReader.TYPE, false);
        Setting[] settingData = createReaderSettings();
        ComponentVersion componentVersion = TestUtils.createComponentVersion(component, null,
                settingData);
        componentVersion.setResource(createResource(createResourceSettings()));
        FlowStep readerComponent = new FlowStep();
        readerComponent.setFlowVersionId(flow.getId());
        readerComponent.setComponentVersionId(componentVersion.getId());
        readerComponent.setCreateBy("Test");
        readerComponent.setCreateTime(new Date());
        readerComponent.setLastModifyBy("Test");
        readerComponent.setLastModifyTime(new Date());
        readerComponent.setComponentVersion(componentVersion);
        return readerComponent;
    }

    private static Resource createResource(List<Setting> settings) {
        Resource resource = new Resource();
        Folder folder = TestUtils.createFolder("Test Folder Resource");
        resource.setName("Test Resource");
        resource.setFolderId("Test Folder Resource");
        resource.setType(LocalFileResource.TYPE);
        resource.setFolder(folder);
        resource.setSettings(settings);

        return resource;
    }

    private static Setting[] createReaderSettings() {
        Setting[] settings = new Setting[2];
        settings[0] = new Setting(BinaryFileReader.BINARYFILEREADER_RELATIVE_PATH, FILE_NAME);
        settings[1] = new Setting(BinaryFileReader.BINARYFILEREADER_SIZE_PER_MESSAGE, "1");
        return settings;
    }

    private static List<Setting> createResourceSettings() {
        List<Setting> settings = new ArrayList<Setting>(2);
        settings.add(new Setting(LocalFileResource.LOCALFILE_PATH, FILE_PATH));
        settings.add(new Setting(LocalFileResource.LOCALFILE_MUST_EXIST, "true"));
        return settings;
    }

    class MessageTarget implements IMessageTarget {

        List<Message> targetMsgArray = new ArrayList<Message>();

        @Override
        public void put(Message message) {
            targetMsgArray.add(message);
        }

        public Message getMessage(int idx) {
            return targetMsgArray.get(idx);
        }

        public int getTargetMessageCount() {
            return targetMsgArray.size();
        }
    }
}