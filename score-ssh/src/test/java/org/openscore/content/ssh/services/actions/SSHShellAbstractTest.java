package org.openscore.content.ssh.services.actions;

import com.hp.oo.sdk.content.plugin.GlobalSessionObject;
import com.hp.oo.sdk.content.plugin.SessionResource;
import org.openscore.content.ssh.entities.KeyFile;
import org.openscore.content.ssh.entities.SSHConnection;
import org.openscore.content.ssh.entities.SSHShellInputs;
import org.openscore.content.ssh.services.SSHService;
import org.openscore.content.ssh.services.actions.SSHShellAbstract;
import org.openscore.content.ssh.services.impl.SSHServiceImpl;
import org.openscore.content.ssh.utils.CacheUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CacheUtils.class})
public class SSHShellAbstractTest {

    @Mock
    private SSHServiceImpl sshServiceMock;
    @Mock
    private SessionResource<Map<String, SSHConnection>> sessionResourceMock;
    @Mock
    private SSHShellInputs sshShellInputsMock;
    @Mock
    private GlobalSessionObject<Map<String, SSHConnection>> sshGlobalSessionObjectMock;

    @Test
    public void testAddSecurityProvider() throws Exception {
        SSHShellAbstract sshShellAbstract = new SSHShellAbstract() {
        };
        boolean securityProviderAdded = sshShellAbstract.addSecurityProvider();
        assertTrue(securityProviderAdded);
        sshShellAbstract.addSecurityProvider();
        securityProviderAdded = sshShellAbstract.addSecurityProvider();
        assertFalse(securityProviderAdded);
        sshShellAbstract.removeSecurityProvider();
    }

    @Test
    public void testRemoveSecurityProvider() throws Exception {
        SSHShellAbstract sshShellAbstract = new SSHShellAbstract() {
        };
        boolean securityProviderAdded = sshShellAbstract.addSecurityProvider();
        assertTrue(securityProviderAdded);
        sshShellAbstract.removeSecurityProvider();
        securityProviderAdded = sshShellAbstract.addSecurityProvider();
        assertTrue(securityProviderAdded);
        sshShellAbstract.removeSecurityProvider();
    }

    @Test
    public void testGetKeyFile() throws Exception {
        SSHShellAbstract sshShellAbstract = new SSHShellAbstract() {
        };
        final String myPrivateKeyFileName = "myPrivateKeyFile";
        final String myPrivateKeyPassPhraseName = "myPrivateKeyPassPhrase";
        KeyFile keyFile = sshShellAbstract.getKeyFile(myPrivateKeyFileName, myPrivateKeyPassPhraseName);
        assertEquals(myPrivateKeyFileName, keyFile.getKeyFilePath());
        assertEquals(myPrivateKeyPassPhraseName, keyFile.getPassPhrase());

        keyFile = sshShellAbstract.getKeyFile(myPrivateKeyFileName, null);
        assertEquals(myPrivateKeyFileName, keyFile.getKeyFilePath());
        assertEquals(null, keyFile.getPassPhrase());

        keyFile = sshShellAbstract.getKeyFile(null, myPrivateKeyPassPhraseName);
        assertNull(keyFile);
    }

    @Test
    public void testGetFromCache() throws Exception {
        SSHShellAbstract sshShellAbstract = new SSHShellAbstract() {
        };
        mockStatic(CacheUtils.class);
        String sessionId = "sessionId";

        when(sshShellInputsMock.getSshGlobalSessionObject()).thenReturn(sshGlobalSessionObjectMock);
        when(sshGlobalSessionObjectMock.getResource()).thenReturn(sessionResourceMock);
        when(CacheUtils.getFromCache(sessionResourceMock, sessionId)).thenReturn(sshServiceMock);
        SSHService serviceFromCache = sshShellAbstract.getFromCache(sshShellInputsMock, sessionId);
        assertNotNull(serviceFromCache);
        assertEquals(sshServiceMock, serviceFromCache);
        serviceFromCache = sshShellAbstract.getFromCache(sshShellInputsMock, "");
        assertNull(serviceFromCache);
        serviceFromCache = sshShellAbstract.getFromCache(sshShellInputsMock, null);
        assertNull(serviceFromCache);
    }

    @Test
    public void testSaveToCache() throws Exception {
        SSHShellAbstract sshShellAbstract = new SSHShellAbstract() {
        };
        String sessionId = "sessionId";
        boolean savedToCache = sshShellAbstract.saveToCache(sshGlobalSessionObjectMock, sshServiceMock, sessionId);
        assertEquals(false, savedToCache);

        when(sshServiceMock.saveToCache(sshGlobalSessionObjectMock, sessionId)).thenReturn(true);
        savedToCache = sshShellAbstract.saveToCache(sshGlobalSessionObjectMock, sshServiceMock, sessionId);
        assertEquals(true, savedToCache);

        final GlobalSessionObject<Map<String, SSHConnection>> sessionParam = new GlobalSessionObject<>();
        when(sshServiceMock.saveToCache(sessionParam, sessionId)).thenReturn(true);
        savedToCache = sshShellAbstract.saveToCache(sessionParam, sshServiceMock, sessionId);
        assertEquals(sessionParam.getName(), "sshSessions:default-id");
        assertEquals(true, savedToCache);
    }

    @Test
    public void testPopulateResult() throws Exception {
        SSHShellAbstract sshShellAbstract = new SSHShellAbstract() {
        };
        Map<String, String> returnResult = new HashMap<>();
        final String testExceptionMessage = "Test exception";
        Exception testException = new Exception(testExceptionMessage);
        sshShellAbstract.populateResult(returnResult, testException);

        assertEquals(returnResult.get("returnResult"), testExceptionMessage);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        testException.printStackTrace(pw);
        pw.close();

        assertEquals(returnResult.get("exception"), sw.toString());
        assertEquals(returnResult.get("returnCode"), "-1");
    }
}